# ZbarScanLibrary
Zar scan open source

ZbarScanLibrary是之前的ZBar解析二维码项目的重构和优化


>* 使用了最新版本的libiconv库进行编译，版本号：1.14；下载地址： http://www.gnu.org/software/libiconv/
>* 重新修改ZBar对中文的支持，并且可以解析GBK，UTF-8格式生成的二维码图片
>* 使用最新的ndk-r10d进行ndk-build，支持64位cpu
##使用步骤
**1.在activity布局中加入如下：**

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical" >

    <include
        layout="@layout/captrue_layout"/>

</RelativeLayout>
```
**2.activity中生命周期设备：**

```
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
```


##License

```java
/*
 * Copyright (C) 2016 uuxia <263996097@qq.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```
