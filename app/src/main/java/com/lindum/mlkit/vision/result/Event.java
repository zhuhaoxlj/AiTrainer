package com.lindum.mlkit.vision.result;

import androidx.annotation.NonNull;

public class Event {
    long time;
    String event;

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

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "time=" + time +
                ", event='" + event + '\'' +
                '}';
    }
}
