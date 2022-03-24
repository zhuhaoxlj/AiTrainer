package com.lindum.mlkit.vision.dev.utils;

import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Objects;

public class FileUtils {
    // 覆盖写入
    public static final int OVERWRITE_TEXT = 0;
    // 追加文本
    public static final int APPEND_TEXT = 1;
    // 创建文件夹
    public final static int CREATE_FOLDER = 0;
    // 创建文件
    public final static int CREATE_FILE = 1;
    // 移动文件夹
    public final static int MOVE_FOLDER = 0;
    // 移动文件
    public final static int MOVE_FILE = 1;

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

    /**
     * 获取当前路径下的文件名和文件夹名
     *
     * @param filePath 路径
     */
    public static void getFileName(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        Logger.e("123文件夹:" + file2.getAbsolutePath());
                    } else {
                        Logger.e("123文件:" + file2.getAbsolutePath());
                    }
                }
            }
        } else {
            Logger.e("文件不存在!");
        }
    }

    /**
     *  获取当前路径下的文件夹个数
     * @param path 路径
     * @return 文件夹个数
     */
    public static int countFoldersUnderPath(String path){
        File file = new File(path);
        int count = 0;
        if(file.exists()){
            File[]files = file.listFiles();
            if(files!=null){
                for(File temp:files){
                    if(temp.isDirectory()){
                        count++;
                    }
                }
            }
            return count;
        }
        return count;
    }
    /**
     * 创建文件夹或者文件
     *
     * @param fileName 文件夹名称或者文件名称
     * @param fileType 文件类型 true 为文件夹 1 为文件
     */
    public static boolean createFile(String fileName, int fileType) {
        File file = new File(fileName);
        if (!file.exists()) {
            if (fileType == 0) {
                return file.mkdir();
            } else {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 移动文件或者文件夹到指定目录
     *
     * @param oldFileName 旧的文件路径
     * @param newPath     新的文件路径
     */
    public static void moveFiles(String oldFileName, String newPath) {
        File file = new File(oldFileName);
        if (file.isDirectory()) {
            copyFolder(oldFileName, newPath);
            deleteFile(file);
        } else {
            Logger.e(file.renameTo(new File(newPath)) ? "移动成功" : "移动失败");
        }
    }

    // 删除某个目录及目录下的所有子目录和文件
    public static boolean deleteFile(File dir) {
        // 如果是文件夹
        if (dir.isDirectory()) {
            // 则读出该文件夹下的的所有文件
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < Objects.requireNonNull(children).length; i++) {
                // File f=new File（String parent ，String child）
                // parent抽象路径名用于表示目录，child 路径名字符串用于表示目录或文件。
                // 连起来刚好是文件路径
                boolean isDelete = deleteFile(new File(dir, children[i]));
                // 如果删完了，没东西删，isDelete==false的时候，则跳出此时递归
                if (!isDelete) {
                    return false;
                }
            }
        }
        // 读到的是一个文件或者是一个空目录，则可以直接删除
        return dir.delete();
    }

    // 复制某个目录及目录下的所有子目录和文件到新文件夹
    public static void copyFolder(String oldPath, String newPath) {
        try {
            // 如果文件夹不存在，则建立新文件夹
            (new File(newPath)).mkdirs();
            // 读取整个文件夹的内容到file字符串数组，下面设置一个游标i，不停地向下移开始读这个数组
            File filelist = new File(oldPath);
            String[] file = filelist.list();
            // 要注意，这个temp仅仅是一个临时文件指针
            // 整个程序并没有创建临时文件
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                // 如果oldPath以路径分隔符/或者\结尾，那么则oldPath/文件名就可以了
                // 否则要自己oldPath后面补个路径分隔符再加文件名
                // 谁知道你传递过来的参数是f:/a还是f:/a/啊？
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                // 如果游标遇到文件
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    // 复制并且改名
                    FileOutputStream output = new FileOutputStream(newPath
                            + "/" + (temp.getName()));
                    byte[] bufferarray = new byte[1024 * 64];
                    int prereadlength;
                    while ((prereadlength = input.read(bufferarray)) != -1) {
                        output.write(bufferarray, 0, prereadlength);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                // 如果游标遇到文件夹
                if (temp.isDirectory()) {
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
        }
    }
}
