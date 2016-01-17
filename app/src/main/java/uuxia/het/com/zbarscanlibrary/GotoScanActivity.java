package uuxia.het.com.zbarscanlibrary;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import uuxia.het.com.library.callback.QrScanCallBack;
import uuxia.het.com.library.view.QrScanManager;

public class GotoScanActivity extends Activity implements QrScanCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_goto_scan);
    }

    private QrScanManager qrScanManager;

    @Override
    protected void onResume() {
        super.onResume();
        qrScanManager = new QrScanManager(this,findViewById(R.id.borad));
        qrScanManager.setCallBack(this);
    }

    public void onPause() {
        super.onPause();
        qrScanManager.release();
    }

    @Override
    public void onResult(String resuilt) {
        Toast.makeText(this, resuilt, Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", resuilt);
//			bundle.putParcelable("bitmap", barcode);
        resultIntent.putExtras(bundle);
        this.setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onException(Object object, Exception e) {

    }
}
