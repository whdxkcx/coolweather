package com.coolweather.app.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.coolweather.R;
import com.coolweather.app.coolweather.db.CoolWeatherDB;
import com.coolweather.app.coolweather.model.City;
import com.coolweather.app.coolweather.model.County;
import com.coolweather.app.coolweather.model.Province;
import com.coolweather.app.coolweather.util.HttpCallBackListener;
import com.coolweather.app.coolweather.util.HttpUtil;
import com.coolweather.app.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2017/8/24.
 */

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB mCoolWeatherDB;
    private List<String> dataList=new ArrayList<String>();


    //省列表
    private List<Province> mProvinceList;

    //市列表
    private  List<City> mCityList;

    //县列表
    private List<County> mCountyList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //当前选中的级别

    private int currentLevel;

    //是否从WeatherActivity中跳转过来
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)&& !isFromWeatherActivity){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView =(ListView) findViewById(R.id.list_view);
        titleText =(TextView) findViewById(R.id.title_text);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        mCoolWeatherDB=CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                if(currentLevel==LEVEL_PROVINCE) {
                    System.out.println("LEVEL_PROVINCE");
                    selectedProvince = mProvinceList.get(index);
                    queryCities();//获取当前省份的所有市
                }else if (currentLevel==LEVEL_CITY){
                    System.out.println("LEVEL_CITY");
                    selectedCity=mCityList.get(index);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String countyCode=mCountyList.get(index).getCountyCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();//加载省级数据
    }


    //查询全国所有的省，优先从数据库中查询，如果没有查询到再到服务器上去查询。
    private void queryProvinces(){
        System.out.println("queryProvinces()");
        //从数据库中读取全国省份信息
        mProvinceList =mCoolWeatherDB.loadProvinces();
        if(mProvinceList.size()>0){
            dataList.clear();//清理dataList列表
            //把所有省份的名称存入dataList列表
            for(Province province:  mProvinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();//实时更新列表
            listView.setSelection(0);//将光标移到列表的第一个元素
            titleText.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }


    //查询选中的省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCities(){
        System.out.println("queryCities()");
        mCityList=mCoolWeatherDB.loadCities(selectedProvince.getId());
        if(mCityList.size()>0){
            dataList.clear();//清理dataList列表
            //读取该省份所有的市的名称，并存到dataList列表
            for(City city:mCityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//设置列表的数据实时更新
            listView.setSelection(0);//跳到列表的第一个元素
            titleText.setText(selectedProvince.getProvinceName());//设置标题为当前省份
            currentLevel=LEVEL_CITY;//把当前级别设置成市级
        }else {//从数据库中没查到，就从服务器中查

            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }


    //查询所有选中市所有的县，优先从数据库查询，如果没有查询到再到服务器上去查询
    private void queryCounties(){
        System.out.println("queryCounties()");
        mCountyList=mCoolWeatherDB.loadConties(selectedCity.getId());
        if(mCountyList.size()>0){
            dataList.clear();
            for(County county:mCountyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel=LEVEL_COUNTY;
        }else {//从数据库中没查到，就从服务器中查

            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }





    //根据传入的代号和类型从服务器上查询省市县的数据。
    private void queryFromServer(final String code,final String type){
        System.out.println(" queryFromServer(final String code,final String type)");
        String address;
        if(!TextUtils.isEmpty(code)){//如果code不为空，则根据上级的code查询市级或者县级的信息
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address="http://www.weather.com.cn/data/list3/city.xml";
            //address="http://www.baidu.com";
        }
        System.out.println(address);
        showProgeressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                System.out.println("finish");
                boolean result=false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(mCoolWeatherDB,response);
                }else if("city".equals(type)){
                    System.out.println("\"city\".equals(type)");
                    result =Utility.handleCitiesReponse(mCoolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleContiesReponse(mCoolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                System.out.println("error");
                //通过runOnUiThread方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //显示进度对话框
    private void showProgeressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog(){
        if(progressDialog!=null){
         progressDialog.dismiss();
        }
    }


    //捕获back键，根据当前的级别来判断，此时应返回省、市列表还是直接退出

    @Override
    public void onBackPressed() {
        if(currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }else{
            if(isFromWeatherActivity){
                Intent intent=new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
