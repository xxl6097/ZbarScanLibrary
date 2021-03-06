package uuxia.het.com.zbarscanlibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import uuxia.het.zbar.library.ZBarQrScanManager;
import uuxia.het.zbar.library.callback.ZBarQrScanCallBack;

public class GotoScanActivity extends Activity implements ZBarQrScanCallBack {

    private ZBarQrScanManager ZBarZBarQrScanManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_goto_scan);
        ZBarZBarQrScanManager = new ZBarQrScanManager();
        ZBarZBarQrScanManager.onCreate(this);
        ZBarZBarQrScanManager.setCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZBarZBarQrScanManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ZBarZBarQrScanManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZBarZBarQrScanManager.onDestroy();
    }

    @Override
    public void onResult(String resuilt) {
        Toast.makeText(this, resuilt, Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", resuilt);
        resultIntent.putExtras(bundle);
        this.setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onException(Object object, Exception e) {

    }
}
