package com.lindum.mlkit.vision.dev.entity;

import androidx.annotation.NonNull;

import com.lindum.mlkit.vision.dev.posedetector.PosePart;

import java.util.Arrays;
import java.util.List;

/**
 * @author: MarkGosling
 * @date: 2022/1/16 0:40
 **/
public class PoseEntity {
    /**
     * 动作强度
     */
    int poseLevel;
    /**
     * 每次动作消耗的卡路里
     */
    double poseBurnCal;
    /**
     * 每分钟动作消耗卡路里
     */
    double getPoseBurnCalPerMin;
    /**
     * 动作库封面图
     */
    String poseCoverImage;
    /**
     * 动作详情三个面展示
     */
    String[] poseSwiperImageUrl;
    /**
     * 动作名称
     */
    String poseName;
    /**
     * 动作类型 0 为保持性动作  1 为计数性动作
     */
    int poseType;
    /**
     * 动作的过程量  维持秒数  完成次数
     */
    int poseProcessValue;

    /**
     * 需要监测的部位列表
     */
    List<PosePart> monitorPosePartList;
    /**
     * 锻炼的部位 通过这个进行检索针对某个部位的锻炼
     */
    List<String> trainPartList;
    /**
     * 最佳拍摄监测角度
     */
    int bestMonitorAngle;
    // ↓↓↓ 注意事项 ↓↓↓


    /**
     * 视频链接
     */
    String videoUrl;
    /**
     * 动作步骤
     */
    String[] stepsOfPose;
    /**
     * 呼吸要点
     */
    String breathTips;
    /**
     * 动作感觉
     */
    String[] poseFeeling;
    /**
     * 动作感觉配图
     */
    String poseFeelingImage;
    /**
     * 常见错误
     */
    String[] commonMistakes;
    /**
     * 细节图示
     */
    String[] detailImageUrl;
    /**
     * 细节图示文字描述
     */
    String[] detailTips;

    public int getPoseLevel() {
        return poseLevel;
    }

    public void setPoseLevel(int poseLevel) {
        this.poseLevel = poseLevel;
    }

    public double getPoseBurnCal() {
        return poseBurnCal;
    }

    public void setPoseBurnCal(double poseBurnCal) {
        this.poseBurnCal = poseBurnCal;
    }

    public double getGetPoseBurnCalPerMin() {
        return getPoseBurnCalPerMin;
    }

    public void setGetPoseBurnCalPerMin(double getPoseBurnCalPerMin) {
        this.getPoseBurnCalPerMin = getPoseBurnCalPerMin;
    }

    public String[] getPoseSwiperImageUrl() {
        return poseSwiperImageUrl;
    }

    public void setPoseSwiperImageUrl(String[] poseSwiperImageUrl) {
        this.poseSwiperImageUrl = poseSwiperImageUrl;
    }

    public String getPoseCoverImage() {
        return poseCoverImage;
    }

    public void setPoseCoverImage(String poseCoverImage) {
        this.poseCoverImage = poseCoverImage;
    }

    public int getBestMonitorAngle() {
        return bestMonitorAngle;
    }

    public void setBestMonitorAngle(int bestMonitorAngle) {
        this.bestMonitorAngle = bestMonitorAngle;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String[] getStepsOfPose() {
        return stepsOfPose;
    }

    public void setStepsOfPose(String[] stepsOfPose) {
        this.stepsOfPose = stepsOfPose;
    }

    public String getBreathTips() {
        return breathTips;
    }

    public void setBreathTips(String breathTips) {
        this.breathTips = breathTips;
    }

    public String[] getPoseFeeling() {
        return poseFeeling;
    }

    public void setPoseFeeling(String[] poseFeeling) {
        this.poseFeeling = poseFeeling;
    }

    public String getPoseFeelingImage() {
        return poseFeelingImage;
    }

    public void setPoseFeelingImage(String poseFeelingImage) {
        this.poseFeelingImage = poseFeelingImage;
    }

    public String[] getCommonMistakes() {
        return commonMistakes;
    }

    public void setCommonMistakes(String[] commonMistakes) {
        this.commonMistakes = commonMistakes;
    }

    public String[] getDetailImageUrl() {
        return detailImageUrl;
    }

    public void setDetailImageUrl(String[] detailImageUrl) {
        this.detailImageUrl = detailImageUrl;
    }

    public String[] getDetailTips() {
        return detailTips;
    }

    public void setDetailTips(String[] detailTips) {
        this.detailTips = detailTips;
    }

    public int getPoseType() {
        return poseType;
    }

    public void setPoseType(int poseType) {
        this.poseType = poseType;
    }

    public int getPoseProcessValue() {
        return poseProcessValue;
    }

    public void setPoseProcessValue(int poseProcessValue) {
        this.poseProcessValue = poseProcessValue;
    }

    public List<PosePart> getMonitorPosePartList() {
        return monitorPosePartList;
    }

    public String getPoseName() {
        return poseName;
    }

    public void setPoseName(String poseName) {
        this.poseName = poseName;
    }

    public List<PosePart> getMonitoringPosePartList() {
        return monitorPosePartList;
    }

    public void setMonitorPosePartList(List<PosePart> monitorPosePartList) {
        this.monitorPosePartList = monitorPosePartList;
    }

    public List<String> getTrainPartList() {
        return trainPartList;
    }

    @NonNull
    @Override
    public String toString() {
        return "PoseEntity{" +
                "poseLevel=" + poseLevel +
                ", poseBurnCal=" + poseBurnCal +
                ", getPoseBurnCalPerMin=" + getPoseBurnCalPerMin +
                ", poseCoverImage='" + poseCoverImage + '\'' +
                ", poseSwiperImageUrl=" + Arrays.toString(poseSwiperImageUrl) +
                ", poseName='" + poseName + '\'' +
                ", poseType=" + poseType +
                ", poseProcessValue=" + poseProcessValue +
                ", monitorPosePartList=" + monitorPosePartList +
                ", trainPartList=" + trainPartList +
                ", bestMonitorAngle=" + bestMonitorAngle +
                ", videoUrl='" + videoUrl + '\'' +
                ", stepsOfPose=" + Arrays.toString(stepsOfPose) +
                ", breathTips='" + breathTips + '\'' +
                ", poseFeeling=" + Arrays.toString(poseFeeling) +
                ", poseFeelingImage='" + poseFeelingImage + '\'' +
                ", commonMistakes=" + Arrays.toString(commonMistakes) +
                ", detailImageUrl=" + Arrays.toString(detailImageUrl) +
                ", detailTips=" + Arrays.toString(detailTips) +
                '}';
    }

    public void setTrainPartList(List<String> trainPartList) {
        this.trainPartList = trainPartList;
    }
}
