package com.lindum.mlkit.vision.result;

import androidx.annotation.NonNull;

public class Event {
    long time;
    String event;
    int score;          // 训练分数
    int duration;       // 训练时长（秒）
    int errorCount;     // 错误次数
    String poseDataPath; // 训练数据路径

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
    
    public String getPoseDataPath() {
        return poseDataPath;
    }
    
    public void setPoseDataPath(String poseDataPath) {
        this.poseDataPath = poseDataPath;
    }

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "time=" + time +
                ", event='" + event + '\'' +
                ", score=" + score +
                ", duration=" + duration +
                ", errorCount=" + errorCount +
                ", poseDataPath='" + poseDataPath + '\'' +
                '}';
    }
}
