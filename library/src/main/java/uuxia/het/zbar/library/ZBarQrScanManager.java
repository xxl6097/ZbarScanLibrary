package uuxia.het.zbar.library;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

import uuxia.het.zbar.library.callback.ZBarQrScanCallBack;
import uuxia.het.zbar.library.qrcode.CaptureActivityHandler;
import uuxia.het.zbar.library.qrcode.camera.CameraManager;
import uuxia.het.zbar.library.qrcode.camera.decode.DecodeUtils;
import uuxia.het.zbar.library.qrcode.utils.BeepManager;
import uuxia.het.zbar.library.qrcode.utils.InactivityTimer;


public class ZBarQrScanManager implements SurfaceHolder.Callback {
    public static String TAG_LOG = "ZBarQrScanManager";
    public static final int IMAGE_PICKER_REQUEST_CODE = 100;
    SurfaceView capturePreview;
    ImageView captureErrorMask;
    ImageView captureScanMask;
    RelativeLayout captureCropView;
    RelativeLayout captureContainer;
    Button captureLightBtn,capturePictureBtn;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private boolean hasSurface;
    private boolean isLightOn;
    private InactivityTimer mInactivityTimer;
    private BeepManager mBeepManager;
    private ObjectAnimator mScanMaskObjectAnimator = null;
    private Rect cropRect;
    private int dataMode = DecodeUtils.DECODE_DATA_MODE_ALL;
    private Activity activity;

    private ZBarQrScanCallBack callBack;

    public void setCallBack(ZBarQrScanCallBack call){
        callBack = call;
    }

    public void onCreate(Activity c) {
        activity = c;
        findViewById(c);
        initViewsAndEvents();
    }

    public void onResume() {
        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(activity.getApplication());

        handler = null;

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(capturePreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            capturePreview.getHolder().addCallback(this);
        }

        mInactivityTimer.onResume();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        mBeepManager.close();
        mInactivityTimer.onPause();
        cameraManager.closeDriver();

        if (!hasSurface) {
            capturePreview.getHolder().removeCallback(this);
        }

        if (null != mScanMaskObjectAnimator && mScanMaskObjectAnimator.isStarted()) {
            mScanMaskObjectAnimator.cancel();
        }
    }

    public void onDestroy() {
        mInactivityTimer.shutdown();
    }

    protected void initViewsAndEvents() {
        hasSurface = false;
        mInactivityTimer = new InactivityTimer(activity);
        mBeepManager = new BeepManager(activity);
        capturePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                readyGoForResult(CommonImagePickerListActivity.class, IMAGE_PICKER_REQUEST_CODE);
            }
        });
//
        captureLightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLightOn) {
                    captureLightBtn.setBackgroundResource(R.drawable.zbar_light_nor);
                    cameraManager.setTorch(false);
                    captureLightBtn.setSelected(false);
                } else {
                    captureLightBtn.setBackgroundResource(R.drawable.zbar_light_sel);
                    cameraManager.setTorch(true);
                    captureLightBtn.setSelected(true);
                }
                isLightOn = !isLightOn;
            }
        });

    }

    private void findViewById(Activity v) {
        capturePreview = (SurfaceView) v.findViewById(R.id.capture_preview);
        captureErrorMask = (ImageView) v.findViewById(R.id.capture_error_mask);
        captureScanMask = (ImageView) v.findViewById(R.id.capture_scan_mask);
        captureCropView = (RelativeLayout) v.findViewById(R.id.capture_crop_view);
        captureContainer = (RelativeLayout) v.findViewById(R.id.capture_container);
        captureLightBtn = (Button) v.findViewById(R.id._zbar_light);
        capturePictureBtn = (Button) v.findViewById(R.id._zbar_picture);
    }


    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        int[] location = new int[2];
        captureCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1];

        int cropWidth = captureCropView.getWidth();
        int cropHeight = captureCropView.getHeight();

        int containerWidth = captureContainer.getWidth();
        int containerHeight = captureContainer.getHeight();

        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;

        setCropRect(new Rect(x, y, width + x, height + y));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG_LOG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
//            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(holder);
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     */
    public void handleDecode(String result, Bundle bundle) {
        mInactivityTimer.onActivity();
        mBeepManager.playBeepSoundAndVibrate();
        if (callBack != null){
            callBack.onResult(result);
        }
    }

    private void onCameraPreviewSuccess() {
        initCrop();
        captureErrorMask.setVisibility(View.GONE);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.85f);
        animation.setDuration(3000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        captureScanMask.startAnimation(animation);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG_LOG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }

            onCameraPreviewSuccess();
        } catch (IOException ioe) {
            Log.w(TAG_LOG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG_LOG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }

    }

    private void displayFrameworkBugMessageAndExit() {
        captureErrorMask.setVisibility(View.VISIBLE);
    }

    public Rect getCropRect() {
        return cropRect;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }

    public int getDataMode() {
        return dataMode;
    }


}
