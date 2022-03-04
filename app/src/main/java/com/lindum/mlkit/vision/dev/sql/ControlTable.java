package com.lindum.mlkit.vision.dev.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lindum.mlkit.vision.dev.entity.PoseEntity;
import com.lindum.mlkit.vision.dev.posedetector.PosePart;

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/*
  @author YipJing
 * @date 2022/2/15 13:46
 */

public class ControlTable {

    /**
     * 建表函数
     *
     * @param createTable 创建tableSQL语句
     */
    public void createTable(String createTable) {
        Statement statement;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            // 创建表
            statement = connection.createStatement();
            if (statement.executeUpdate(createTable) == 0) {
                System.out.println("成功创建表");
            } else {
                System.out.println("创建失败");
            }
            // 关闭连接
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param addField 增加字段sql语句
     */
    public void addField(String addField) {
        Statement statement;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            // 创建表
            statement = connection.createStatement();
            if (statement.executeUpdate(addField) == 0) {
                System.out.println("成功增加字段");
            } else {
                System.out.println("增加失败");
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
        String jsonStr;
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch;
            StringBuilder sb = new StringBuilder();
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
     * @param jsonPath JSON文件路径
     */
    public void writeInJson(String jsonPath) {
        PreparedStatement preparedStatement = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String s = readJsonFile(jsonPath);
            JSONObject jsonObject = JSON.parseObject(s);
            JSONArray poseEntity = jsonObject.getJSONArray("poseEntities");
            String sql;
            for (Object o : poseEntity) {
                JSONObject key = (JSONObject) o;

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
                String videoUrl = (String) key.get("videoUrl");
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
                preparedStatement.setString(7, videoUrl);
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
            assert preparedStatement != null;
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 写入实体类 函数
     *
     * @param poseEntity 写入的poseEntity实体类
     */
    public void writeInClass(PoseEntity poseEntity) {
        PreparedStatement preparedStatement;
        String sql = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String jsonStr = JSONObject.toJSONString(poseEntity);
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            //1
            String poseName = (String) jsonObject.get("poseName");

            if (search(poseName) == null) {
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
                String videoUrl = (String) jsonObject.get("videoUrl");
                //8
                JSONArray stepsOfPose = (JSONArray) jsonObject.get("stepsOfPose");
                //9
                String breathTips = (String) jsonObject.get("breathTips");
                //10
                JSONArray poseFeeling = (JSONArray) jsonObject.get("poseFeeling");
                //11
                String poseFeelingImage = (String) jsonObject.get("poseFeelingImage");
                //12
                JSONArray commonMistakes = (JSONArray) jsonObject.get("commonMistakes");
                //13
                JSONArray detailImageUrl = (JSONArray) jsonObject.get("detailImageUrl");
                //14
                JSONArray detailTips = (JSONArray) jsonObject.get("detailTips");
                //15
                Integer poseLevel = (Integer) jsonObject.get("poseLevel");
                //16
                Object poseBurnCalObject = jsonObject.get("poseBurnCal");
                double poseBurnCal = Double.parseDouble(poseBurnCalObject.toString());
                //17
                Object getPoseBurnCalPerMinObject = jsonObject.get("getPoseBurnCalPerMin");
                double getPoseBurnCalPerMin = Double.parseDouble(getPoseBurnCalPerMinObject.toString());
                //18
                String poseCoverImage = (String) jsonObject.get("poseCoverImage");
                //19
                JSONArray poseSwiperImageUrl = (JSONArray) jsonObject.get("poseSwiperImageUrl");
                sql = "INSERT  INTO poseEntity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, poseName);

                preparedStatement.setInt(2, poseType);

                preparedStatement.setInt(3, poseProcessValue);

                if (monitorPosePartList != null) {
                    preparedStatement.setString(4, monitorPosePartList.toString());
                } else {
                    preparedStatement.setString(4, null);
                }

                preparedStatement.setInt(5, bestMonitorAngle);

                if (trainPartList != null) {
                    preparedStatement.setString(6, trainPartList.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(6, null);
                }

                preparedStatement.setString(7, videoUrl);

                if (stepsOfPose != null) {
                    preparedStatement.setString(8, stepsOfPose.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(8, null);
                }

                preparedStatement.setString(9, breathTips);

                if (poseFeeling != null) {
                    preparedStatement.setString(10, poseFeeling.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(10, null);
                }

                preparedStatement.setString(11, poseFeelingImage);

                if (commonMistakes != null) {
                    preparedStatement.setString(12, commonMistakes.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(12, null);
                }

                if (detailImageUrl != null) {
                    preparedStatement.setString(13, detailImageUrl.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(13, null);
                }

                if (detailTips != null) {
                    preparedStatement.setString(14, detailTips.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(14, null);
                }

                preparedStatement.setInt(15, poseLevel);

                preparedStatement.setDouble(16, poseBurnCal);

                preparedStatement.setDouble(17, getPoseBurnCalPerMin);

                preparedStatement.setString(18, poseCoverImage);

                if (poseSwiperImageUrl != null) {
                    preparedStatement.setString(19, poseSwiperImageUrl.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(19, null);
                }

                preparedStatement.executeUpdate();
                preparedStatement.close();
                System.out.println("写入成功");
            } else {
                System.out.println("请不要重复写入！");
            }
            // 关闭连接
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
        PreparedStatement preparedStatement;
        ResultSet resultSet;
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
                if (posePartJSONArray != null) {
                    for (Object o : posePartJSONArray) {
                        JSONObject key = (JSONObject) o;
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
                }
                poseEntity.setMonitorPosePartList(monitorPosePartList);

                int bestMonitorAngle = resultSet.getInt("bestMonitorAngle");
                poseEntity.setBestMonitorAngle(bestMonitorAngle);

                List<String> trainPartList = new ArrayList<>();
                if (resultSet.getString("trainPartList") != null) {
                    String[] trainPartListString = resultSet.getString("trainPartList").replace("[", "").replace("]", "").split(",");
                    Collections.addAll(trainPartList, trainPartListString);
                }
                poseEntity.setTrainPartList(trainPartList);

                String videoUrl = resultSet.getString("videoURL");
                poseEntity.setVideoUrl(videoUrl);

                String stepsOfPoseStr = resultSet.getString("stepsOfPose");
                if (resultSet.getString("stepsOfPose") != null) {
                    String[] stepsOfPose = stepsOfPoseStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setStepsOfPose(stepsOfPose);
                }


                String breathTips = resultSet.getString("breathTips");
                poseEntity.setBreathTips(breathTips);

                String poseFeelingStr = resultSet.getString("poseFeeling");
                if (resultSet.getString("poseFeeling") != null) {
                    String[] poseFeeling = poseFeelingStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setPoseFeeling(poseFeeling);
                }

                String poseFeelingImage = resultSet.getString("poseFeelingImage");
                poseEntity.setPoseFeelingImage(poseFeelingImage);

                String commonMistakesStr = resultSet.getString("commonMistakes");
                if (resultSet.getString("commonMistakes") != null) {
                    String[] commonMistakes = commonMistakesStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setCommonMistakes(commonMistakes);
                }

                String detailTipsStr = resultSet.getString("detailTips");
                if (resultSet.getString("detailTips") != null) {
                    String[] detailTips = detailTipsStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setDetailTips(detailTips);
                }

                String detailImageUrlStr = resultSet.getString("detailImageURL");
                if (resultSet.getString("detailImageURL") != null) {
                    String[] detailImageUrl = detailImageUrlStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setDetailImageUrl(detailImageUrl);
                }

                int poseLevel = resultSet.getInt("poseLevel");
                poseEntity.setPoseLevel(poseLevel);

                double poseBurnCal = resultSet.getDouble("poseBurnCal");
                poseEntity.setPoseBurnCal(poseBurnCal);

                double getPoseBurnCalPerMin = resultSet.getDouble("getPoseBurnCalPerMin");
                poseEntity.setGetPoseBurnCalPerMin(getPoseBurnCalPerMin);

                String poseCoverImage = resultSet.getString("poseCoverImage");
                poseEntity.setPoseCoverImage(poseCoverImage);

                String poseSwiperImageUrlStr = resultSet.getString("poseSwiperImageUrl");
                if (resultSet.getString("poseSwiperImageUrl") != null) {
                    String[] poseSwiperImageUrl = poseSwiperImageUrlStr.replace("[", "").replace("]", "").split(",");
                    poseEntity.setPoseSwiperImageUrl(poseSwiperImageUrl);
                }

                poseEntityList.add(poseEntity);
            }
            // 关闭连接
            preparedStatement.close();
            connection.close();
            System.out.println("success");
            return poseEntityList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * @param poseName 要删除的列的poseName
     * @return 返回删除结果 1为删除成功
     */
    public int delete(String poseName) {
        PreparedStatement preparedStatement = null;

        String sql = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            sql = "delete from poseEntity where poseName = " + "\"" + poseName + "\"";
            preparedStatement = connection.prepareStatement(sql);
            int result = preparedStatement.executeUpdate(sql);
            // 关闭连接
            preparedStatement.close();
            connection.close();
            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * @param poseName 要查找的数据的poseName
     * @return 以 List<Map<String, Object>>形式返回查找到的数据 返回值为 NULL 则为未查找到
     */
    public PoseEntity search(String poseName) {
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String sql = "select * from poseEntity where poseName = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, poseName);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            PoseEntity poseEntity = null;
            while (resultSet.next()) {
                for (int t = 1; t < columnCount; t++) {
                    poseEntity = new PoseEntity();

                    poseEntity.setPoseName(poseName);

                    int poseTYPE = resultSet.getInt("poseType");
                    poseEntity.setPoseType(poseTYPE);

                    int poseProcessValue = resultSet.getInt("poseProcessValue");
                    poseEntity.setPoseProcessValue(poseProcessValue);

                    List<PosePart> monitorPosePartList = new ArrayList<>();
                    String monitorPosePartString = resultSet.getString("monitorPosePartList");

                    JSONArray posePartJSONArray = JSONArray.parseArray(monitorPosePartString);
                    if (posePartJSONArray != null) {
                        for (Object o : posePartJSONArray) {
                            JSONObject key = (JSONObject) o;
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
                    }
                    poseEntity.setMonitorPosePartList(monitorPosePartList);

                    int bestMonitorAngle = resultSet.getInt("bestMonitorAngle");
                    poseEntity.setBestMonitorAngle(bestMonitorAngle);

                    List<String> trainPartList = new ArrayList<>();
                    if (resultSet.getString("trainPartList") != null) {
                        String[] trainPartListString = resultSet.getString("trainPartList").replace("[", "").replace("]", "").split(",");
                        trainPartList.addAll(Arrays.asList(trainPartListString));
                    }
                    poseEntity.setTrainPartList(trainPartList);

                    String videoURL = resultSet.getString("videoURL");
                    poseEntity.setVideoUrl(videoURL);

                    String stepsOfPoseStr = resultSet.getString("stepsOfPose");
                    if (resultSet.getString("stepsOfPose") != null) {
                        String[] stepsOfPose = stepsOfPoseStr.replace("[", "").replace("]", "").split(",");
                        poseEntity.setStepsOfPose(stepsOfPose);
                    }


                    String breathTips = resultSet.getString("breathTips");
                    poseEntity.setBreathTips(breathTips);

                    String poseFeelingStr = resultSet.getString("poseFeeling");
                    if (resultSet.getString("poseFeeling") != null) {
                        String[] poseFeeling = poseFeelingStr.replace("[", "").replace("]", "").split(",");
                        poseEntity.setPoseFeeling(poseFeeling);
                    }

                    String poseFeelingImage = resultSet.getString("poseFeelingImage");
                    poseEntity.setPoseFeelingImage(poseFeelingImage);

                    String commonMistakesStr = resultSet.getString("commonMistakes");
                    if (resultSet.getString("commonMistakes") != null) {
                        String[] commonMistakes = commonMistakesStr.replace("[", "").replace("]", "").split(",");
                        poseEntity.setCommonMistakes(commonMistakes);
                    }

                    String detailTipsStr = resultSet.getString("detailTips");
                    if (resultSet.getString("detailTips") != null) {
                        String[] detailTips = detailTipsStr.replace("[", "").replace("]", "").split(",");
                        poseEntity.setDetailTips(detailTips);
                    }

                    String detailImageUrl = resultSet.getString("detailImageURL");
                    if (resultSet.getString("detailImageURL") != null) {
                        String[] detailImageURL = detailImageUrl.replace("[", "").replace("]", "").split(",");
                        poseEntity.setDetailImageUrl(detailImageURL);
                    }

                    int poseLevel = resultSet.getInt("poseLevel");
                    poseEntity.setPoseLevel(poseLevel);

                    double poseBurnCal = resultSet.getDouble("poseBurnCal");
                    poseEntity.setPoseBurnCal(poseBurnCal);

                    double getPoseBurnCalPerMin = resultSet.getDouble("getPoseBurnCalPerMin");
                    poseEntity.setGetPoseBurnCalPerMin(getPoseBurnCalPerMin);

                    String poseCoverImage = resultSet.getString("poseCoverImage");
                    poseEntity.setPoseCoverImage(poseCoverImage);

                    String poseSwiperImageUrlStr = resultSet.getString("poseSwiperImageUrl");
                    if (resultSet.getString("poseSwiperImageUrl") != null) {
                        String[] poseSwiperImageUrl = poseSwiperImageUrlStr.replace("[", "").replace("]", "").split(",");
                        poseEntity.setPoseSwiperImageUrl(poseSwiperImageUrl);
                    }
                }
            }

            // 关闭连接
            connection.close();
            return poseEntity;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    /**
     * @param poseName     要修改的列的poseName
     * @param variableName 要修改的属性
     * @param object       修改后的属性的值
     */
    public void upDate(String poseName, String variableName, Object object) {
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            if (search(poseName) == null) {
                System.out.println("该动作不存在");
            } else {
                String sql = "update poseEntity set " + variableName + " = ? where poseName = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, object);
                preparedStatement.setObject(2, poseName);
                preparedStatement.executeUpdate();
                System.out.println("修改成功");
            }
            // 关闭连接
            connection.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
