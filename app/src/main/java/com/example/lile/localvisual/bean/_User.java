package com.example.lile.localvisual.bean;

import com.baidu.mapapi.model.LatLng;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 用户beans
 * Created by lile on 2017/5/7.
 */

public class _User extends BmobUser {
    private double Latitude;
    private double Longtitude;

    public void setLatitude(double latLng){
        Latitude=latLng;
    }
    public void setLongtitude(double latLng2){
        Longtitude=latLng2;
    }
    public double getLatitude(){
        return Latitude;
    }
    public double getLongtitude(){
        return Longtitude;
    }
}
