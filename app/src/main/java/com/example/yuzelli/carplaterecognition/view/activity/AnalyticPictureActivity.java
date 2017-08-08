package com.example.yuzelli.carplaterecognition.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yuzelli.carplaterecognition.R;
import com.example.yuzelli.carplaterecognition.base.BaseActivity;

public class AnalyticPictureActivity extends BaseActivity {

    @Override
    protected int layoutInit() {
        return R.layout.activity_analytic_picture;
    }

    @Override
    protected void binEvent() {

    }

    @Override
    protected void fillData() {

    }

    public static void actionStart(Context context,String path){
        Intent intent = new Intent(context,AnalyticPictureActivity.class);
        intent.putExtra("path",path);
        context.startActivity(intent);
    }
}
