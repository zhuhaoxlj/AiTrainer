package com.lindum.mlkit.vision.dev.sql;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lindum.mlkit.vision.dev.entity.UserEntity;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YipJing
 * @date 2022/2/16 14:27
 */
public class UpdateUserInfo {

    /**
     * 读取resultSet中的userEntity
     *
     * @param resultSet 导入需要读取的resultSet
     * @return 返回读取到的userEntity
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private UserEntity getUserEntity(ResultSet resultSet) {
        try {
            // 建立连接
            UserEntity userEntity = new UserEntity();

            String nickName = resultSet.getString("nickName");
            userEntity.setNickName(nickName);

            String profileImageUrl = resultSet.getString("profileImageUrl");
            userEntity.setProfileImageUrl(profileImageUrl);

            String region = resultSet.getString("region");
            userEntity.setRegion(region);

            String profile = resultSet.getString("profile");
            userEntity.setProfile(profile);

            String sex = resultSet.getString("sex");
            userEntity.setSex(sex);

            String sportsPreferenceStr = resultSet.getString("sportsPreference");

            if (resultSet.getString("sportsPreference") != null) {
                String[] sportsPreference = sportsPreferenceStr.replace("[", "").replace("]", "").split(",");
                userEntity.setSportsPreference(sportsPreference);
            }

            if(resultSet.getString("birthDay") != null) {
                String birthDayStr = resultSet.getString("birthDay");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthDay = LocalDate.parse(birthDayStr, dateTimeFormatter);
                userEntity.setBirthDay(birthDay);
            }

            float height = resultSet.getFloat("height");
            userEntity.setHeight(height);

            float weight = resultSet.getFloat("weight");
            userEntity.setWeight(weight);

            String userUniqueId = resultSet.getString("userUniqueId");
            userEntity.setUserUniqueId(userUniqueId);

            int calGoalValue = resultSet.getInt("calGoalValue");
            userEntity.setCalGoalValue(calGoalValue);

            int stepsGoalValue = resultSet.getInt("stepsGoalValue");
            userEntity.setStepsGoalValue(stepsGoalValue);
            // 关闭连接
            return userEntity;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

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
     *
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeInJson(String jsonPath) {
        PreparedStatement preparedStatement = null;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String s = readJsonFile(jsonPath);
            JSONObject jsonObject = JSON.parseObject(s);
            JSONArray poseEntity = jsonObject.getJSONArray("userEntities");
            String sql;
            for (Object o : poseEntity) {
                JSONObject key = (JSONObject) o;

                //1
                String nickName = (String) key.get("nickName");
                //2
                String profileImageUrl = (String) key.get("profileImageUrl");
                //3
                String region = (String) key.get("region");
                //4
                String profile = (String) key.get("profile");
                //5
                String sex = (String) key.get("sex");
                //6
                String sportsPreference = (String) key.get("sportsPreference");
                //7
                LocalDate birthDay = (LocalDate) key.get("birthDay");
                //8
                Float height = (Float) key.get("height");
                //9
                Float weight = (Float) key.get("weight");

                sql = "INSERT  INTO userEntity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, nickName);

                preparedStatement.setString(2, profileImageUrl);

                preparedStatement.setString(3, region);

                preparedStatement.setString(4, profile);

                preparedStatement.setString(5, sex);

                preparedStatement.setString(6, sportsPreference);

                preparedStatement.setString(7, birthDay.toString());

                preparedStatement.setFloat(8, height);

                preparedStatement.setFloat(9, weight);

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
     * @param userEntity 写入的poseEntity实体类
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeInClass(UserEntity userEntity) {
        PreparedStatement preparedStatement;
        String sql;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String jsonStr = JSONObject.toJSONString(userEntity);
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            //1
            String userUniqueId = (String) jsonObject.get("userUniqueId");
            if (select(userUniqueId) == 0) {
                //2
                String profileImageUrl = (String) jsonObject.get("profileImageUrl");
                //3
                String region = (String) jsonObject.get("region");
                //4
                String profile = (String) jsonObject.get("profile");
                //5
                String sex = (String) jsonObject.get("sex");
                //6
                JSONArray sportsPreference = (JSONArray) jsonObject.get("sportsPreference");
                //7
                Object birthDayObject = jsonObject.get("birthDay");
                LocalDate birthDay;
                if (birthDayObject != null) {
                    birthDay = LocalDate.parse(birthDayObject.toString());
                }else {
                    birthDay = null;
                }
                //8
                Object heightObject = jsonObject.get("height");
                float height = Float.parseFloat(heightObject.toString());
                //9
                Object weightObject = jsonObject.get("weight");
                float weight = Float.parseFloat(weightObject.toString());
                //10
                String nickName = (String) jsonObject.get("nickName");
                //11
                int calGoalValue = jsonObject.getIntValue("calGoalValue");
                //12
                int stepsGoalValue = jsonObject.getIntValue("stepsGoalValue");

                sql = "INSERT  INTO userEntity VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)";
                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, userUniqueId);

                preparedStatement.setString(2, profileImageUrl);

                preparedStatement.setString(3, region);

                preparedStatement.setString(4, profile);

                preparedStatement.setString(5, sex);

                if (sportsPreference != null) {
                    preparedStatement.setString(6, sportsPreference.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    preparedStatement.setString(6, null);
                }

                if (birthDay != null) {
                    preparedStatement.setString(7, birthDay.toString());
                } else {
                    preparedStatement.setString(7, null);
                }

                preparedStatement.setFloat(8, height);

                preparedStatement.setFloat(9, weight);

                preparedStatement.setString(10, nickName);

                preparedStatement.setInt(11, calGoalValue);

                preparedStatement.setInt(12, stepsGoalValue);

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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<UserEntity> readTable() {
        List<UserEntity> userEntityList = new ArrayList<>();
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String sql = "select * from userEntity";
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userEntityList.add(getUserEntity(resultSet));
            }
            // 关闭连接
            preparedStatement.close();
            connection.close();
            System.out.println("success");
            return userEntityList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     *
     * 删除函数
     * @param userUniqueId 要删除的列的userUniqueId
     * @return 返回删除结果 1为删除成功
     */
    public int delete(String userUniqueId) {
        PreparedStatement preparedStatement;

        String sql;
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            sql = "delete from userEntity where userUniqueId = " + "\"" + userUniqueId + "\"";
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
     *
     * @param userUniqueId 要查找的数据的 userUniqueId
     * @return 返回找到的列所对应的实体对象
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public UserEntity search(String userUniqueId) {
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String sql = "select * from userEntity where userUniqueId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,userUniqueId);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            UserEntity userEntity = new UserEntity();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    userEntity = getUserEntity(resultSet);
                }
            }
            // 关闭连接
            connection.close();
            return userEntity;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    /**
     *
     * @param userUniqueId 要查找的数据的 userUniqueId
     * @return 返回查找到的行数
     */
    public int select(String userUniqueId) {
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            String sql = "select * from userEntity where userUniqueId = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1,userUniqueId);
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                count++;
            }
            // 关闭连接
            connection.close();
            return count;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     *
     * @param userUniqueId 要修改的列的 userUniqueId
     * @param variableName 要修改的属性
     * @param object 修改后的属性的值
     */
    public void upDate(String userUniqueId, String variableName, Object object) {
        try {
            // 建立连接
            Connection connection = DriverManager.getConnection("jdbc:mysql://47.102.122.203:3306/aitrainer", "aitrainer", "jay666..");
            if (select(userUniqueId) == 0) {
                System.out.println("该动作不存在");
            } else {
                String sql = "update userEntity set " + variableName + " = ? where userUniqueId = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1,object);
                preparedStatement.setObject(2,userUniqueId);
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
