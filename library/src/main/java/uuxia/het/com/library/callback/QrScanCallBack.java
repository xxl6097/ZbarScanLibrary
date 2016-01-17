package uuxia.het.com.library.callback;

/**
 * Created by uuxia-mac on 16/1/17.
 */
public interface QrScanCallBack {
    void onResult(String resuilt);
    void onException(Object object,Exception e);
}
