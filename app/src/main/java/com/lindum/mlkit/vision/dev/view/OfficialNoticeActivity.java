package com.lindum.mlkit.vision.dev.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.entity.OfficialNoticeEntity;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息列表活动
 *
 * @author MarkGosling
 * @date 2022/3/14 10:14
 **/


public class OfficialNoticeActivity extends AppCompatActivity {

    private final List<OfficialNoticeEntity> chatList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        setContentView(R.layout.activity_chat_list);
        initChat();
        OfficialNoticeAdapter adapter = new OfficialNoticeAdapter(OfficialNoticeActivity.this, R.layout.chat_item, chatList);
        ListView listView = findViewById(R.id.chat_list);
        listView.setAdapter(adapter);

    }

    private void initChat() {
        // 标题栏
        TitleBar noticeTitleBar = findViewById(R.id.noticeTitleBar);
        noticeTitleBar.setLeftClickListener(v -> {
            finish();
        });
        long currentTime = System.currentTimeMillis();
        @SuppressLint("SimpleDateFormat") String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
//        timeNow = "2022-03-12 12:35:27";
        String chatContent = "上周02/07 ~ 02/13 没有运动，这是你的运动数据周报。";
        for (int i = 0; i < 20; i++) {
            Resources resources = getResources();
            OfficialNoticeEntity chat = new OfficialNoticeEntity("系统通知", timeNow, chatContent, resources.getIdentifier("notice", "drawable", getPackageName()));
            chatList.add(chat);
        }

        // 状态栏背景条
//        LinearLayout statusBar = findViewById(R.id.homePageStatusBar);
//        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
//        params.height = StatusBarUtils.getStatusBarHeight(this);
    }
}