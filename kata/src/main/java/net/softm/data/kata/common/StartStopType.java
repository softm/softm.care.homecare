package net.softm.data.kata.common;

public enum StartStopType {
    NONE("NONE","NONE"), // NONE
    BLUETOOTH("BLUETOOTH","BLUETOOTH"), // bluetooth
    WIFI("WIFI","wifi"), // WIFI
    TETHERING("TETHERING","TETHERING"), // TETHERING
    AIRPLANE_MODE("AIRPLANE_MODE","AIRPLANE_MODE"), // AIRPLANE_MODE
    SCREEN_ON("SCREEN_ON","SCREEN_ON"), // SCREEN_ON
    UNLOCK("UNLOCK","UNLOCK"), // UNLOCK
    PLAY_MUSIC("PLAY_MUSIC","PLAY_MUSIC"), // PLAY_MUSIC
    HOME_SCREEN("HOME_SCREEN","HOME_SCREEN"), // HOME_SCREEN
    POWER_OFF("POWER_OFF","POWER_OFF"), // POWER_OFF

    MOBILE_DATA("MOBILE_DATA","MOBILE_DATA"), // MOBILE_DATA

    ;

    private String id;
    private String description;
    StartStopType(String id, String description) {
        this.id = id;
        this.description = description;
    }
    public String getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public static StartStopType value(String id) {
        StartStopType rtn = null;
        switch (id) {
            case "BLUETOOTH":
                rtn = StartStopType.BLUETOOTH;
                break;
            case "WIFI":
                rtn = StartStopType.WIFI;
                break;
            case "TETHERING":
                rtn = StartStopType.TETHERING;
                break;
            case "AIRPLANE_MODE":
                rtn = StartStopType.AIRPLANE_MODE;
                break;
            case "SCREEN_ON":
                rtn = StartStopType.SCREEN_ON;
                break;
            case "UNLOCK":
                rtn = StartStopType.UNLOCK;
                break;
            case "PLAY_MUSIC":
                rtn = StartStopType.PLAY_MUSIC;
                break;
            case "HOME_SCREEN":
                rtn = StartStopType.HOME_SCREEN;
                break;
            case "POWER_OFF":
                rtn = StartStopType.POWER_OFF;
                break;
            case "MOBILE_DATA":
                rtn = StartStopType.MOBILE_DATA;
                break;
            default:
                rtn = StartStopType.NONE;
                break;
        }
        return rtn;
    }
}
