package com.example.lile.localvisual.view;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 公交线路查询
 * 
 * @author ys
 *
 */
public class BusLineSearchActivity extends Activity implements OnClickListener {

	private EditText cityEt;
	private EditText buslineEt;
	private Button searchBtn;
	private Button nextlineBtn;

	private MapView mapView;
	private BaiduMap bdMap;

	private String city;// 城市
	private String busline;// 公交路线
	private List<String> buslineIdList;// 存储公交线路的uid
	private int buslineIndex = 0;// 标记第几个路线

	private PoiSearch poiSearch;
	private BusLineSearch busLineSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_busline_search);
		init();
	}

	/**
	 * 初始化操作
	 */
	private void init() {
		mapView = (MapView) findViewById(R.id.mapview);
		bdMap = mapView.getMap();

		cityEt = (EditText) findViewById(R.id.city_et);
		buslineEt = (EditText) findViewById(R.id.searchkey_et);
		searchBtn = (Button) findViewById(R.id.busline_search_btn);
		nextlineBtn = (Button) findViewById(R.id.nextline_btn);
		searchBtn.setOnClickListener(this);
		nextlineBtn.setOnClickListener(this);

		//公交线路信息保存
		buslineIdList = new ArrayList<String>();

		poiSearch = PoiSearch.newInstance();
		poiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);

		busLineSearch = BusLineSearch.newInstance();
		busLineSearch
				.setOnGetBusLineSearchResultListener(busLineSearchResultListener);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.busline_search_btn:
			city = cityEt.getText().toString();
			busline = buslineEt.getText().toString();
			//poi城市检索
			poiSearch.searchInCity((new PoiCitySearchOption()).city(city)
					.keyword(busline));
			break;
		case R.id.nextline_btn:
			searchBusline();
			break;
		}
	}

	//当Poi检索后被调用.
	private void searchBusline() {
		if (buslineIndex >= buslineIdList.size()) {
			buslineIndex = 0;
		}
		if (buslineIndex >= 0 && buslineIndex < buslineIdList.size()
				&& buslineIdList.size() > 0) {
			boolean flag = busLineSearch
					.searchBusLine((new BusLineSearchOption().city(city)
							.uid(buslineIdList.get(buslineIndex))));  //调用Bus路线查询
			if (flag) {
				Toast.makeText(BusLineSearchActivity.this, "检索成功~", 1000)
						.show();
			} else {
				Toast.makeText(BusLineSearchActivity.this, "检索失败~", 1000)
						.show();
			}
			buslineIndex++;
		}
	}

	/**
	 * POI检索结果监听器,按钮被点击时最先被调用
	 */
	OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiResult(PoiResult poiResult) {
			if (poiResult == null
					|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
				Toast.makeText(BusLineSearchActivity.this, "未找到结果",
						Toast.LENGTH_LONG).show();
				return;
			}

			if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
				// 遍历所有poi，找到类型为公交线路的poi
				buslineIdList.clear();
				for (PoiInfo poi : poiResult.getAllPoi()) {
					if (poi.type == PoiInfo.POITYPE.BUS_LINE
							|| poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
						buslineIdList.add(poi.uid);
					}
				}
				searchBusline();
			}
		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult arg0) {

		}
	};

	/**
	 * 公交信息查询结果监听器,一般公交线路是一条不变的.
	 */
	OnGetBusLineSearchResultListener busLineSearchResultListener = new OnGetBusLineSearchResultListener() {

		@Override
		public void onGetBusLineResult(BusLineResult busLineResult) {

			if (busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(BusLineSearchActivity.this, "抱歉，未找到结果",
						Toast.LENGTH_SHORT).show();
			} else {
				bdMap.clear();
				BusLineOverlay overlay = new MyBuslineOverlay(bdMap);// 用于显示一条公交详情结果的Overlay
				overlay.setData(busLineResult);
				overlay.addToMap();// 将overlay添加到地图上
				overlay.zoomToSpan();// 缩放地图，使所有overlay都在合适的视野范围内
				bdMap.setOnMarkerClickListener(overlay);
				// 公交线路名称
				Toast.makeText(BusLineSearchActivity.this,
						busLineResult.getBusLineName(), Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	class MyBuslineOverlay extends BusLineOverlay {

		public MyBuslineOverlay(BaiduMap arg0) {
			super(arg0);
		}

		/**
		 * 站点点击事件
		 */
		@Override
		public boolean onBusStationClick(int arg0) {
			//
			MarkerOptions options = (MarkerOptions) getOverlayOptions().get(arg0);
			bdMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(options.getPosition()));
			return true;
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
		poiSearch.destroy();// 释放检索对象资源
		busLineSearch.destroy();// 释放检索对象资源
		mapView.onDestroy();
	}

}
