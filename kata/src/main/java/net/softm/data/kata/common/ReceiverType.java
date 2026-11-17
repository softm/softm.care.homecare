package net.softm.data.kata.common;

public enum ReceiverType {
    NONE("NONE","NONE"), //NONE
    POWER("POWER","Power"), // 전원
    BLUETOOTH("BLUETOOTH","Bluetooth"), // BLUETOOTH
    WIFI("WIFI","Wifi"), // WIFI
    ;

    private String id;
    private String description;
    ReceiverType(String id,String description) {
        this.id = id;
        this.description = description;
    }
    public String getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public static ReceiverType value(String id) {
        ReceiverType rtn = null;
        switch (id) {
            case "POWER":
                rtn = ReceiverType.POWER;
                break;
            case "BLUETOOTH":
                rtn = ReceiverType.BLUETOOTH;
                break;
            case "WIFI":
                rtn = ReceiverType.WIFI;
                break;
            case "NONE":
                rtn = ReceiverType.NONE;
                break;
            default:
                rtn = ReceiverType.NONE;
                break;
        }
        return rtn;
    }
}
