package com.lindum.mlkit.vision.dev.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;

/**
 * @author MarkGosling
 * @date 2022/2/14 22:49
 */
public class ChangeNickName extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        XUI.initTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_nickname);
        TitleBar nickNameTitleBar = findViewById(R.id.nickNameTitleBar);
        nickNameTitleBar.addAction(new TitleBar.TextAction("保存") {

            @Override
            public void performAction(View view) {
                EditText nickNameEditText = findViewById(R.id.nickNameEditText);
                Toast.makeText(ChangeNickName.this, "保存成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("nickName", nickNameEditText.getText());
                setResult(1, intent);
                finish();
            }
        });
    }
}
