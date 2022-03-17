package com.lindum.mlkit.vision.dev.utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class FileUtils {
    public static final int overwriteText = 0;
    public static final int appendText = 1;

    /**
     * 字符串写入本地txt，覆盖原内容
     *
     * @param stringContent 文件内容
     * @param filePath      文件地址
     * @param fileName      文件名
     * @param writeMethod   写入方式
     * @return 写入结果
     */
    public static boolean writeStringToFile(String stringContent, String filePath, String fileName, int writeMethod) {
        String strFilePath = filePath + fileName;
        File file = new File(strFilePath);
        if (writeMethod == 0) {
            try {
                Logger.e(file.getPath() + "---" + file.getName());
                if (!file.exists()) {
                    Log.d("TestFile", "Create the file:" + strFilePath);
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringContent);
                fileWriter.close();
            } catch (Exception e) {
                Log.e("TestFile", "Error on write File:" + e);
                return false;
            }
        } else if (writeMethod == 1) {
            try {
                // 打开一个随机访问文件流，按读写方式
                RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                // 文件长度，字节数
                long fileLength = randomFile.length();
                // 将写文件指针移到文件尾。
                randomFile.seek(fileLength);
                randomFile.writeBytes(stringContent + "\r\n");
                randomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 将文件读取到字符串
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @return 文件内容
     */
    public static String readFileToString(String filePath, String fileName) {
        String fileContent = "";
        String strFilePath = filePath + fileName;
        //文件逐行读入
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(strFilePath)));
            //按行读取 将每次读取一行的结果赋值给lineStr
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                fileContent = fileContent + lineStr;
            }
            br.close();//关闭IO
        } catch (Exception e) {
            System.out.println("文件操作失败");
            e.printStackTrace();
        }
        return fileContent;
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isFileExist(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        return file.exists();
    }

    public static void getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        Logger.e("文件夹:" + file2.getAbsolutePath());
                    } else {
                        Logger.e("文件:" + file2.getAbsolutePath());
                    }
                }
            }
        } else {
            Logger.e("文件不存在!");
        }
    }
}
