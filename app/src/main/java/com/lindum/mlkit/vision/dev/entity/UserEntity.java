package com.lindum.mlkit.vision.dev.entity;

import java.time.LocalDate;

/**
 * @author MarkGosling
 * @date 2022/2/15 12:10
 */
public class UserEntity {
    String nickName;
    String profileImageUrl;
    String region;
    String profile;
    String sex;
    String sportsPreference;
    LocalDate birthDay;
    float height;
    float weight;

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

    public String getSportsPreference() {
        return sportsPreference;
    }

    public void setSportsPreference(String sportsPreference) {
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

    @Override
    public String toString() {
        return "UserEntity{" +
                "nickName='" + nickName + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", region='" + region + '\'' +
                ", profile='" + profile + '\'' +
                ", sex='" + sex + '\'' +
                ", sportsPreference='" + sportsPreference + '\'' +
                ", birthDay=" + birthDay +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }


    public UserEntity(String nickName, String profileImageUrl, String region, String profile, String sex, String sportsPreference, LocalDate birthDay, float height, float weight) {
        this.nickName = nickName;
        this.profileImageUrl = profileImageUrl;
        this.region = region;
        this.profile = profile;
        this.sex = sex;
        this.sportsPreference = sportsPreference;
        this.birthDay = birthDay;
        this.height = height;
        this.weight = weight;
    }

    public UserEntity() {

    }
}
