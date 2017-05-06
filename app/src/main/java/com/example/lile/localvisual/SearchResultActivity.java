package com.example.lile.localvisual;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
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
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.example.lile.localvisual.ck.PoiSearchActivity;

/**
 * Created by lile on 2017/4/23.
 * 用于呈现当前的搜索结果的一个页面
 */

public class SearchResultActivity extends Activity {
    private Button btn_refreshresult;
    private TextView tv_resutltext;
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
    private MyLocationConfiguration.LocationMode currentMode;
    // 定位图标描述
    private BitmapDescriptor currentMarker = null;
    private boolean isFirstLoc = true;
    private Vibrator mVibrator; //震动
    private String str_search;
    private Intent intent;

    //检索信息
    private PoiSearch poiSearch;
    private SuggestionSearch suggestionSearch;
    private ShareUrlSearch shareUrlSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
        citySearch();
    }

    void init(){
        initview();
        tv_resutltext.setText(str_search+"的搜索结果是：");
        btn_refreshresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 刷新搜索结果
            }
        });
        //TODO 搜索结果

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLoc=true;
            }
        });
        currentMode = MyLocationConfiguration.LocationMode.COMPASS;
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                currentMode, true, currentMarker));
        baiduMap.setMyLocationEnabled(true);
        // 1. 初始化LocationClient类
        locationClient = new LocationClient(getApplicationContext());
        // 2. 声明LocationListener类
        locationListener = new SearchResultActivity.MyLocationListener();
        // 3. 注册监听函数
        locationClient.registerLocationListener(locationListener);
        // 4. 设置参数
        LocationClientOption locOption = new LocationClientOption();
        locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        locOption.setCoorType("bd09ll");// 设置定位结果类型
        locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

        locationClient.setLocOption(locOption);
        // 5. 注册位置提醒监听事件
        notifyListener = new SearchResultActivity.MyNotifyListener();
        notifyListener.SetNotifyLocation(longitude, latitude, 3000, "bd09ll");//精度，维度，范围，坐标类型
        locationClient.registerNotify(notifyListener);
        // 6. 开启/关闭 定位SDK
        locationClient.start();
    }

    void initview(){
        mapView = (MapView) findViewById(R.id.baidumap);
        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        locateBtn = (Button) findViewById(R.id.btn_location);
        btn_refreshresult = (Button)findViewById(R.id.btn_refresh);
        tv_resutltext = (TextView) findViewById(R.id.tv_resulttext);
        intent = this.getIntent();
        str_search= intent.getStringExtra("searchInfo");

        //检索组件初始化
        poiSearch = PoiSearch.newInstance();
        suggestionSearch = SuggestionSearch.newInstance();
        shareUrlSearch = ShareUrlSearch.newInstance();

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
//                Toast.makeText(MainActivity.this, addrStr,
//                        Toast.LENGTH_SHORT).show();
            }
            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
            district = location.getDistrict();// 区县

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)//
                    .direction(direction)// 方向
                    .latitude(latitude)//
                    .longitude(longitude)//
                    .build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(latitude, longitude);
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
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

    /**
     * 自定义poi搜索
     * @param str
     */

    private void mypoisearch (String str){

    }
    OnGetSuggestionResultListener suggestionResultListener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {

        }
    };
    OnGetShareUrlResultListener shareUrlResultListener = new OnGetShareUrlResultListener() {
        //poi详情分享url
        @Override
        public void onGetPoiDetailShareUrlResult(ShareUrlResult arg0) {

            Toast.makeText(SearchResultActivity.this, "详细url分享："+arg0.toString(), Toast.LENGTH_SHORT).show();
        }
        //请求位置信息分享url
        @Override
        public void onGetLocationShareUrlResult(ShareUrlResult arg0) {

            Toast.makeText(SearchResultActivity.this, "url分享："+arg0.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {

        }
    };
    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null
                    || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(SearchResultActivity.this, "未找到结果",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                baiduMap.clear();
                SearchResultActivity.MyPoiOverlay poiOverlay = new SearchResultActivity.MyPoiOverlay(baiduMap);
                poiOverlay.setData(poiResult);
                baiduMap.setOnMarkerClickListener(poiOverlay);
                poiOverlay.addToMap();
                poiOverlay.zoomToSpan();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(SearchResultActivity.this, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            } else {// 正常返回结果的时候，此处可以获得很多相关信息
                Toast.makeText(
                        SearchResultActivity.this,
                        poiDetailResult.getName() + ": "
                                + poiDetailResult.getAddress(),
                        Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap arg0) {
            super(arg0);
        }

        // 检索Poi详细信息.获取PoiOverlay
        @Override
        public boolean onPoiClick(int arg0) {
            super.onPoiClick(arg0);
            PoiInfo poiInfo = getPoiResult().getAllPoi().get(arg0);
            poiSearch.searchPoiDetail(new PoiDetailSearchOption()
                    .poiUid(poiInfo.uid));
            return true;
        }

    }

    /**
     * 城市内搜索
     */
    private void citySearch() {
        // 设置检索参数
        PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
        citySearchOption.city(city);// 城市
        citySearchOption.keyword(str_search);// 关键字
        citySearchOption.pageCapacity(15);// 默认每页10条
//        citySearchOption.pageNum(page);// 分页编号
        // 发起检索请求
        poiSearch.searchInCity(citySearchOption);
    }

    /**
     * 范围检索
     */
    private void boundSearch(int page) {
        PoiBoundSearchOption boundSearchOption = new PoiBoundSearchOption();
        LatLng southwest = new LatLng(latitude - 0.01, longitude - 0.012);// 西南
        LatLng northeast = new LatLng(latitude + 0.01, longitude + 0.012);// 东北
        LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
                .include(northeast).build();// 得到一个地理范围对象
        boundSearchOption.bound(bounds);// 设置poi检索范围
        boundSearchOption.keyword(str_search);// 检索关键字
        boundSearchOption.pageNum(page);
        poiSearch.searchInBound(boundSearchOption);// 发起poi范围检索请求
    }

    /**
     * 附近检索
     */
    private void nearbySearch(int page) {
        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
        nearbySearchOption.location(new LatLng(latitude, longitude));
        nearbySearchOption.keyword(str_search);
        nearbySearchOption.radius(1000);// 检索半径，单位是米
        nearbySearchOption.pageNum(page);
        poiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
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
