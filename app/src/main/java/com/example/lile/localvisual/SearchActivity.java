package com.example.lile.localvisual;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by lile on 2017/4/23.
 */

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myview);
//        Search_View view=new Search_View(getApplicationContext());
        Search_View view=(Search_View) findViewById(R.id.search_layout);
        Log.i("搜索activity","begin");
        view.setOnclik(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
