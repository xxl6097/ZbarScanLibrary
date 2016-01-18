package uuxia.het.zbar.library.qrcode.camera.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2016-01-18 16:50
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife -
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: DecodeUtils.java
 * Create: 2016/1/18 16:50
 */
public class DecodeUtils {

    public static final int DECODE_MODE_ZBAR = 10001;
//    public static final int DECODE_MODE_ZXING = 10002;

    public static final int DECODE_DATA_MODE_ALL = 10003;
    public static final int DECODE_DATA_MODE_QRCODE = 10004;
    public static final int DECODE_DATA_MODE_BARCODE = 10005;

    private int mDataMode;
    private ImageScanner mImageScanner;

    static {
        System.loadLibrary("iconv");
    }

    public DecodeUtils(int dataMode) {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
        mDataMode = (dataMode != 0) ? dataMode : DECODE_DATA_MODE_ALL;
    }

    public String decodeWithZbar(byte[] data, int width, int height, Rect crop) {
        changeZBarDecodeDataMode();

        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        if (null != crop) {
            barcode.setCrop(crop.left, crop.top, crop.width(), crop.height());
        }

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        return resultStr;
    }

    public String decodeWithZbar(Bitmap bitmap) {
        changeZBarDecodeDataMode();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Image barcode = new Image(width, height, "Y800");

        int size = width * height;
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] pixelsData = new byte[size];
        for (int i = 0; i < size; i++) {
            pixelsData[i] = (byte) pixels[i];
        }

        barcode.setData(pixelsData);

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        return resultStr;
    }


    private void changeZBarDecodeDataMode() {
        switch (mDataMode) {
            case DECODE_DATA_MODE_ALL:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 1);
                break;

            case DECODE_DATA_MODE_QRCODE:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                // bar code
                mImageScanner.setConfig(Symbol.I25, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODABAR, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE93, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.DATABAR, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.DATABAR_EXP, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.ISBN10, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.ISBN13, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.PARTIAL, Config.ENABLE, 0);
                // qr code
                mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.PDF417, Config.ENABLE, 1);

                break;

            case DECODE_DATA_MODE_BARCODE:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                // bar code
                mImageScanner.setConfig(Symbol.I25, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODABAR, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE93, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.DATABAR, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.DATABAR_EXP, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.ISBN10, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.ISBN13, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.PARTIAL, Config.ENABLE, 1);
                // qr code
                mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.PDF417, Config.ENABLE, 0);

                break;
        }
    }

    public int getDataMode() {
        return mDataMode;
    }

    public void setDataMode(int dataMode) {
        this.mDataMode = dataMode;
    }
}
