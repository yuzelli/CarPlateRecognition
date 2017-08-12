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
                helper.setText(R.id.tv_color,item.getColor());
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
                craetExcel();
                break;
        }
    }


        private void craetExcel() {
            try {
                File f = new File("mnt/sdcard/myCarList.xls");
                if (f.exists()){
                    f.delete();
                }
                // 打开文件
                WritableWorkbook book = Workbook.createWorkbook(new File("mnt/sdcard/myCarList.xls"));
                // 生成名为“第一张工作表”的工作表，参数0表示这是第一页
                WritableSheet sheet = book.createSheet("第一张工作表", 0);
                // 在Label对象的构造子中指名单元格位置是第一列第一行(0,0)
                // 以及单元格内容为baby
                Label label = new Label(0, 0, "序号");
                Label labe2 = new Label(1, 0, "时间");
                Label labe3 = new Label(2, 0, "车牌号");
                Label labe4 = new Label(3, 0, "车车牌颜色");
                // 将定义好的单元格添加到工作表中
                sheet.addCell(label);
                sheet.addCell(labe2);
                sheet.addCell(labe3);
                sheet.addCell(labe4);
                // 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。
                //单元格位置是第二列，第一行，值为123
//                jxl.write.Number number = new jxl.write.Number(1, 0, 123);
//                sheet.addCell(number);
                for (int  i = 0 ; i < cars.size();i++){
                    Label ll = new Label(0, i+1, (i+1)+"");
                    Label l2 = new Label(1, i+1, OtherUtils.stampToDate(cars.get(i).getTime()));
                    Label l3 = new Label(2, i+1, cars.get(i).getNumber());
                    Label l4 = new Label(3, i+1, cars.get(i).getColor());
                    sheet.addCell(ll);
                    sheet.addCell(l2);
                    sheet.addCell(l3);
                    sheet.addCell(l4);

                }
                showToast("文件已导出，文件名为：myCarList，请前往sd卡中查看");
                //写入数据并关闭
                book.write();
                book.close();

            } catch (WriteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}
