package com.example.lile.localvisual;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
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
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
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
    private Button btn_go;
    private Button btn_switch;
    private LatLng latLngfrom;//开始的
    private LatLng latlngto;

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
    private TextView tv_locinfo;
    //地图点击
    private TextView tv_to;
    private String AddrStr;
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

    void init() {
        intent = getIntent();
        myUser = intent.getStringExtra("user");
        Log.i("MainActivity","获取到信息"+myUser);
        //百度地图
        mapView = (MapView) findViewById(R.id.baidumap);
        bdMap = mapView.getMap();
        bdMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
        locateBtn = (Button) findViewById(R.id.btn_location);
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLoc = true;
            }
        });
        tv_to = (TextView) findViewById(R.id.tv_to);
        tv_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_to.setVisibility(View.GONE);
            }
        });
        tv_locinfo =(TextView) findViewById(R.id.tv_locinfo);
        //TODO 定位模式
        currentMarker = BitmapDescriptorFactory.fromResource(R.drawable.dingwei);
        currentMode = MyLocationConfiguration.LocationMode.COMPASS;
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
                Intent intent2=new Intent(MainActivity.this,Login.class);
                startActivity(intent2);
                MainActivity.this.finish();
            }
        });
        tv_myUser = (TextView) findViewById(R.id.tv_myuser);
        tv_myUser.setText("当前用户--》"+myUser);
        initSenson();
        bdMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(latLng)
                        .icon(bitmap);
                bdMap.clear();
                bdMap.addOverlay(option);
                //实例化一个地理编码查询对象
                GeoCoder geoCoder = GeoCoder.newInstance();
                //设置反地理编码位置坐标
                ReverseGeoCodeOption op = new ReverseGeoCodeOption();
                op.location(latLng);
                //发起反地理编码请求(经纬度->地址信息)
                geoCoder.reverseGeoCode(op);
                geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                        //获取点击的坐标地址
                        addrStr = arg0.getAddress();
                        System.out.println("address="+addrStr);
                    }

                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult arg0) {
                    }
                });
                btn_go.setVisibility(View.VISIBLE);
                latlngto = latLng;
                tv_to.setVisibility(View.VISIBLE);
                tv_to.setText("该点的位置信息为："+addrStr);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                bdMap.clear();
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(mapPoi.getPosition())
                        .icon(bitmap);
                bdMap.addOverlay(option);
                Log.e("SearchResultActivit", "点击到地图上的POI物体了！名称：" + mapPoi.getName() + ",Uid:" + mapPoi.getUid());
                return true;
            }
        });
        btn_go = (Button)findViewById(R.id.btn_to2);//去那里的按钮
        btn_switch =(Button) findViewById(R.id.btn_switch2);//切换方式
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkroute(latlngto);
                btn_switch.setVisibility(View.VISIBLE);
                btn_go.setVisibility(View.GONE);
                tv_to.setVisibility(View.GONE);
            }
        });
        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_switch.getText().toString().equals("驾车切换")){
                    carroute(latlngto);
                    btn_switch.setText("步行切换");
                    Log.i("MainActivity","点击使用步行");
                }else{
                    walkroute(latlngto);
                    Log.i("MainActivity","点击使用驾车");
                    btn_switch.setText("驾车切换");
                }
            }
        });
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
        myOrientationListener.start();
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
            tv_locinfo.setText("您当前所在的城市是："+province+"省"+city+"市"+district+"区"
                +location.getStreet()+"街道");
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(mLastX)// 方向 通过传感器的到的
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            bdMap.setMyLocationData(locData);
            latLngfrom = new LatLng(latitude, longitude);
            //添加位置信息
            sendlocToBmob(latLngfrom);
            //TODO 添加数据
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLngfrom);

            bdMap.animateMapStatus(msu);

        }
    }

    void walkroute(LatLng latLng){
        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();//路线规划对象
        //给路线规划添加监听
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            //步行路线结果回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                bdMap.clear();
                if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    WalkingRouteOverlay walkingOverlay = new WalkingRouteOverlay(bdMap);
                    walkingOverlay.setData(walkingRouteResult.getRouteLines().get(0));// 设置一条路线方案
                    walkingOverlay.addToMap();
                    walkingOverlay.zoomToSpan();
                    bdMap.setOnMarkerClickListener(walkingOverlay);

                } else {
                    Toast.makeText(getBaseContext(), "搜不到！", Toast.LENGTH_SHORT).show();
                }
            }
            //换乘线结果回调
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }
            //跨城公共交通路线结果回调
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }
            //驾车路线结果回调
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                bdMap.clear();//清除图标或路线
                if (drivingRouteResult == null
                        || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getBaseContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            bdMap);
                    drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(1));// 设置一条驾车路线方案
                    bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    int totalLine = drivingRouteResult.getRouteLines().size();
                    Toast.makeText(getBaseContext(),
                            "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_LONG).show();

                    // 通过getTaxiInfo()可以得到很多关于驾车的信息
                }
            }
            //室内路线规划回调
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }
            // 骑行路线结果回调
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });

        //定义Maker坐标点,深圳大学经度和纬度113.943062,22.54069
        //设置的时候经纬度是反的 纬度在前，经度在后
//        LatLng point = new LatLng(22.54069, 113.943062);
        //获得关键字
        String key = "西安电子科技大学南校区";
        //创建步行路线搜索对象
        WalkingRoutePlanOption walkingSearch = new WalkingRoutePlanOption();
        //设置节点对象，可以通过城市+关键字或者使用经纬度对象来设置
        PlanNode fromeNode = PlanNode.withLocation(latLngfrom);
        PlanNode toNode = PlanNode.withLocation(latLng);
        walkingSearch.from(fromeNode).to(toNode);
        routePlanSearch.walkingSearch(walkingSearch);//发起路线检索
    }
    //驾驶路径规划
    void carroute(LatLng latLng){
        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();//路线规划对象
        //给路线规划添加监听
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            //步行路线结果回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                bdMap.clear();
                if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    WalkingRouteOverlay walkingOverlay = new WalkingRouteOverlay(bdMap);
                    walkingOverlay.setData(walkingRouteResult.getRouteLines().get(0));// 设置一条路线方案
                    walkingOverlay.addToMap();
                    walkingOverlay.zoomToSpan();
                    bdMap.setOnMarkerClickListener(walkingOverlay);
                    Log.e("TAG", walkingOverlay.getOverlayOptions() + "");

                } else {
                    Toast.makeText(getBaseContext(), "搜不到！", Toast.LENGTH_SHORT).show();
                }
            }
            //换乘线结果回调
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }
            //跨城公共交通路线结果回调
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }
            //驾车路线结果回调
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                bdMap.clear();//清除图标或路线
                if (drivingRouteResult == null
                        || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getBaseContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    MainActivity.MyDrivingRouteOverlay drivingRouteOverlay = new MainActivity.MyDrivingRouteOverlay(  //使用自定义的overlay
                            bdMap);
                    bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                    drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));// 设置一条驾车路线方案
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    int totalLine = drivingRouteResult.getRouteLines().size();
                    Toast.makeText(getBaseContext(),
                            "共查询出" + totalLine + "条符合条件的线路", Toast.LENGTH_LONG).show();

                    // 通过getTaxiInfo()可以得到很多关于驾车的信息
                }
            }
            //室内路线规划回调
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }
            // 骑行路线结果回调
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });

        //定义Maker坐标点,深圳大学经度和纬度113.943062,22.54069
        //设置的时候经纬度是反的 纬度在前，经度在后
        LatLng point = new LatLng(22.54069, 113.943062);
        //获得关键字
        String key = "西安电子科技大学南校区";
        //创建驾车路线搜索对象
        DrivingRoutePlanOption drivingOptions = new DrivingRoutePlanOption();
        //设置节点对象，可以通过城市+关键字或者使用经纬度对象来设置
        PlanNode fromeNode = PlanNode.withLocation(latLngfrom);
        PlanNode toNode = PlanNode.withLocation(latLng);
        drivingOptions.from(fromeNode).to(toNode);
        drivingOptions.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_AVOID_JAM);//设置驾车策略，避免拥堵
        routePlanSearch.drivingSearch(drivingOptions);//发起驾车检索
    }
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public int getLineColor() {
            //红色的路径
            return Color.RED;
        }
        @Override
        public BitmapDescriptor getStartMarker() {
            //自定义的起点图标
            return BitmapDescriptorFactory.fromResource(R.drawable.begin);
        }
        @Override
        public BitmapDescriptor getTerminalMarker() {
            //自定义的终点图标
            return BitmapDescriptorFactory.fromResource(R.drawable.end);
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