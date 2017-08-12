package com.example.yuzelli.carplaterecognition.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yuzelli.carplaterecognition.R;
import com.example.yuzelli.carplaterecognition.base.BaseActivity;
import com.example.yuzelli.carplaterecognition.bean.CarBean;
import com.example.yuzelli.carplaterecognition.constants.ConstantsUtils;
import com.example.yuzelli.carplaterecognition.utils.CommonAdapter;
import com.example.yuzelli.carplaterecognition.utils.OtherUtils;
import com.example.yuzelli.carplaterecognition.utils.SharePreferencesUtil;
import com.example.yuzelli.carplaterecognition.utils.ViewHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class CarListActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.lv_car)
    ListView lvCar;
    @BindView(R.id.tv_ok)
    TextView tvOk;
private ArrayList<CarBean> cars;
    @Override
    protected int layoutInit() {
        return R.layout.activity_car_list;
    }

    @Override
    protected void binEvent() {
        cars = (ArrayList<CarBean>) SharePreferencesUtil.readObject(this, ConstantsUtils.CAR_INFO);
        if(cars==null){
            cars = new ArrayList<>();
        }
        lvCar.setAdapter(new CommonAdapter<CarBean>(this,cars,R.layout.cell_time) {
            @Override
            public void convert(ViewHolder helper, CarBean item, int position) {
                helper.setText(R.id.tv_number,item.getNumber());
                helper.setText(R.id.tv_time, OtherUtils.stampToDate(item.getTime()));
            }
        });

    }

    @Override
    protected void fillData() {

    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, CarListActivity.class);
        context.startActivity(intent);

    }

    @OnClick({R.id.img_back,  R.id.tv_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_ok:

                break;
        }
    }


}
