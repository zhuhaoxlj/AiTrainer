package com.lindum.mlkit.vision.dev.sql;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lindum.mlkit.vision.dev.posedetector.PoseEntity;
import com.lindum.mlkit.vision.dev.posedetector.PosePart;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YipJing
 * @date 2022/2/15 13:46
 */
public class ControlTable {

    /**
     * 建表函数
     *
     * @param createTable
     */
    public void createTable(String createTable) {
        Statement statement = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            // 创建表
            statement = connection.createStatement();
            if (statement.executeUpdate(createTable) == 0) {
                Logger.e("成功创建表");
            } else {
                Logger.e("创建失败");
            }
            // 关闭连接
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 读Json文件 函数
     *
     * @param fileName JSON 文件名
     * @return 返回 JSON String
     */
    public String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 写入json文件 函数
     *
     * @param jsonPath
     */
    public void writeInJson(String jsonPath) {
        PreparedStatement preparedStatement = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String s = readJsonFile(jsonPath);
            JSONObject jsonObject = JSON.parseObject(s);
            JSONArray poseEntity = jsonObject.getJSONArray("poseEntities");
            String sql = null;
            for (int i = 0; i < poseEntity.size(); i++) {
                JSONObject key = (JSONObject) poseEntity.get(i);

                //1
                String poseName = (String) key.get("poseName");
                //2
                Integer poseType = (Integer) key.get("poseType");
                //3
                Integer poseProcessValue = (Integer) key.get("poseProcessValue");
                //4
                JSONArray monitorPosePartList = (JSONArray) key.get("monitorPosePartList");
                //5
                Integer bestMonitorAngle = (Integer) key.get("bestMonitorAngle");
                //6
                String trainPartList = (String) key.get("trainPartList");
                //7
                String videoURL = (String) key.get("videoURL");
                //8
                String stepsOfPose = (String) key.get("stepsOfPose");
                //9
                String breathTips = (String) key.get("breathTips");
                //10
                String poseFeeling = (String) key.get("poseFeeling");
                //11
                String poseFeelingImage = (String) key.get("poseFeelingImage");
                //12
                String commonMistakes = (String) key.get("commonMistakes");
                //13
                String detailImageURL = (String) key.get("detailImageURL");
                //14
                String detailTips = (String) key.get("detailTips");

                sql = "INSERT  INTO poseEntity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, poseName);
                preparedStatement.setInt(2, poseType);
                preparedStatement.setInt(3, poseProcessValue);
                preparedStatement.setString(4, monitorPosePartList.toString());
                preparedStatement.setInt(5, bestMonitorAngle);
                preparedStatement.setString(6, trainPartList);
                preparedStatement.setString(7, videoURL);
                preparedStatement.setString(8, stepsOfPose);
                preparedStatement.setString(9, breathTips);
                preparedStatement.setString(10, poseFeeling);
                preparedStatement.setString(11, poseFeelingImage);
                preparedStatement.setString(12, commonMistakes);
                preparedStatement.setString(13, detailImageURL);
                preparedStatement.setString(14, detailTips);

                preparedStatement.executeUpdate();
            }
            System.out.println("写入成功");
            // 关闭连接
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 写入实体类 函数
     *
     * @param poseEntity
     */
    public void writeInClass(PoseEntity poseEntity) {
        PreparedStatement preparedStatement = null;
        String sql = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String jsonStr = JSONObject.toJSONString(poseEntity);
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            //1
            String poseName = (String) jsonObject.get("poseName");
            //2
            Integer poseType = (Integer) jsonObject.get("poseType");
            //3
            Integer poseProcessValue = (Integer) jsonObject.get("poseProcessValue");
            //4
            JSONArray monitorPosePartList = (JSONArray) jsonObject.get("monitorPosePartList");
            //5
            Integer bestMonitorAngle = (Integer) jsonObject.get("bestMonitorAngle");
            //6
            JSONArray trainPartList = (JSONArray) jsonObject.get("trainPartList");
            //7
            String videoURL = (String) jsonObject.get("videoURL");
            //8
            JSONArray stepsOfPose = (JSONArray) jsonObject.get("stepsOfPose");
            //9
            String breathTips = (String) jsonObject.get("breathTips");
            //10
            JSONArray poseFeeling = (JSONArray) jsonObject.get("poseFeeling");
            //11
            String poseFeelingImage = (String) jsonObject.get("poseFeelingImage");
            //12
            String commonMistakes = (String) jsonObject.get("commonMistakes");
            //13
            JSONArray detailImageURL = (JSONArray) jsonObject.get("detailImageURL");
            //14
            JSONArray detailTips = (JSONArray) jsonObject.get("detailTips");

            sql = "INSERT  INTO poseEntity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, poseName);
            preparedStatement.setInt(2, poseType);
            preparedStatement.setInt(3, poseProcessValue);
            preparedStatement.setString(4, monitorPosePartList.toString());
            preparedStatement.setInt(5, bestMonitorAngle);
            preparedStatement.setString(6, trainPartList.toString().replace("[", "").replace("]", "").replace("\"", ""));
            preparedStatement.setString(7, videoURL);
            preparedStatement.setString(8, stepsOfPose.toString().replace("[", "").replace("]", "").replace("\"", ""));
            preparedStatement.setString(9, breathTips);
            preparedStatement.setString(10, poseFeeling.toString().replace("[", "").replace("]", "").replace("\"", ""));
            preparedStatement.setString(11, poseFeelingImage);
            preparedStatement.setString(12, commonMistakes);
            preparedStatement.setString(13, detailImageURL.toString().replace("[", "").replace("]", "").replace("\"", ""));
            preparedStatement.setString(14, detailTips.toString().replace("[", "").replace("]", "").replace("\"", ""));

            preparedStatement.executeUpdate();
            System.out.println("写入成功");
            // 关闭连接
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 读表函数
     *
     * @return poseEntityList
     * List<PoseEntity> poseEntityList = new ArrayList<>();
     */
    public List<PoseEntity> readTable() {
        List<PoseEntity> poseEntityList = new ArrayList<>();
        Logger.e("123132");
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String sql = "select * from poseEntity";
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PoseEntity poseEntity = new PoseEntity();

                String poseName = resultSet.getString("poseName");
                poseEntity.setPoseName(poseName);

                int poseTYPE = resultSet.getInt("poseType");
                poseEntity.setPoseType(poseTYPE);

                int poseProcessValue = resultSet.getInt("poseProcessValue");
                poseEntity.setPoseProcessValue(poseProcessValue);

                List<PosePart> monitorPosePartList = new ArrayList<>();
                String monitorPosePartString = resultSet.getString("monitorPosePartList");

                JSONArray posePartJSONArray = JSONArray.parseArray(monitorPosePartString);
                for (int i = 0; i < posePartJSONArray.size(); i++) {
                    JSONObject key = (JSONObject) posePartJSONArray.get(i);
                    PosePart posePart = new PosePart();

                    int poseIndex = (Integer) key.get("poseIndex");
                    posePart.setPoseIndex(poseIndex);


                    JSONArray threePointIndexJA = (JSONArray) key.get("threePointIndex");
                    if (threePointIndexJA != null) {
                        String threePointIndexStr = threePointIndexJA.toString().replace("[", "").replace("]", "");

                        String[] threePointIndexStrArray = threePointIndexStr.split(",");
                        int[] threePointIndex = new int[threePointIndexStrArray.length];
                        for (int j = 0; j < threePointIndexStrArray.length; j++) {
                            threePointIndex[j] = Integer.parseInt(threePointIndexStrArray[j]);
                        }
                        posePart.setThreePointIndex(threePointIndex);
                    }

                    int poseType = (Integer) key.get("poseType");
                    posePart.setPoseType(poseType);

                    int poseAngleType = (Integer) key.get("poseAngleType");
                    posePart.setPoseAngleType(poseAngleType);

                    JSONArray fixedRangeJA = (JSONArray) key.get("fixedRange");
                    if (fixedRangeJA != null) {
                        String fixedRangeStr = fixedRangeJA.toString().replace("[", "").replace("]", "");
                        String[] fixedRangeStrArray = fixedRangeStr.split(",");
                        int[] fixedRange = new int[fixedRangeStrArray.length];
                        for (int j = 0; j < fixedRangeStrArray.length; j++) {
                            fixedRange[j] = Integer.parseInt(fixedRangeStrArray[j]);
                        }
                        posePart.setFixedRange(fixedRange);
                    }

                    JSONArray firstStandardRangeJA = (JSONArray) key.get("firstStandardRange");
                    if (firstStandardRangeJA != null) {
                        String firstStandardRangeStr = firstStandardRangeJA.toString().replace("[", "").replace("]", "");
                        String[] firstStandardRangeStrArray = firstStandardRangeStr.split(",");
                        int[] firstStandardRange = new int[firstStandardRangeStrArray.length];
                        for (int j = 0; j < firstStandardRangeStrArray.length; j++) {
                            firstStandardRange[j] = Integer.parseInt(firstStandardRangeStrArray[j]);
                        }
                        posePart.setFirstStandardRange(firstStandardRange);
                    }

                    JSONArray secondStandardRangeJA = (JSONArray) key.get("secondStandardRange");
                    if (secondStandardRangeJA != null) {
                        String secondStandardRangeStr = secondStandardRangeJA.toString().replace("[", "").replace("]", "");
                        String[] secondStandardRangeStrArray = secondStandardRangeStr.split(",");
                        int[] secondStandardRange = new int[secondStandardRangeStrArray.length];
                        for (int j = 0; j < secondStandardRangeStrArray.length; j++) {
                            secondStandardRange[j] = Integer.parseInt(secondStandardRangeStrArray[j]);
                        }
                        posePart.setSecondStandardRange(secondStandardRange);
                    }
                    monitorPosePartList.add(posePart);
                }
                poseEntity.setMonitorPosePartList(monitorPosePartList);

                int bestMonitorAngle = resultSet.getInt("bestMonitorAngle");
                poseEntity.setBestMonitorAngle(bestMonitorAngle);

                List<String> trainPartList = new ArrayList<>();
                String[] trainPartListString = resultSet.getString("trainPartList").replace("[", "").replace("]", "").split(",");
                for (int i = 0; i < trainPartListString.length; i++) {
                    trainPartList.add(trainPartListString[i]);
                }
                poseEntity.setTrainPartList(trainPartList);

                String videoURL = resultSet.getString("videoURL");
                poseEntity.setVideoUrl(videoURL);

                String stepsOfPoseStr = resultSet.getString("stepsOfPose");
                String[] stepsOfPose = stepsOfPoseStr.replace("[", "").replace("]", "").split(",");
                poseEntity.setStepsOfPose(stepsOfPose);

                String breathTips = resultSet.getString("breathTips");
                poseEntity.setBreathTips(breathTips);

                String poseFeelingStr = resultSet.getString("poseFeeling");
                String[] poseFeeling = poseFeelingStr.replace("[", "").replace("]", "").split(",");
                poseEntity.setPoseFeeling(poseFeeling);

                String poseFeelingImage = resultSet.getString("poseFeelingImage");
                poseEntity.setPoseFeelingImage(poseFeelingImage);

                String commonMistakes = resultSet.getString("commonMistakes");
//                poseEntity.setCommonMistakes(commonMistakes);

                String detailTipsStr = resultSet.getString("detailTips");
                String[] detailTips = detailTipsStr.replace("[", "").replace("]", "").split(",");
                poseEntity.setDetailTips(detailTips);

                String detailImageUrl = resultSet.getString("detailImageURL");
                String[] detailImageURL = detailImageUrl.replace("[", "").replace("]", "").split(",");
                poseEntity.setDetailImageUrl(detailImageURL);

                poseEntityList.add(poseEntity);
            }
            // 关闭连接
            preparedStatement.close();
            connection.close();
            Logger.e("Success");
            return poseEntityList;
        } catch (SQLException e) {
            Logger.e(e.getMessage() + "");
            return null;
        }
    }


    public int delete(String poseName) {
        PreparedStatement preparedStatement = null;
        String sql = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            Logger.e(connection.toString());
            sql = "delete from poseEntity where poseName = ?";
            sql = sql.replace("?", "\"" + poseName + "\"");
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate(sql);
            // 关闭连接
            preparedStatement.close();
            connection.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

}
