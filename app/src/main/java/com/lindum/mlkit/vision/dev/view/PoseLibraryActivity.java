package com.lindum.mlkit.vision.dev.view;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.posedetector.PoseEntity;
import com.xuexiang.xui.XUI;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 主类
 *
 * @author: MarkGosling
 * @date: 2022/1/27 5:22
 **/

public class PoseLibraryActivity extends AppCompatActivity implements HomeAdapter.ItemListener {
    HomeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pose_library);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ArrayList<Item> arrayList = new ArrayList<>();
        XUI.initTheme(this);
        // 使主线程可以进行简单的网络请求
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Document document = null;
        try {
            document = Jsoup.connect("http://lindum.top/poseData.html").get();
            Log.e("html", document.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Element element = document.body();
        String jsonText = element.ownText();

        ArrayList<PoseEntity> poseEntityArrayList;
        JSONArray jsonArray = (JSONArray) JSON.parse(jsonText);
        poseEntityArrayList = JSON.parseObject(jsonArray.toJSONString(), new TypeReference<ArrayList<PoseEntity>>() {
        });
        Log.e("entity", poseEntityArrayList.toString());
        ArrayList<PoseEntity> poseEntityList = poseEntityArrayList;

        for (PoseEntity poseEntity : poseEntityList) {
            arrayList.add(new Item(poseEntity.getPoseName(), "副标题", R.mipmap.q1));
        }
        adapter = new HomeAdapter(this, arrayList, this);
        recyclerView.setAdapter(adapter);
        GridLayoutManager manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
//        TitleBar titleBar = findViewById(R.id.titleBar);
//        titleBar.setLeftClickListener(view -> Toast.makeText(PoseBankActivity.this, "点击了返回", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onItemClick(Item item) {
        Toast.makeText(getApplicationContext(), item.title + " is clicked", Toast.LENGTH_SHORT).show();
    }

}