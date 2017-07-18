package cn.com.shengchuang.webviewphoto;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private String path="file:///android_asset/index.html";
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private static final int REQ_CAMERA = FILECHOOSER_RESULTCODE+1;
    private static final int REQ_CHOOSE = REQ_CAMERA+1;
    private static final int SCALE = 5;//照片缩小比例
    private Uri imageUri;
    private ImageView imgview;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = REQ_CHOOSE+1;
    private static final int ABLUM_PERMISSIONS_REQUEST_CODE = CAMERA_PERMISSIONS_REQUEST_CODE+1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgview = (ImageView) findViewById(R.id.imgsrc);
        mWebView = (WebView) findViewById(R.id.web);
        mWebView.loadUrl(path);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
       settings.setBlockNetworkImage(false);

//        /**
//         * 监听 WebView 加载前、中、后的方法
//         */
//        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                // TODO Auto-generated method stub
//                super.onPageStarted(view, url, favicon);
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                // TODO Auto-generated method stub
//                super.onPageFinished(view, url);
//            }
//        });
        /**
         * 不同手机相机相册兼容问题
         */
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                takePhoto();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }
        });

        //js 调用java方法
//        mWebView.addJavascriptInterface(new DemoJavascriptInterface(),"network");

    }

//    /**
//     * js 调用java方法
//     */
//    class DemoJavascriptInterface {
//
//        public DemoJavascriptInterface() {
//        }
//
//        @JavascriptInterface
//        public void openPhtoto() {
//            Toast.makeText(MainActivity.this,"js 调用java方法",Toast.LENGTH_SHORT).show();
//        }
//
//    }

    public void takePhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setNegativeButton("相机拍照", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkCameraPermission();//调用相机
                    }
                })
                .setPositiveButton("相册选择", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkAblumPermission();
                    }
                });
        builder.show();
    }

    /**********************************方法一***********************************/
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
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
        File tempFile = new File(Environment.getExternalStorageDirectory(),"image.jpg");
        imageUri = Uri.fromFile(tempFile);
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
        if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CAMERA:
                    //将保存在本地的图片取出并缩小后显示在界面上
                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+"/image.jpg");
                    Bitmap newBitmap = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
                    //由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                    bitmap.recycle();
                    //将处理过的图片显示在界面上，并保存到本地
                    imgview.setImageBitmap(newBitmap);
                    //mWebView.loadUrl(String.format("javascript:showInfoFromJava("+Environment.getExternalStorageDirectory()+"/image.jpg"+")"));
                    //ImageTools.savePhotoToSDCard(newBitmap, Environment.getExternalStorageDirectory().getAbsolutePath(), String.valueOf(System.currentTimeMillis()));
                    break;
                case REQ_CHOOSE:
                    ContentResolver resolver = getContentResolver();
                    //照片的原始资源地址
                    imageUri = data.getData();
                    try {
                        //使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, imageUri);
                        if (photo != null) {
                            //为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            Bitmap smallBitmap = ImageTools.zoomBitmap(photo, photo.getWidth() / SCALE, photo.getHeight() / SCALE);
                            //释放原始图片占用的内存，防止out of memory异常发生
                            photo.recycle();
                            imgview.setImageBitmap(smallBitmap);
                            //mWebView.loadUrl(String.format("javascript:showInfoFromJava("+smallBitmap+")"));
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
        if (mUploadCallbackAboveL != null) {
            if(imageUri !=null){
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{imageUri});
                mUploadCallbackAboveL= null;
            }
        } else if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(imageUri);
            mUploadMessage = null;
        }
    }

/*******************************方法二****************************************************/

//
//    /**
//     * 打开照相机
//     */
//    private void openCarcme(String imagePaths) {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // 必须确保文件夹路径存在，否则拍照后无法完成回调
//        File vFile = new File(imagePaths);
//        if (!vFile.exists()) {
//            File vDirPath = vFile.getParentFile();
//            vDirPath.mkdirs();
//        } else {
//            if (vFile.exists()) {
//                vFile.delete();
//            }
//        }
//        imageUri = Uri.fromFile(vFile);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intent, REQ_CAMERA);
//    }
//
//    /**
//     * 本地相册选择图片
//     */
//    private void chosePic(String imagePaths) {
//        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
//        String IMAGE_UNSPECIFIED = "image/*";
//        innerIntent.setType(IMAGE_UNSPECIFIED); // 查看类型
//        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
//        startActivityForResult(wrapperIntent, REQ_CHOOSE);
//    }
//
    /**
     * 图片返回
     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//        if (null == mUploadMessage) return;
//        Uri uri = null;
//        if(requestCode == REQ_CAMERA){
////            afterOpenCamera();
////            uri = cameraUri;
//        }else if(requestCode == REQ_CHOOSE){
////            uri = afterChosePic(intent);
//        }
//        mUploadMessage.onReceiveValue(uri);
//        mUploadMessage = null;
//
//    }
//
//    /**
//     * 检查SD卡是否存在
//     * @return
//     */
//    public final boolean checkSDcard() {
//        boolean flag = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//        if (!flag) {
//            Toast.makeText(this, "请插入手机存储卡再使用本功能", Toast.LENGTH_SHORT).show();
//        }
//        return flag;
//    }

    /************************************方法三***************************************************/
//    /**
//     * 拍照或选择相册 返回结果
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FILECHOOSER_RESULTCODE) {
//            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
//            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
//            if (mUploadCallbackAboveL != null) {
//                onActivityResultAboveL(requestCode, resultCode, data);
//            } else if (mUploadMessage != null) {
//                if (result == null) {
//                    mUploadMessage.onReceiveValue(imageUri);
//                    mUploadMessage = null;
//                } else {
//                    mUploadMessage.onReceiveValue(result);
//                    mUploadMessage = null;
//                }
//
//
//            }
//        }
//    }
//
//
//    private Bitmap getDiskBitmap(String pathString)
//    {
//        Bitmap bitmap = null;
//        try
//        {
//            File file = new File(pathString);
//            if(file.exists())
//            {
//                bitmap = BitmapFactory.decodeFile(pathString);
//            }
//        } catch (Exception e)
//        {
//            // TODO: handle exception
//        }
//
//
//        return bitmap;
//    }
//    @SuppressWarnings("null")
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
//        if (requestCode != FILECHOOSER_RESULTCODE || mUploadCallbackAboveL == null) {
//            return;
//        }
//        Uri[] results = null;
//        if (resultCode == Activity.RESULT_OK) {
//            if (data == null) {
//                results = new Uri[]{imageUri};
//            } else {
//                String dataString = data.getDataString();
//                ClipData clipData = data.getClipData();
//
//                if (clipData != null) {
//                    results = new Uri[clipData.getItemCount()];
//                    for (int i = 0; i < clipData.getItemCount(); i++) {
//                        ClipData.Item item = clipData.getItemAt(i);
//                        results[i] = item.getUri();
//                    }
//                }
//
//                if (dataString != null)
//                    results = new Uri[]{Uri.parse(dataString)};
//            }
//
//        }
//        if (results != null) {
//            mUploadCallbackAboveL.onReceiveValue(results);
//            mUploadCallbackAboveL = null;
//        } else {
//            results = new Uri[]{imageUri};
//            mUploadCallbackAboveL.onReceiveValue(results);
//            mUploadCallbackAboveL = null;
//        }
//        return;
//    }
//
//    private void takePhoto() {
//        // 外存sdcard存放路径
//        String FILE_PATH = Environment.getExternalStorageDirectory() +"/" + "BaoWu" +"/";
//        File imageStorageDir = new File(FILE_PATH, "img");
//        // Create the storage directory if it does not exist
//        if (!imageStorageDir.exists()) {
//            imageStorageDir.mkdirs();
//        }
//        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
//        imageUri = Uri.fromFile(file);
//        final List<Intent> cameraIntents = new ArrayList<Intent>();
//        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        final PackageManager packageManager = getPackageManager();
//        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
//        for (ResolveInfo res : listCam) {
//            final String packageName = res.activityInfo.packageName;
//            final Intent i = new Intent(captureIntent);
//            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//            i.setPackage(packageName);
//            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            cameraIntents.add(i);
//        }
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");
//        Intent chooserIntent = Intent.createChooser(i, "选择图片");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
//        MainActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
//    }

}
