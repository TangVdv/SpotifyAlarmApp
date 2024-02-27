package com.example.spotifyalarm.model;

import java.util.HashMap;

public class SettingsModel {
    private boolean repeat;
    private boolean shuffle;
    private int volume;
    private int stopAlarm;

    public SettingsModel(HashMap<String, Object> settings){
        repeat = settings.containsKey("repeat") && (Boolean) settings.get("repeat");
        shuffle = settings.containsKey("shuffle") && (Boolean) settings.get("shuffle");
        volume = settings.containsKey("volume") ? (Integer) settings.get("volume") : 7;
        stopAlarm = settings.containsKey("stopAlarm") ? (Integer) settings.get("stopAlarm") : 0;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getStopAlarm() {
        return stopAlarm;
    }

    public void setStopAlarm(int stopAlarm) {
        this.stopAlarm = stopAlarm;
    }

    public HashMap<String, Object> getSettingsModelContent(){
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("repeat", this.repeat);
        map.put("shuffle", this.shuffle);
        map.put("volume", this.volume);
        map.put("stopAlarm", this.stopAlarm);

        return map;
    }
}
