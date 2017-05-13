package com.example.lile.localvisual;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.example.lile.localvisual.bean._User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

/**
 * Created by lile on 2017/4/23.
 * 用于呈现当前的搜索结果的一个页面
 */

public class SearchResultActivity extends Activity {
    private Button btn_refreshresult;
    private TextView tv_resutltext;
    private ImageView iv_back;
    private BaiduMap baiduMap;
    private MapView mapView;
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
    private Button locateBtn;
    private Button btn_go;
    private Button btn_switch;
    private MyLocationConfiguration.LocationMode currentMode;
    // 定位图标描述
    private BitmapDescriptor currentMarker = null;
    private boolean isFirstLoc = true;
    private Vibrator mVibrator; //震动
    private String str_search;
    private Intent intent;
    //传感器
    private  MyOrientationListener myOrientationListener;
    private  float mLastX;
    //检索信息
    private PoiSearch poiSearch;
    private SuggestionSearch suggestionSearch;
    private ShareUrlSearch shareUrlSearch;
    private LatLng myLatLng;// 目的点
    private LatLng fromlatLng;
    private static final String TAG="SearchResultActivit";
    private String  address;

    private Button btn_returnAll;
    private TextView tv_user;
    private TextView tv_locinfo;
    private TextView tv_to;//地图上点击的地方

    private _User user;//当前用户
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("SearchResultActivit","Oncreate开始");
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.mydrawerlayout2);
        Bmob.initialize(this, "cedd190c558644d012167c477e2a68c9"); // 后端云
        Log.i("SearchResultActivit","地图initalize");
        init();
//        citySearch();
//        nearbySearch();
    }

    void init(){
        initview();
//        Log.i("SearchResultActivit","viw初始化");
        //侧滑
        tv_user = (TextView) findViewById(R.id.tv_myuser2);
        btn_returnAll= (Button) findViewById(R.id.btn_returnAll2);
        user = BmobUser.getCurrentUser(_User.class);
        tv_user.setText("当前用户为：-》"+user.getUsername());
        btn_returnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchResultActivity.this,Login.class);
                startActivity(intent);
                SearchResultActivity.this.finish();
            }
        });
        tv_resutltext.setText(str_search+"的搜索结果是：");
        btn_refreshresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 刷新搜索结果
                refresh();
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                myLatLng = latLng;
                baiduMap.clear();
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(latLng)
                        .icon(bitmap);
                baiduMap.addOverlay(option);
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
                       address = arg0.getAddress();
                        System.out.println("address="+address);
                    }

                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult arg0) {
                    }
                });

                btn_go.setVisibility(View.VISIBLE);
                tv_to.setVisibility(View.VISIBLE);
                tv_to.setText("该点位置是；"+address);
            }
            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                baiduMap.clear();
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(mapPoi.getPosition())
                        .icon(bitmap);
                baiduMap.addOverlay(option);
                Log.e("SearchResultActivit", "点击到地图上的POI物体了！名称：" + mapPoi.getName() + ",Uid:" + mapPoi.getUid());
                return true;
            }
        });
        poi();
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
        myOrientationListener.start();
    }

    void initview(){
        //百度地图
        mapView = (MapView) findViewById(R.id.baidu_map2);
        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(19));
        tv_locinfo = (TextView) findViewById(R.id.tv_locinfo2);
        tv_to = (TextView) findViewById(R.id.tv_to2);
        tv_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_to.setVisibility(View.GONE);
            }
        });
        loc();
        locateBtn = (Button) findViewById(R.id.btn_location2);
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLoc=true;
            }
        });
        btn_refreshresult = (Button)findViewById(R.id.btn_refresh);
        btn_go = (Button) findViewById(R.id.btn_to);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                Log.i("SearchResultActivit","点击了去那里");
                walkroute(myLatLng);
                Log.i(TAG,"设置不可见");
                btn_switch.setVisibility(View.VISIBLE);
                btn_go.setVisibility(View.GONE);
                tv_to.setVisibility(View.GONE);
            }
        });
        btn_switch = (Button)findViewById(R.id.btn_switch);
        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击了切换"+"----"+btn_switch.getText().toString().trim());

                if(btn_switch.getText().toString().equals("驾车切换")){
                    Log.i(TAG,"切换到步行");
                    carroute(myLatLng);
                    btn_switch.setText("步行切换");
                }else if(btn_switch.getText().toString().equals("步行切换")){
                    walkroute(myLatLng);
                    Log.i(TAG,"切换到驾车");
                    btn_switch.setText("驾车切换");
                }
            }
        });
        tv_resutltext = (TextView) findViewById(R.id.tv_resulttext);
        intent = this.getIntent();
        str_search= intent.getStringExtra("searchInfo");
        iv_back = (ImageView)findViewById(R.id.iv_back2);

        //检索组件初始化
        poiSearch = PoiSearch.newInstance();
        suggestionSearch = SuggestionSearch.newInstance();
        shareUrlSearch = ShareUrlSearch.newInstance();


    }
    //定位
    void loc(){
        currentMode = MyLocationConfiguration.LocationMode.COMPASS;
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                currentMode, true, currentMarker));
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE );
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(getApplicationContext());
        locationListener = new SearchResultActivity.MyLocationListener();
        locationClient.registerLocationListener(locationListener);
        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        locationClient.setLocOption(locOption);
        notifyListener = new SearchResultActivity.MyNotifyListener();
        notifyListener.SetNotifyLocation(longitude, latitude, 3000, "bd09ll");//精度，维度，范围，坐标类型
        locationClient.registerNotify(notifyListener);
        locationClient.start();
    }
    private void refresh(){
        finish();
        intent.setClass(SearchResultActivity.this,SearchResultActivity.class);
        Bundle bl=new Bundle();
        intent.putExtra("searchInfo",str_search);
        startActivity(intent);
    }

    //搜索
    void  poi(){
        //POI检索的监听对象
        OnGetPoiSearchResultListener resultListener = new OnGetPoiSearchResultListener() {
            //获得POI的检索结果，一般检索数据都是在这里获取
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                //如果搜索到的结果不为空，并且没有错误
                if (poiResult != null && poiResult.error == PoiResult.ERRORNO.NO_ERROR) {
                    MyOverLay overlay = new MyOverLay(baiduMap, poiSearch);//这传入search对象，因为一般搜索到后，点击时方便发出详细搜索
                    //设置数据,这里只需要一步，
                    overlay.setData(poiResult);
                    //添加到地图
                    overlay.addToMap();
                    //将显示视图拉倒正好可以看到所有POI兴趣点的缩放等级
                    overlay.zoomToSpan();//计算工具
                    //设置标记物的点击监听事件
                    baiduMap.setOnMarkerClickListener(overlay);
                } else {
                    Toast.makeText(getApplication(), "搜索不到你需要的信息！", Toast.LENGTH_SHORT).show();
                }
            }
            //获得POI的详细检索结果，如果发起的是详细检索，这个方法会得到回调(需要uid)
            //详细检索一般用于单个地点的搜索，比如搜索一大堆信息后，选择其中一个地点再使用详细检索
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getApplication(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                } else {// 正常返回结果的时候，此处可以获得很多相关信息
                    Toast.makeText(getApplication(), poiDetailResult.getName() + ": "
                                    + poiDetailResult.getAddress(),
                            Toast.LENGTH_LONG).show();
                }
            }
            //获得POI室内检索结果
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            }
        };
        poiSearch.setOnGetPoiSearchResultListener(resultListener);
        Log.i("SearchResultActivit","poisearch设置lisnter");
        //发起检索

        PoiCitySearchOption poiCity = new PoiCitySearchOption();

        city=city==null? "西安":city;
        Log.i("SearchResultActivit",str_search+"-->"+city);
        poiCity.keyword(str_search).city(city);//这里还能设置显示的个数，默认显示10个
        Log.i("SearchResultActivit","poi设置值");
        poiSearch.searchInCity(poiCity);

    }
    public class MyOverLay extends PoiOverlay {
        /**
         * 构造函数
         */
        PoiSearch poiSearch;

        public MyOverLay(BaiduMap baiduMap, PoiSearch poiSearch) {
            super(baiduMap);
            this.poiSearch = poiSearch;
        }

        /**
         * 覆盖物被点击时
         */
        @Override
        public boolean onPoiClick(int i) {
            //获取点击的标记物的数据
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(i);
            Log.e("TAG", poiInfo.name + "   " + poiInfo.address + "   " + poiInfo.phoneNum);
            //  发起一个详细检索,要使用uid
            btn_go.setVisibility(View.VISIBLE);
            poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(poiInfo.uid));
            return true;
        }
    }

    //步行路径规划
    void walkroute(LatLng latLng){
        RoutePlanSearch routePlanSearch = RoutePlanSearch.newInstance();//路线规划对象
        //给路线规划添加监听
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            //步行路线结果回调
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                baiduMap.clear();
                if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    WalkingRouteOverlay walkingOverlay = new WalkingRouteOverlay(baiduMap);
                    walkingOverlay.setData(walkingRouteResult.getRouteLines().get(0));// 设置一条路线方案
                    walkingOverlay.addToMap();
                    walkingOverlay.zoomToSpan();
                    baiduMap.setOnMarkerClickListener(walkingOverlay);
                    Log.i(TAG, walkingOverlay.getOverlayOptions() + "");

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
                baiduMap.clear();//清除图标或路线
                if (drivingRouteResult == null
                        || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getBaseContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            baiduMap);
                    drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(1));// 设置一条驾车路线方案
                    baiduMap.setOnMarkerClickListener(drivingRouteOverlay);
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
//        PlanNode fromeNode = PlanNode.withCityNameAndPlaceName(city, key);
        PlanNode fromNode = PlanNode.withLocation(fromlatLng);
        PlanNode toNode = PlanNode.withLocation(latLng);
        walkingSearch.from(fromNode).to(toNode);
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
                baiduMap.clear();
                if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    WalkingRouteOverlay walkingOverlay = new WalkingRouteOverlay(baiduMap);
                    walkingOverlay.setData(walkingRouteResult.getRouteLines().get(0));// 设置一条路线方案
                    walkingOverlay.addToMap();
                    walkingOverlay.zoomToSpan();
                    baiduMap.setOnMarkerClickListener(walkingOverlay);
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
                baiduMap.clear();//清除图标或路线
                if (drivingRouteResult == null
                        || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(getBaseContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    MyDrivingRouteOverlay drivingRouteOverlay = new MyDrivingRouteOverlay(  //使用自定义的overlay
                            baiduMap);
                    baiduMap.setOnMarkerClickListener(drivingRouteOverlay);
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
        PlanNode fromeNode = PlanNode.withCityNameAndPlaceName(city, key);
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

            } else if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
            }
            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
//            Log.i("SearchResultActivit","city-->"+city);
            district = location.getDistrict();// 区县
            tv_locinfo.setText("您当前所在的城市是："+province+"省"+city+"市"+district+"区"
                    +location.getStreet()+"街道");
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(mLastX)// 方向
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);
            fromlatLng = new LatLng(latitude, longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(fromlatLng);
            baiduMap.animateMapStatus(msu);

        }
    }

    class MyNotifyListener extends BDNotifyListener {
        @Override
        public void onNotify(BDLocation bdLocation, float distance) {
            super.onNotify(bdLocation, distance);
            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
        }
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
}
