package com.lindum.mlkit.vision.dev.posedetector;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class PoseDataUtils {
    /**
     * 把 JSON 数据写入本地
     *
     * @param jsonText
     * @param filePath
     */
    public static void writeJSON2Loacl(List<PoseEntity> poseEntityList, String filePath) {
        String jsonText = JSON.toJSONString(poseEntityList);
        ;
        try {
            File file = new File(filePath);
            // 创建上级目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 如果文件存在，则删除文件
            if (file.exists()) {
                file.delete();
            }
            // 创建文件
            file.createNewFile();
            // 写入文件
            Writer write = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            }
            Log.e("file","文件写入成功");
            write.write(jsonText);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
