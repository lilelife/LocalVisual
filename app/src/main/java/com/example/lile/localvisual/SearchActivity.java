package com.example.lile.localvisual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.lile.localvisual.view.BusLineSearchActivity;

/**
 * Created by lile on 2017/4/23.
 */

public class SearchActivity extends Activity {
    private Button btn_return;
    private Button btn_search_action;
    private Button btn_textdelete;
    private EditText edt_search;
    private ListView listView;
    private View empty_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();
    }
    //按钮点击等
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_deleteText:
                        edt_search.setText("");
                        break;
                case R.id.btn_return:
                    finish();//返回上一层打的Mainactivity
                    break;
                case R.id.btn_search:
                    Intent intent=new Intent(SearchActivity.this, BusLineSearchActivity.class);
                    intent.putExtra("searchInfo",edt_search.getText().toString().trim());
                    startActivity(intent);
                    break;
            }
        }
    };
    void init(){
        btn_return=(Button)findViewById(R.id.btn_return);
        btn_return.setOnClickListener(listener);
        btn_search_action=(Button)findViewById(R.id.btn_search_action);
        btn_search_action.setOnClickListener(listener);
        btn_textdelete=(Button)findViewById(R.id.btn_deleteText);
        btn_textdelete.setOnClickListener(listener);
        edt_search=(EditText)findViewById(R.id.edt_search);
        if(edt_search.getText().toString().trim()!=""){
            btn_textdelete.setVisibility(View.VISIBLE);
        }
        listView=(ListView)findViewById(R.id.lst_history);
        empty_view=(View)findViewById(R.id.empty_list);
        listView.setEmptyView(empty_view);//当用户没有地点搜索信息时候，设置一个空的empty view

    }

}
