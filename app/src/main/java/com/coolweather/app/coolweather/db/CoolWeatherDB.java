package com.coolweather.app.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolweather.app.coolweather.model.City;
import com.coolweather.app.coolweather.model.County;
import com.coolweather.app.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 2017/8/24.
 */

public class CoolWeatherDB {
    //数据库名
    public static final String DB_NAME="cool_weather";

    //数据库版本
    public static final int VERSION=1;

    //定义一个静态的当前类引用
    private static CoolWeatherDB coolWeatherDB;

    //定义一个SQLiteDatabase类的引用
    private SQLiteDatabase db;

    //将构造方法私有化,则不可继承
    private CoolWeatherDB(Context context){
        //获取一个SQLiteOpenHelper的派生类
        CoolWeatherOpenHelper dbHelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        //调用该派生类父类的方法创建数据库
        db=dbHelper.getWritableDatabase();
    }

    //获取CoolWeatherDB的实例
   public synchronized  static CoolWeatherDB getInstance(Context context){
       if(coolWeatherDB==null){
           coolWeatherDB=new CoolWeatherDB(context);
       }
       return coolWeatherDB;
   }

   //将province实例存储到数据库

    public  void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

     //从数据库读取全国的省份信息
        public List<Province> loadProvinces(){
            List<Province> list=new ArrayList<Province>();
            Cursor cursor=db.query("Province",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do {
                    Province province=new Province();
                    province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                    province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                    list.add(province);
                }while (cursor.moveToNext());
                }
            return list;
            }

    //将City实例存储到数据库
    public void saveCity(City city){
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            db.insert("City", null, values);
        }
    }

    //从数据库读取某省下所有城市的信息。

    public  List<City>  loadCities(int provinceId){
        List<City> list=new ArrayList<City>();
        Cursor cursor=db.query("City",null,"province_id=?",new String[] {String.valueOf(provinceId)},null,null,null);
        if(cursor.moveToFirst()){
            do {
                City city=new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while (cursor.moveToNext());
        }
        return list;
    }


    //将county实例存储到数据库
    public void saveCounty(County county){
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            db.insert("County", null, values);
        }
    }

    //从数据库读取某城市下所有的县信息。
    public  List<County>  loadConties(int cityId){
        List<County> list=new ArrayList<County>();
        Cursor cursor=db.query("County",null,"city_id=?",new String[] {String.valueOf(cityId)},null,null,null);
        if(cursor.moveToFirst()){
            do {
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        return list;
    }


}









