package com.example.lile.localvisual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.lile.localvisual.bean._User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by lile on 2017/5/7.
 */

public class Sign extends Activity {
    private EditText et_name;
    private EditText et_password;
    private TextView et_password2;
    private Button btn_sign;
    private BmobUser user;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign);
//        Bmob.initialize(this, "cedd190c558644d012167c477e2a68c9");//后端云
        initview();
    }
    void initview(){
        et_name=(EditText) findViewById(R.id.et_signname);
        et_password = (EditText) findViewById(R.id.et_signpassword);
        et_password2 = (EditText) findViewById(R.id.et_signpassword2);
        btn_sign = (Button)findViewById(R.id.btn_sign);
//        Log.i("注册Activity","-->"+et_password.getText().toString()+"-"+et_name.getText().toString().trim());
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = new _User();
                user.setPassword(et_password.getText().toString()); // et_name.getText().toString()
                user.setUsername(et_name.getText().toString());
                Log.i("注册Activity2","-->"+et_password.getText().toString()+"-"+et_name.getText().toString().trim());
                user.signUp(new SaveListener<_User>() {
                    @Override
                    public void done(_User users, BmobException e) {
                        if(e==null){
                            Log.i("注册","注册成功:");
                            Intent intent = new Intent(Sign.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            Log.i("注册","注册失败:" );// +users.toString()

                        }
                    }
                });

            }
        });
    }
}
