package com.example.yuzelli.carplaterecognition.view.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuzelli.carplaterecognition.R;
import com.example.yuzelli.carplaterecognition.base.BaseActivity;
import com.example.yuzelli.carplaterecognition.constants.ConstantsUtils;
import com.example.yuzelli.carplaterecognition.utils.AuthService;
import com.example.yuzelli.carplaterecognition.utils.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.tv_take)
    TextView tvTake;

    /**
     * 定义三种状态
     */
    private static final int HEAD_PORTRAIT_PIC = 1;//相册
    private static final int HEAD_PORTRAIT_CAM = 2;//相机
    private static final int HEAD_PORTRAIT_CUT = 3;//图片裁剪


    private File photoFile;
    private Bitmap photoBitmap;
    private String photoPath;
    private boolean isSetImgFlag = false;
    private Activity context;

    @Override
    protected int layoutInit() {
        return R.layout.activity_main;
    }

    @Override
    protected void binEvent() {
        context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                token = AuthService.getAuth();
            }
        }).start();
    }

    @Override
    protected void fillData() {

    }

    public final int GET_IMAGE_BY_CAMERA_U = 5001;
    Uri photoUri;

    //打开相机方法
    public void openPhotoGraph() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!file.exists()) {
                file.mkdirs();
            }
            photoFile = new File(file, System.currentTimeMillis() + "");

//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                photoUri = FileProvider.getUriForFile(context,"com.example.yuzelli.carplaterecognition.fileprovider", file);//这里进行替换uri的获得方式
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里加入flag
//                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//                startActivityForResult(intent, HEAD_PORTRAIT_CAM);
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

/*
* 这里就是高版本需要注意的，需用使用FileProvider来获取Uri，同时需要注意getUriForFile
* 方法第二个参数要与AndroidManifest.xml中provider的里面的属性authorities的值一致
* */
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                photoUri = FileProvider.getUriForFile(MainActivity.this,
                        "com.example.yuzelli.carplaterecognition.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(intent, GET_IMAGE_BY_CAMERA_U);
            } else {
                photoUri = Uri.fromFile(photoFile);//这里进行替换uri的获得方式
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, HEAD_PORTRAIT_CAM);
            }

        } else {

            Toast.makeText(this, "请确认已经插入SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    //回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000&&resultCode==1001){
            openPhotoGraph();
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case HEAD_PORTRAIT_CAM:
                    //startPhotoZoom(Uri.fromFile(photoFile));

                    if (data != null) { //可能尚未指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        //返回有缩略图
                        if (data.hasExtra("data")) {
                            photoBitmap = data.getParcelableExtra("data");
                            try {
                                File SDCardRoot = Environment.getExternalStorageDirectory();
                                if (ImageUtils.saveBitmap2file(photoBitmap)) {
                                    isSetImgFlag = true;
                                    photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //得到bitmap后的操作
                        }
                    } else {
                        //由于指定了目标uri，存储在目标uri，intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        // 通过目标uri，找到图片
                        // 对图片的缩放处理
                        // 操作
                        // String url = getRealFilePath(MainActivity.this,photoUri);
                        try {
                            photoBitmap = getBitmapFormUri(MainActivity.this, photoUri);
                            File SDCardRoot = Environment.getExternalStorageDirectory();
                            if (ImageUtils.saveBitmap2file(photoBitmap)) {
                                isSetImgFlag = true;
                                photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    AnalyticPictureActivity.actionStart(MainActivity.this, photoPath);
                    break;
                case GET_IMAGE_BY_CAMERA_U:
                    showToast("llllllllllllllllllllllll");
                    if (data != null) { //可能尚未指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        //返回有缩略图
                        if (data.hasExtra("data")) {
                            photoBitmap = data.getParcelableExtra("data");
                            try {
                                File SDCardRoot = Environment.getExternalStorageDirectory();
                                if (ImageUtils.saveBitmap2file(photoBitmap)) {
                                    isSetImgFlag = true;
                                    photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //得到bitmap后的操作
                        }
                    } else {
                        //由于指定了目标uri，存储在目标uri，intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        // 通过目标uri，找到图片
                        // 对图片的缩放处理
                        // 操作
                        // String url = getRealFilePath(MainActivity.this,photoUri);
                        try {
                            photoBitmap = getBitmapFormUri(MainActivity.this, photoUri);
                            File SDCardRoot = Environment.getExternalStorageDirectory();
                            if (ImageUtils.saveBitmap2file(photoBitmap)) {
                                isSetImgFlag = true;
                                photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    AnalyticPictureActivity.actionStart(MainActivity.this, photoPath);


                    // startPhotoZoom(data.getData());
                    break;
//                case HEAD_PORTRAIT_CUT:
//                    if (data != null) {
//                        photoBitmap = data.getParcelableExtra("data");
//                        rw_head.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        rw_head.setImageBitmap(photoBitmap);
//                        try {
//                            File SDCardRoot = Environment.getExternalStorageDirectory();
//                            if (ImageUtils.saveBitmap2file(photoBitmap)) {
//                                isSetImgFlag = true;
//                                photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }

//                    break;
            }
        }
    }

    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    private final int NEED_CAMERA = 200;

    @OnClick({R.id.tv_take})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_take:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    context.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, NEED_CAMERA);
                } else {
                    openPhotoGraph();
                }

                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case NEED_CAMERA:
                // 如果权限被拒绝，grantResults 为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openPhotoGraph();
                } else {
                    Toast.makeText(context, "改功能需要相机和读写文件权限", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}
