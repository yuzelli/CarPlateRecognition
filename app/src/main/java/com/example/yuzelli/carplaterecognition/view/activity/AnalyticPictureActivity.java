package com.example.yuzelli.carplaterecognition.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yuzelli.carplaterecognition.R;
import com.example.yuzelli.carplaterecognition.base.BaseActivity;
import com.example.yuzelli.carplaterecognition.bean.CarBean;
import com.example.yuzelli.carplaterecognition.constants.ConstantsUtils;
import com.example.yuzelli.carplaterecognition.https.OkHttpClientManager;
import com.example.yuzelli.carplaterecognition.utils.AuthService;
import com.example.yuzelli.carplaterecognition.utils.OtherUtils;
import com.example.yuzelli.carplaterecognition.utils.SharePreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import okhttp3.Request;

public class AnalyticPictureActivity extends BaseActivity {
    private static String URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=";

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.et_numner)
    EditText etNumner;

    @BindView(R.id.tv_ok)
    TextView tvOk;
    private AnalyticPictureActivityHandler handler;
    private String filePath;
    private  Bitmap bitmap;
    private Context context;


    @Override
    protected int layoutInit() {
        return R.layout.activity_analytic_picture;
    }

    @Override
    protected void binEvent() {
        handler = new AnalyticPictureActivityHandler();
        Intent intent = getIntent();
        filePath = intent.getStringExtra("path");
        bitmap = BitmapFactory.decodeFile(filePath, getBitmapOption(1)); //将图片的长和宽缩小味原来的1/2
        imageView.setImageBitmap(bitmap);
        context = this;

                doGetImageInfo(bitmap);



    }

    private void doGetImageInfo(Bitmap bm) {


        String url = URL + token;
        final Map<String, String> map = new HashMap<>();
        map.put("image", removeN(bitmapToBase64(bm)));
        map.put("language_type", "CHN_ENG");
        OkHttpClientManager.postAsync(url, map, new OkHttpClientManager.DataCallBack() {
            @Override
            public void requestFailure(Request request, IOException e) {
                showToast(request.body().toString());
            }

            @Override
            public void requestSuccess(String result) throws Exception {
                Message msg = new Message();
                msg.what = 1001;
                msg.obj = result;
                handler.handleMessage(msg);
            }
        });
    }

    private BitmapFactory.Options getBitmapOption(int inSampleSize)

    {
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    @Override
    protected void fillData() {

    }

    private String removeN(String result) {

        return result.replaceAll("\n", "");
    }

    public static void actionStart(Activity context, String path) {
        Intent intent = new Intent(context, AnalyticPictureActivity.class);
        intent.putExtra("path", path);
        context.startActivityForResult(intent,1000);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @OnClick({R.id.img_back, R.id.tv_ok,R.id.tv_congpa})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_ok:

                 ArrayList<CarBean> carLists  = (ArrayList<CarBean>) SharePreferencesUtil.readObject(context, ConstantsUtils.CAR_INFO);
                if (carLists ==null){
                    carLists = new ArrayList<>();
                }
                for (CarBean c :carLists){
                    if (c.getNumber().equals(etNumner.getText().toString().trim())){
                        showToast("该字符已经保存到excel中，请重新识别其他图片！");
                        return;
                    }
                }
                CarBean car = new CarBean();
                car.setNumber(etNumner.getText().toString().trim());
                car.setTime(System.currentTimeMillis()+"");
                carLists.add(car);
                SharePreferencesUtil.saveObject(context,ConstantsUtils.CAR_INFO,carLists);
                craetExcel();
                Intent intent = new Intent();
                setResult(1001, intent);
                finish();

                break;
            case R.id.tv_congpa:
                Intent intent2 = new Intent();
                setResult(1001, intent2);
                finish();

                break;
        }
    }

    private void congpa() {
    }


    class AnalyticPictureActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    String msgResult = (String) msg.obj;
                    updataView(msgResult);
                    break;
                case 1002:
                    doGetImageInfo(bitmap);
                    break;
            }
        }
    }

    private void updataView(String msgResult) {
        try {
            JSONObject json = new JSONObject(msgResult);
            if (json.isNull("error_code")) {
                JSONArray words_result = json.optJSONArray("words_result");
                StringBuffer buffer = new StringBuffer();
                for (int i = 0 ; i < words_result.length();i++){
                    JSONObject jsonObject = (JSONObject) words_result.get(i);
                    buffer.append(jsonObject.optString("words"));
                }

                String  number = buffer.toString();
                if (number.length()>8){
                    number = number.substring(0,8);
                }
                etNumner.setText(number);
            }else {
                showToast("图片识别失败！");
                finish();
             //  showToast(json.optString(""));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        token = AuthService.getAuth();
//                        handler.sendEmptyMessage(1002);
//                    }
//                }).start();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void craetExcel() {

        ArrayList<CarBean>   cars = (ArrayList<CarBean>) SharePreferencesUtil.readObject(this, ConstantsUtils.CAR_INFO);
        if(cars==null){
            cars = new ArrayList<>();
        }
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

            // 将定义好的单元格添加到工作表中
            sheet.addCell(label);
            sheet.addCell(labe2);
            sheet.addCell(labe3);

            // 生成一个保存数字的单元格，必须使用Number的完整包路径，否则有语法歧义。
            //单元格位置是第二列，第一行，值为123
//                jxl.write.Number number = new jxl.write.Number(1, 0, 123);
//                sheet.addCell(number);
            for (int  i = 0 ; i < cars.size();i++){
                Label ll = new Label(0, i+1, (i+1)+"");
                Label l2 = new Label(1, i+1, OtherUtils.stampToDate(cars.get(i).getTime()));
                Label l3 = new Label(2, i+1, cars.get(i).getNumber());

                sheet.addCell(ll);
                sheet.addCell(l2);
                sheet.addCell(l3);

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
