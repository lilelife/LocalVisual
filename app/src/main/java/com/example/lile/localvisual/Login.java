package com.example.lile.localvisual;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lile.localvisual.bean.Users;

import org.w3c.dom.Text;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by lile on 2017/5/7.
 */

public class Login extends Activity {
    private EditText et_name;
    private EditText et_password;
    private Button btn_login;
    private  TextView tv_sign;
    private Users user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Bmob.initialize(this, "cedd190c558644d012167c477e2a68c9");
        initview();
    }

    void initview(){
        et_name = (EditText) findViewById(R.id.et_loginname);
        et_password = (EditText) findViewById(R.id.et_loginpassword);
        btn_login = (Button) findViewById(R.id.btn_login);

        user.setUsername("lucky");
        user.setPassword("123456");
        user.login(new SaveListener<Users>() {

            @Override
            public void done(Users bmobUser, BmobException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(),"登录成功:",Toast.LENGTH_LONG);
                    //通过BmobUser user = BmobUser.getCurrentUser()获取登录成功后的本地用户信息
                    //如果是自定义用户对象MyUser，可通过MyUser user = BmobUser.getCurrentUser(MyUser.class)获取自定义用户信息
                }else{
                    Log.i("登录","登录失败"+et_name.getText());
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,MainActivity.class);
                startActivity(intent);
            }
        });

        tv_sign = (TextView) findViewById(R.id.tv_tosign);
        tv_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Sign.class);
                startActivity(intent);
            }
        });
    }
}
