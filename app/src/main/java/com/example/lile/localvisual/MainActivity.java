package com.example.lile.localvisual;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.lile.localvisual.bean._User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends Activity {
    private MapView mapView;
    private BaiduMap bdMap;

    private LocationClient locationClient;
    private BDLocationListener locationListener;
    private BDNotifyListener notifyListener;

    private double longitude;// 精度
    private double latitude;// 维度
    private float radius;// 定位精度半径，单位是米
    private String addrStr;// 反地理编码
    private String province;// 省份信息
    private String city;// 城市信息
    private String district;// 区县信息
    private float direction;// 手机方向信息
    private int locType;
    private Intent intent;
    private String myUser;

    // 定位按钮
    private Button locateBtn;
    private Button btn_search;//搜索按钮
    // 定位模式 （普通-跟随-罗盘）
    private MyLocationConfiguration.LocationMode currentMode;
    // 定位图标描述
    private BitmapDescriptor currentMarker = null;
    // 记录是否第一次定位
    private boolean isFirstLoc = true;
    //振动器设备
    private Vibrator mVibrator;
    private _User user;

    //侧滑向
    private DrawerLayout drawerLayout;
    private Button btn_returnAll;
    private TextView tv_myUser;

    private  MyOrientationListener myOrientationListener;
    private  float mLastX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity","oncreate");
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.mydrawerlayout);
        Bmob.initialize(this, "cedd190c558644d012167c477e2a68c9"); // 后端云
       // mapView = (MapView) findViewById(R.id.baidumap);
        init();
    }
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_location:// 定位
                    switch (currentMode) {
                        case FOLLOWING:
                            locateBtn.setText("跟随");
                            currentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                            break;
                        case COMPASS:
                            locateBtn.setText("罗盘");
                            currentMode = MyLocationConfiguration.LocationMode.COMPASS;
                            break;
                        case NORMAL:
                            locateBtn.setText("普通");
                            currentMode = MyLocationConfiguration.LocationMode.NORMAL;
                            break;
                    }
                    bdMap.setMyLocationConfigeration(new MyLocationConfiguration(
                            currentMode, true, currentMarker));
                    break;
            }
        }
    };
    void init() {
        intent = getIntent();
        myUser = intent.getStringExtra("user");
        Log.i("MainActivity","获取到信息"+myUser);
        //百度地图
        mapView = (MapView) findViewById(R.id.baidumap);
        bdMap = mapView.getMap();
        bdMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
        locateBtn = (Button) findViewById(R.id.btn_location);
        locateBtn.setOnClickListener(listener);
        //TODO 定位模式
        currentMarker = BitmapDescriptorFactory.fromResource(R.drawable.dingwei);
        currentMode = MyLocationConfiguration.LocationMode.NORMAL;
        bdMap.setMyLocationConfigeration(new MyLocationConfiguration(
                currentMode, true, currentMarker));
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE );
        bdMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(getApplicationContext());
        locationListener = new MainActivity.MyLocationListener();
        locationClient.registerLocationListener(locationListener);
        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        locationClient.setLocOption(locOption);
        notifyListener = new MainActivity.MyNotifyListener();
        notifyListener.SetNotifyLocation(longitude, latitude, 3000, "bd09ll");//精度，维度，范围，坐标类型
        locationClient.registerNotify(notifyListener);
        locationClient.start();

        //搜索按钮
        btn_search=(Button)findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                intent.putExtra("CITY",city);
                startActivity(intent);
            }
        });
        user = BmobUser.getCurrentUser(_User.class);// bmobuser初始化 得到当前登陆user
        String username = (String) BmobUser.getObjectByKey("username");
        Log.i("Main进程","当前登录用户"+username);

        //实现侧滑
        drawerLayout = (DrawerLayout) findViewById(R.id.dlMenu);
        btn_returnAll = (Button)findViewById(R.id.btn_returnAll);
        btn_returnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity","实现退出当前登录/bug");
            }
        });
        tv_myUser = (TextView) findViewById(R.id.tv_myuser);
        tv_myUser.setText("当前用户--》"+myUser);

        initSenson();
    }

    //传感器方法
    void initSenson(){
        Log.i("MainActivity","传感器使用");
        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                //将获取的x轴方向赋值给全局变量
                mLastX = x;
                Log.i("MainActivity","传感器"+mLastX);
            }
        });
    }
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
        // 异步返回的定位结果
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            locType = location.getLocType();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
//            Toast.makeText(MainActivity.this, "当前定位的返回值是："+locType+" "+longitude+" "+latitude, Toast.LENGTH_SHORT).show();
            if (location.hasRadius()) {// 判断是否有定位精度半径
                radius = location.getRadius();
            }
            if (locType == BDLocation.TypeGpsLocation) {//
                Toast.makeText(
                        MainActivity.this,
                        "当前速度是：" + location.getSpeed() + "~~定位使用卫星数量："
                                + location.getSatelliteNumber(),
                        Toast.LENGTH_SHORT).show();
            } else if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
//                Toast.makeText(MainActivity.this, addrStr,
//                        Toast.LENGTH_SHORT).show();
            }
            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
            district = location.getDistrict();// 区县
//            Toast.makeText(MainActivity.this,
//                    province + "~" + city + "~" + district, Toast.LENGTH_SHORT)
//                    .show();
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(mLastX)// 方向 通过传感器的到的
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            bdMap.setMyLocationData(locData);
            LatLng ll = new LatLng(latitude, longitude);
            //添加位置信息
            sendlocToBmob(ll);
            //TODO 添加数据
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);

            bdMap.animateMapStatus(msu);

        }
    }

    /**
     * 位置提醒监听器
     * @author dell
     *
     */
    class MyNotifyListener extends BDNotifyListener {
        @Override
        public void onNotify(BDLocation bdLocation, float distance) {
            super.onNotify(bdLocation, distance);
            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            Toast.makeText(MainActivity.this, "震动提醒", Toast.LENGTH_SHORT).show();
        }
    }

    //发送位置数据到bmob
    private void sendlocToBmob(LatLng latLng){
        user.setLatitude(latLng.latitude);
        user.setLongtitude(latLng.longitude);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e == null){
                    Log.i("sendLoctoBmob","更新成功");
                }else {
                    Log.i("sendLoctoBmob","跟新失败");
                }
            }
        });
        Log.i("sendloctoBmob","-->数据为"+latLng.toString());
    }



    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.unRegisterLocationListener(locationListener);
        //取消位置提醒
        locationClient.removeNotifyEvent(notifyListener);
        locationClient.stop();
    }
    //实现按键两次退出
    //退出时的时间
    private long mExitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出LocalVisual", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {

            finish();
            System.exit(0);
        }
    }

    }