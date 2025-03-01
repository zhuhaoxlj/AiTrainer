package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.orhanobut.logger.Logger;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;

/**
 * @author MarkGosling
 * @date 2022/2/15 0:10
 */
public class ChangeProfileActivity extends AppCompatActivity {
    EditText profileEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.change_profile);
        init();
    }

    public void init() {
        profileEditText = findViewById(R.id.profileEditText);
        profileEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                TextView profileTextLimit = findViewById(R.id.profileTextLimit);
                profileTextLimit.setText(profileEditText.getText().length() + "/160");
            }
        });
        TitleBar nickNameTitleBar = findViewById(R.id.profileTitleBar);
        nickNameTitleBar.addAction(new TitleBar.TextAction("保存") {
            @Override
            public void performAction(View view) {
                Intent intent = new Intent();
                intent.putExtra("profile", profileEditText.getText());
                Logger.e(profileEditText.getText() + "");
                setResult(2, intent);
                finish();
            }
        });
    }
}
