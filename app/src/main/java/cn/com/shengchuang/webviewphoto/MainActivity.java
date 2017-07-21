package cn.com.shengchuang.webviewphoto;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.File;

import cn.com.shengchuang.webviewphoto.Zxing.CaptureActivity;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private static final int REQ_CAMERA = FILECHOOSER_RESULTCODE+1;//拍照
    private static final int REQ_CHOOSE = REQ_CAMERA+1; //调用相册
    private Uri imageUri;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = REQ_CHOOSE+1;
    private static final int ABLUM_PERMISSIONS_REQUEST_CODE = CAMERA_PERMISSIONS_REQUEST_CODE+1;
    private static final int SCAN_CODE = ABLUM_PERMISSIONS_REQUEST_CODE+1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.web);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBlockNetworkImage(false);
        mWebView.addJavascriptInterface(this,"scan");
        mWebView.loadUrl("file:///android_asset/addfigure.html");
        /**
         * 不同手机相机相册兼容问题
         */
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }
    @JavascriptInterface
    public void openScan(){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, SCAN_CODE);
    }

    /**
     * android webview 兼容相机相册选择
     */
    public class MyWebChromeClient extends WebChromeClient{
        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,FileChooserParams fileChooserParams) {
            mUploadCallbackAboveL = filePathCallback;
            takePhoto();
            return true;
        }
        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            takePhoto();
        }
        // For Android  >= 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            takePhoto();
        }
        //For Android  >= 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            takePhoto();
        }
    }
    public void takePhoto(){
        //弹出提示框
        new CommomDialog(this,new CommomDialog.CameraOpenListener(){

            @Override
            public void onClick(CommomDialog c) {
                checkCameraPermission();
                c.dismiss();
            }
        },new CommomDialog.AblumOpenListener(){

            @Override
            public void onClick(CommomDialog c) {
                showAblum();
                c.dismiss();
            }
        })
         .show();
    }
    /**
     * 调用相机权限控制
     */
    public void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
        }else{
            showCamera();
        }
    }
    /**
     * 调用相机
     */
    public void showCamera(){
        File tempFile = new File(Environment.getExternalStorageDirectory(),"image.jpg");
        imageUri = Uri.fromFile(tempFile);
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(openCameraIntent,REQ_CAMERA);
    }
    /**
     * 相册权限控制
     */
    public void checkAblumPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, ABLUM_PERMISSIONS_REQUEST_CODE);
        }else{
            showAblum();
        }
    }
    /**
     * 调用相册
     */
    public void showAblum(){
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        openAlbumIntent.addCategory(Intent.CATEGORY_OPENABLE);
        openAlbumIntent.setType("image/*");
        startActivityForResult(openAlbumIntent,REQ_CHOOSE);
    }
    /**
     * 权限处理回掉
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //申请成功，可以拍照
                showCamera();
            } else {
                Toast.makeText(MainActivity.this,"你拒绝了权限，该功能不可用\n可在应用设置里授权拍照哦",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if(requestCode == ABLUM_PERMISSIONS_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //申请成功
                showAblum();
            } else {
                Toast.makeText(MainActivity.this,"你拒绝了权限，该功能不可用\n可在应用设置里授权查看相册哦",Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAMERA||requestCode == REQ_CHOOSE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }else if(requestCode == SCAN_CODE){
           String arg = data.getStringExtra("result");
            mWebView.loadUrl("javascript:receptionResult('" +arg + "')");
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }
        return;
    }
}
