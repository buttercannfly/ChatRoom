package com.example.chatroom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatroom.Activity.ChatRoom;

import static com.example.chatroom.DBUtils.Operation.verify;

public class MainActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verify(username.getText().toString(),password.getText().toString())){
                    Intent intent = new Intent(MainActivity.this, ChatRoom.class);
                    intent.putExtra("username",username.getText().toString());
                    startActivity(intent);
                    Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_LONG).show();
                    // login success
                }
            }
        });
    }

    private void initView() {
        username = this.findViewById(R.id.ed1);
        password = this.findViewById(R.id.ed2);
        login = this.findViewById(R.id.bt);
    }
}
