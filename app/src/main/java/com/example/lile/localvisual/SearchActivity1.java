package com.example.lile.localvisual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


/**
 * Created by lile on 2017/4/23.
 */

public class SearchActivity1 extends Activity {
    private Button btn_return;
    private Button btn_search_action;
    private Button btn_textdelete;
    private EditText edt_search;
    private ListView listView;
    private View empty_view;
    private Intent intent;
    private String city;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                case R.id.btn_search_action:
                    Intent intent=new Intent(SearchActivity1.this, SearchResultActivity.class);
                    intent.putExtra("searchInfo",edt_search.getText().toString().trim());
                    startActivity(intent);
                    break;
            }
        }
    };
    void init(){
        intent = getIntent();
        city = intent.getStringExtra("CITY");
        listView = (ListView)findViewById(R.id.lst_history);
        empty_view = (View)findViewById(R.id.empty_list);
        listView.setEmptyView(empty_view);//当用户没有地点搜索信息时候，设置一个空的empty view

        //TODO listview初始化

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
        //实现键盘的enter键的搜索功能
        edt_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                       //写入搜索操作和数据库插入搜索记录操作

                    Intent intent=new Intent(SearchActivity1.this,SearchResultActivity.class);
                    intent.putExtra("searchInfo",edt_search.getText().toString().trim());
                    startActivity(intent);
                }
                return false;
            }
        });
        //实现Textedit实时筛选的操作
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            //TODO 实现查表并显示在列表中
            }
        });


    }

}
