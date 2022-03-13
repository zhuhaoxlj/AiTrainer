package com.lindum.mlkit.vision.dev.view;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;

import com.lindum.mlkit.vision.dev.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatList extends AppCompatActivity {

    private final List<ChatEntity> chatList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        initChat();
        ChatAdapter adapter = new ChatAdapter(ChatList.this,R.layout.chat_item, chatList);
        ListView listView = (ListView) findViewById(R.id.chat_list);
        listView.setAdapter(adapter);

    }

    private void initChat() {
        long currentTime = System.currentTimeMillis();
        @SuppressLint("SimpleDateFormat") String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime);
//        timeNow = "2022-03-12 12:35:27";
        String chatContent = "米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树米粒麻辣猴子乱跳猴子上树";
        for (int i = 0; i < 20; i++) {
            Resources resources = getResources();
            ChatEntity chat = new ChatEntity("Monkey" + i,timeNow,chatContent,resources.getIdentifier("monkey", "drawable", getPackageName()));
            chatList.add(chat);
        }
    }
}