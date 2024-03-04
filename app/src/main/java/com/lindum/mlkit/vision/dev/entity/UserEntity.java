package com.lindum.mlkit.vision.dev.entity;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * @author MarkGosling
 * @date 2022/2/15 12:10
 */
public class UserEntity {
    String userUniqueId;
    String nickName;
    String profileImageUrl;
    String region;
    String profile;
    String sex;
    String[] sportsPreference;
    LocalDate birthDay;
    int calGoalValue;
    int stepsGoalValue;
    float height;
    float weight;

    public String getUserUniqueId() {
        return userUniqueId;
    }

    public void setUserUniqueId(String userUniqueId) {
        this.userUniqueId = userUniqueId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String[] getSportsPreference() {
        return sportsPreference;
    }

    public void setSportsPreference(String[] sportsPreference) {
        this.sportsPreference = sportsPreference;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getCalGoalValue() {
        return calGoalValue;
    }

    public void setCalGoalValue(int calGoalValue) {
        this.calGoalValue = calGoalValue;
    }

    public int getStepsGoalValue() {
        return stepsGoalValue;
    }

    public void setStepsGoalValue(int stepsGoalValue) {
        this.stepsGoalValue = stepsGoalValue;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserEntity{" +
                "userUniqueId='" + userUniqueId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", region='" + region + '\'' +
                ", profile='" + profile + '\'' +
                ", sex='" + sex + '\'' +
                ", sportsPreference=" + Arrays.toString(sportsPreference) +
                ", birthDay=" + birthDay +
                ", calGoalValue=" + calGoalValue +
                ", stepsGoalValue=" + stepsGoalValue +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }

    public UserEntity() {

    }
}
