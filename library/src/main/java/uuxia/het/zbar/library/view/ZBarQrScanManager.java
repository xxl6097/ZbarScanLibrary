package uuxia.het.zbar.library.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Field;

import uuxia.het.zbar.library.R;
import uuxia.het.zbar.library.callback.ZBarQrScanCallBack;
import uuxia.het.zbar.library.coder.ZBarDecoderNativeManager;

/**
 * Created by uuxia-mac on 16/1/17.
 */
public class ZBarQrScanManager {
    private Activity activity;
    private Camera mCamera;
    private ZBarCameraPreview mPreview;
    private Handler autoFocusHandler;
    private ZBarCameraManager mZBarCameraManager;

    private TextView scanResult;
    private FrameLayout scanPreview;
    private Button scanRestart;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    private boolean barcodeScanned = false;
    private boolean previewing = true;

    private static ZBarQrScanManager instance;
    private ZBarQrScanCallBack qrScanCallBack;

    public ZBarQrScanManager(Activity activity, View view) {
        this.activity = activity;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById(view);
        addEvents();
        initViews();
    }

    private void findViewById(View v) {
        scanPreview = (FrameLayout) v.findViewById(R.id.capture_preview);
        scanResult = (TextView) v.findViewById(R.id.capture_scan_result);
        scanRestart = (Button) v.findViewById(R.id.capture_restart_scan);
        scanContainer = (RelativeLayout) v.findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) v.findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) v.findViewById(R.id.capture_scan_line);
        if (scanLine != null){
            scanLine.setVisibility(View.VISIBLE);
        }
    }

    private void addEvents() {
        scanRestart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (barcodeScanned) {
                    barcodeScanned = false;
                    scanResult.setText("Scanning...");
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCB);
                }
            }
        });
    }

    private void initViews() {
        autoFocusHandler = new Handler();
        mZBarCameraManager = new ZBarCameraManager(activity);
        try {
            mZBarCameraManager.openDriver();
        } catch (IOException e) {
            e.printStackTrace();
            if (qrScanCallBack != null){
                qrScanCallBack.onException(null,e);
            }
        }

        mCamera = mZBarCameraManager.getCamera();
        mPreview = new ZBarCameraPreview(activity, mCamera, previewCb, autoFocusCB);
        scanPreview.addView(mPreview);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.85f);
        animation.setDuration(3000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);
    }

    /**
     * release camera
     */
    public void release() {
        if (previewing && mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
//            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();

            // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < size.height; y++) {
                for (int x = 0; x < size.width; x++)
                    rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
            }

            // 宽高也要调整
            int tmp = size.width;
            size.width = size.height;
            size.height = tmp;

            initCrop();
            ZBarDecoderNativeManager zBarDecoder = new ZBarDecoderNativeManager();
            String result = zBarDecoder.decodeCrop(rotatedData, size.width, size.height, mCropRect.left, mCropRect.top, mCropRect.width(), mCropRect.height());
            if (!TextUtils.isEmpty(result)) {
//                previewing = false;
//                mCamera.setPreviewCallback(null);
//                mCamera.stopPreview();
//                scanResult.setText(qrScanCallBack+"barcode result " + result);

                release();
                if (qrScanCallBack != null){
                    qrScanCallBack.onResult(result);
                }
                barcodeScanned = true;
                Log.i("--------","----"+result);
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = mZBarCameraManager.getCameraResolution().y;
        int cameraHeight = mZBarCameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setCallBack(ZBarQrScanCallBack qrScanCallBack) {
        this.qrScanCallBack = qrScanCallBack;
    }
}
