package uuxia.het.zbar.library.qrcode;

import android.os.Handler;
import android.os.Message;

import uuxia.het.zbar.library.ZBarQrScanManager;
import uuxia.het.zbar.library.qrcode.camera.CameraManager;
import uuxia.het.zbar.library.qrcode.camera.decode.DecodeThread;
import uuxia.het.zbar.library.qrcode.utils.Constants;


/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2016-01-18 16:56
 * Description:This class handles all the messaging which comprises the state machine for capture.
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife -
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: CaptureActivityHandler.java
 * Create: 2016/1/18 16:56
 */
public class CaptureActivityHandler extends Handler {

    private final ZBarQrScanManager activity;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public CaptureActivityHandler(ZBarQrScanManager activity,
                                  CameraManager cameraManager) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Constants.ID_RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case Constants.ID_DECODE_SUCCESS:
                state = State.SUCCESS;
                activity.handleDecode((String) message.obj, message.getData());
                break;
            case Constants.ID_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constants.ID_DECODE);
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Constants.ID_QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(Constants.ID_DECODE_SUCCESS);
        removeMessages(Constants.ID_DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constants.ID_DECODE);
        }
    }

}
