package com.example.lile.localvisual.bean;

import com.baidu.mapapi.model.LatLng;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 用户beans
 * Created by lile on 2017/5/7.
 */

public class Users extends BmobUser {
    private double latLng1;
    private double latLng2;

    public void setLatLng1(double latLng){
        this.latLng1=latLng;
    }
    public void setLatLng2(double latLng2){
        this.latLng2=latLng2;
    }
    public double getLatlng1(){
        return latLng1;
    }
    public double getLatLng2(){
        return latLng2;
    }
}
