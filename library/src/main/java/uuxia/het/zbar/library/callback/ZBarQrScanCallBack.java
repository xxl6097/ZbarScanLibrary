package uuxia.het.zbar.library.callback;

/**
 * Created by uuxia-mac on 16/1/17.
 */
public interface ZBarQrScanCallBack {
    void onResult(String resuilt);
    void onException(Object object, Exception e);
}
