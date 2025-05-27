package com.lindum.mlkit.vision.utils;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    // 获取文件夹列表
    public final static int GET_FOLDER_LIST = 0;
    // 获取文件列表
    public final static int GET_FILE_LIST = 1;
    // 全部获取
    public final static int GET_ALL_LIST = 2;

    // 缓存最近读取的文件内容
    private static String lastReadFilePath = "";
    private static ArrayList<String> cachedFileContent = new ArrayList<>();

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
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(stringContent);
                fileWriter.close();
            } catch (Exception e) {
                Logger.e("TestFile", "Error on write File:" + e);
                return false;
            }
        } else if (writeMethod == 1) {
            try {
                File writeFile = new File(strFilePath);
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(writeFile, true);
                outputStream.write(("\n" + stringContent).getBytes());
                outputStream.close();
                
                // 如果写入的文件正好是缓存的文件，清除缓存
                if ((filePath + fileName).equals(lastReadFilePath)) {
                    clearFileCache();
                }
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
    public static ArrayList<String> readFileToString(String filePath, String fileName) {
        String fullPath = filePath + fileName;
        
        // 检查是否有缓存
        if (fullPath.equals(lastReadFilePath) && !cachedFileContent.isEmpty()) {
            return new ArrayList<>(cachedFileContent);
        }
        
        ArrayList<String> fileContent = new ArrayList<>();
        //文件逐行读入
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fullPath)));
            //按行读取 将每次读取一行的结果赋值给lineStr
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                if (!lineStr.equals("")) {
                    fileContent.add(lineStr.trim());
                }
            }
            br.close();//关闭IO
            
            // 更新缓存
            lastReadFilePath = fullPath;
            cachedFileContent = new ArrayList<>(fileContent);
            
            return fileContent;
        } catch (Exception e) {
            System.out.println("文件操作失败");
            e.printStackTrace();
        }
        return fileContent;
    }
    
    /**
     * 分页读取文件
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @param pageSize 每页大小
     * @param pageNum 页码（从0开始）
     * @return 文件内容
     */
    public static ArrayList<String> readFileToStringPaged(String filePath, String fileName, int pageSize, int pageNum) {
        String fullPath = filePath + fileName;
        ArrayList<String> fileContent = new ArrayList<>();
        ArrayList<String> allContent;
        
        // 检查是否有缓存
        if (fullPath.equals(lastReadFilePath) && !cachedFileContent.isEmpty()) {
            allContent = new ArrayList<>(cachedFileContent);
        } else {
            allContent = new ArrayList<>();
            //文件逐行读入
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(fullPath)));
                //按行读取 将每次读取一行的结果赋值给lineStr
                String lineStr;
                while ((lineStr = br.readLine()) != null) {
                    if (!lineStr.equals("")) {
                        allContent.add(lineStr.trim());
                    }
                }
                br.close();//关闭IO
                
                // 更新缓存
                lastReadFilePath = fullPath;
                cachedFileContent = new ArrayList<>(allContent);
            } catch (Exception e) {
                System.out.println("文件操作失败");
                e.printStackTrace();
                return fileContent;
            }
        }
        
        // 计算分页
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allContent.size());
        
        // 如果起始索引超出范围，返回空列表
        if (startIndex >= allContent.size()) {
            return fileContent;
        }
        
        // 返回请求的页
        for (int i = startIndex; i < endIndex; i++) {
            fileContent.add(allContent.get(i));
        }
        
        return fileContent;
    }
    
    /**
     * 清除文件缓存
     */
    public static void clearFileCache() {
        lastReadFilePath = "";
        cachedFileContent.clear();
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
     * @param filePath       文件路径
     * @param getContentType 获取列表的类型
     * @return 返回文件名或文件夹路径列表
     */
    public static ArrayList<String> getFileName(String filePath, int getContentType) {
        ArrayList<String> fileNameList = new ArrayList<>();
        File file = new File(filePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        if (getContentType == 0 || getContentType == 2) {
                            fileNameList.add(file2.getAbsolutePath());
                        }
                    } else {
                        if (getContentType == 1 || getContentType == 2) {
                            fileNameList.add(file2.getAbsolutePath());
                        }
                    }
                }
            }
            return fileNameList;
        } else {
            Logger.e("文件不存在!");
            return null;
        }
    }

    /**
     * 获取当前路径下的文件夹个数
     *
     * @param path 路径
     * @return 文件夹个数
     */
    public static int countFoldersUnderPath(String path) {
        File file = new File(path);
        int count = 0;
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File temp : files) {
                    if (temp.isDirectory()) {
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
        if (!Objects.requireNonNull(file.getParentFile()).exists()) {
            file.getParentFile().mkdirs();
        }
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

    /**
     * 判断文件夹是否为空
     *
     * @param path 文件夹路径
     * @return 是否为空
     */
    public static boolean isPathEmpty(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.list()).length > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            Logger.e("这不是一个目录!");
        }
        return true;
    }
}
