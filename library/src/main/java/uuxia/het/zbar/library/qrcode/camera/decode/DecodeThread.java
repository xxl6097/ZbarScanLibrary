package uuxia.het.zbar.library.qrcode.camera.decode;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

import uuxia.het.zbar.library.ZBarQrScanManager;


/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2016-01-18 16:50
 * Description: This thread does all the heavy lifting of decoding the images.
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: DecodeThread.java
 * Create: 2016/1/18 16:50
 */
public class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "BARCODE_BITMAP";
    public static final String DECODE_MODE = "DECODE_MODE";
    public static final String DECODE_TIME = "DECODE_TIME";

    private final ZBarQrScanManager activity;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public DecodeThread(ZBarQrScanManager activity) {

        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);
    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
