package com.example.yuzelli.carplaterecognition.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.example.yuzelli.carplaterecognition.utils.ImageUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.tv_take)
    TextView tvTake;
    @BindView(R.id.rw_head)
    ImageView rw_head;
    /**
     * 定义三种状态
     */
    private static final int HEAD_PORTRAIT_PIC = 1;//相册
    private static final int HEAD_PORTRAIT_CAM = 2;//相机
    private static final int HEAD_PORTRAIT_CUT = 3;//图片裁剪
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_ok)
    TextView tvOk;

    private File photoFile;
    private Bitmap photoBitmap;
    private String photoPath;
    private boolean isSetImgFlag = false;


    @Override
    protected int layoutInit() {
        return R.layout.activity_main;
    }

    @Override
    protected void binEvent() {

    }

    @Override
    protected void fillData() {
          isSetImgFlag = false;
        rw_head.setImageResource(R.drawable.ic_no_img);
    }


    //显示Dialog选择拍照还是从相册选择
    private void showPhotoDialog() {
        final Dialog dialog = new Dialog(this, R.style.PhotoDialog);
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.diallog_personal_head_select, null);
        dialog.setContentView(view);
        TextView tv_PhotoGraph = (TextView) view.findViewById(R.id.tv_personal_photo_graph);
        TextView tv_PhotoAlbum = (TextView) view.findViewById(R.id.tv_personal_photo_album);
        TextView tv_Cancel = (TextView) view.findViewById(R.id.tv_cancel);

        tv_PhotoGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoGraph();
            }
        });

        tv_PhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoAlbum();
            }
        });

        tv_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //设置出现Dialog位置
        Window window = dialog.getWindow();
        // 可以在此设置显示动画
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        dialog.show();
    }

    //打开相册方法
    private void openPhotoAlbum() {
        Intent picIntent = new Intent(Intent.ACTION_PICK, null);
        picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(picIntent, HEAD_PORTRAIT_PIC);
    }

    //打开相机方法
    private void openPhotoGraph() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!file.exists()) {
                file.mkdirs();
            }
            photoFile = new File(file, System.currentTimeMillis() + "");

            Uri photoUri = Uri.fromFile(photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, HEAD_PORTRAIT_CAM);
        } else {

            Toast.makeText(this, "请确认已经插入SD卡", Toast.LENGTH_SHORT).show();
        }
    }

    //回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case HEAD_PORTRAIT_CAM:
                    startPhotoZoom(Uri.fromFile(photoFile));
                    break;
                case HEAD_PORTRAIT_PIC:
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    startPhotoZoom(data.getData());
                    break;
                case HEAD_PORTRAIT_CUT:
                    if (data != null) {
                        photoBitmap = data.getParcelableExtra("data");
                        rw_head.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        rw_head.setImageBitmap(photoBitmap);
                        try {
                            File SDCardRoot = Environment.getExternalStorageDirectory();
                            if (ImageUtils.saveBitmap2file(photoBitmap)) {

                                photoPath = SDCardRoot + ConstantsUtils.AVATAR_FILE_PATH;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
        }
    }

    /**
     * 打开系统图片裁剪功能
     *
     * @param uri
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true); //黑边
        intent.putExtra("scaleUpIfNeeded", true); //黑边
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, HEAD_PORTRAIT_CUT);
    }



    @OnClick({R.id.tv_take, R.id.tv_cancel, R.id.tv_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_take:
                showPhotoDialog();
                break;
            case R.id.tv_cancel:
                isSetImgFlag = false;
                rw_head.setImageResource(R.drawable.ic_no_img);
                break;
            case R.id.tv_ok:
                if (isSetImgFlag){
                    AnalyticPictureActivity.actionStart(MainActivity.this,photoPath);
                }else {
                    showToast("图片未设置！");
                }
                break;
        }
    }
}
