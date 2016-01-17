package uuxia.het.com.zbarscanlibrary;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import uuxia.het.com.library.callback.QrScanCallBack;
import uuxia.het.com.library.view.QrScanManager;

public class MainActivity extends Activity implements QrScanCallBack{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        QrScanManager.init(this,findViewById(R.id.borad));
        QrScanManager.setCallBack(this);
    }

    public void onPause() {
        super.onPause();
        QrScanManager.release();
    }

    @Override
    public void onResult(String resuilt) {
        Toast.makeText(this,resuilt,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onException(Object object, Exception e) {

    }
}