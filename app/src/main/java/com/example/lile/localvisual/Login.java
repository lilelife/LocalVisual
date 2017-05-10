package com.example.lile.localvisual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lile.localvisual.bean._User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by lile on 2017/5/7.
 */

public class Login extends Activity {
    private EditText et_name;
    private EditText et_password;
    private Button btn_login;
    private  Button tv_sign;
    private _User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Bmob.initialize(this, "cedd190c558644d012167c477e2a68c9");
        initview();
    }

    void initview(){
        et_name = (EditText) findViewById(R.id.et_loginname);
        et_password = (EditText) findViewById(R.id.et_loginpassword);
        btn_login = (Button) findViewById(R.id.btn_login);



        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = new _User();
                user.setUsername(et_name.getText().toString());
                user.setPassword(et_password.getText().toString());
                Log.i("LOGINActivity","查询前"+et_name.getText().toString()+"--"+et_password.getText().toString());
                user.login(new SaveListener<_User>() {

                    @Override
                    public void done(_User bmobUser, BmobException e) {
                        if(e==null){
                            Toast.makeText(Login.this,"登录成功:",Toast.LENGTH_LONG);
                            //通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
                            //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
                            Intent intent = new Intent(Login.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            Log.i("登录Activity","登 录失败"+et_name.getText());
                        }
                    }
                });

            }
        });
        tv_sign = (Button) findViewById(R.id.tv_tosign);
        tv_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Sign.class);
                startActivity(intent);
            }
        });
    }
}
