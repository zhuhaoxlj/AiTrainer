package com.lindum.mlkit.vision.dev.result;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.lindum.mlkit.vision.dev.R;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.view.HomePage;
import com.orhanobut.logger.Logger;
import com.vivian.timelineitemdecoration.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DotTimeLineAdapter extends RecyclerView.Adapter<DotTimeLineAdapter.ViewHolder> {
    Context mContext;
    List<Event> mList;
    int[] colors = {0xffFFAD6C, 0xff62f434, 0xffdeda78, 0xff7EDCFF, 0xff58fdea, 0xfffdc75f};//颜色组

    public void setList(List<Event> list) {
        mList = list;
    }

    public DotTimeLineAdapter(Context context) {
        mContext = context;
    }

    public DotTimeLineAdapter(Context context, List<Event> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pop_item, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.time.setText(Util.LongtoStringFormat(1000 * mList.get(position).getTime()));
        holder.textView.setText(mList.get(position).getEvent());
        holder.textView.setTextColor(Color.parseColor("#000000"));
        holder.time.setTextColor(colors[position % colors.length]);
        // 添加点击监听事件
        holder.itemView.setOnClickListener(v -> {
            Logger.e("这是第" + position + "个日记" + "--时间戳为：" + mList.get(position).getTime());
            String poseDataPath = HomePage.appFilePath + "/files/analysisReport/" + Util.LongtoStringFormat(1000 * mList.get(position).getTime()).split(" ")[0].replace(".", "-") + "/" + mList.get(position).getTime() + "";
            if (new File(poseDataPath).exists() && new File(poseDataPath + "/analysisReport.json").exists()) {
                Logger.e("动作数据存在");
                ArrayList<JSONResultEntity> arrayList = convertJSON2ArrayList(readFile2String(poseDataPath + "/analysisReport.json"));
                ArrayList<ResultEntity> resultEntities = new ArrayList<>();
                for (JSONResultEntity temp : arrayList) {
                    ResultEntity resultEntity = new ResultEntity();
                    resultEntity.setPoseName(temp.getPoseName());
                    // 找到正确的图片
                    Bitmap rightPoseImage = null;
                    for (PoseEntity poseEntity : HomePage.poseEntityArrayList) {
                        if (poseEntity.getPoseName().equals(temp.getPoseName())) {
                            int indexOfPoseEntityList = HomePage.poseEntityArrayList.indexOf(poseEntity);
                            rightPoseImage = HomePage.poseRightImageArrayList.get(indexOfPoseEntityList);
                        }
                    }
                    resultEntity.setRightPoseImage(rightPoseImage);
                    File wrongImageFile = new File(temp.getWrongPoseImagePath());
                    try {
                        InputStream inputStream = new FileInputStream(wrongImageFile);
                        resultEntity.setWrongPoseImage(BitmapFactory.decodeStream(inputStream));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    resultEntity.setTips(temp.getTips());
                    resultEntities.add(resultEntity);
                }
                TrainingResult.resultEntityList = resultEntities;
                Intent intent = new Intent(mContext, TrainingResult.class);
                intent.putExtra("init",1);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, "动作结果数据不存在或者已损坏", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public ArrayList<JSONResultEntity> convertJSON2ArrayList(String jsonContent) {
        ArrayList<JSONResultEntity> arrayList = (ArrayList<JSONResultEntity>) JSONObject.parseArray(jsonContent, JSONResultEntity.class);
        return (ArrayList<JSONResultEntity>) arrayList;
    }

    /**
     * 将文件读取到字符串
     *
     * @param filePath 文件路径
     * @return String 返回文件字符串内容
     */
    public static String readFile2String(String filePath) {
        String fileContent = "";
        //文件逐行读入
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(filePath)));
            String linestr;//按行读取 将每次读取一行的结果赋值给linestr
            while ((linestr = br.readLine()) != null) {
                fileContent = fileContent + linestr;
            }
            br.close();//关闭IO
        } catch (Exception e) {
            System.out.println("文件操作失败");
            e.printStackTrace();
        }
        return fileContent;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView textView;

        public ViewHolder(View view) {
            super(view);
            time = (TextView) view.findViewById(R.id.time);
            textView = (TextView) view.findViewById(R.id.text);
        }
    }

}
