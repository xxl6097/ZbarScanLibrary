package uuxia.het.zbar.library.qrcode.camera.decode;

import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import uuxia.het.zbar.library.ZBarQrScanManager;
import uuxia.het.zbar.library.qrcode.utils.Constants;


/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2016-01-18 16:49
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: DecodeHandler.java
 * Create: 2016/1/18 16:49
 */
public class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final ZBarQrScanManager activity;
    private boolean running = true;
    private DecodeUtils mDecodeUtils = null;
    private int mDecodeMode = DecodeUtils.DECODE_MODE_ZBAR;

    DecodeHandler(ZBarQrScanManager activity) {
        this.activity = activity;
        mDecodeUtils = new DecodeUtils(DecodeUtils.DECODE_DATA_MODE_ALL);
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case Constants.ID_DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Constants.ID_QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();

        Camera.Size size = activity.getCameraManager().getPreviewSize();
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
        }

        int tmp = size.width;
        size.width = size.height;
        size.height = tmp;

        String resultStr = null;
        Rect cropRect = activity.getCropRect();
        if (null == cropRect) {
            activity.initCrop();
        }
        cropRect = activity.getCropRect();

        mDecodeUtils.setDataMode(activity.getDataMode());

        String zbarStr = mDecodeUtils.decodeWithZbar(rotatedData, size.width, size.height, cropRect);
//        String zxingStr = mDecodeUtils.decodeWithZxing(rotatedData, size.width, size.height, cropRect);

        if (!TextUtils.isEmpty(zbarStr)) {
            mDecodeMode = DecodeUtils.DECODE_MODE_ZBAR;
            resultStr = zbarStr;
        }/* else if (!TextUtils.isEmpty(zxingStr)) {
            mDecodeMode = DecodeUtils.DECODE_MODE_ZXING;
            resultStr = zxingStr;
        }*/

        Handler handler = activity.getHandler();
        if (!TextUtils.isEmpty(resultStr)) {
            long end = System.currentTimeMillis();
            if (handler != null) {
                Message message = Message.obtain(handler, Constants.ID_DECODE_SUCCESS, resultStr);
                Bundle bundle = new Bundle();

                bundle.putInt(DecodeThread.DECODE_MODE, mDecodeMode);
                bundle.putString(DecodeThread.DECODE_TIME, (end - start) + "ms");
//                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, Constants.ID_DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }
}
