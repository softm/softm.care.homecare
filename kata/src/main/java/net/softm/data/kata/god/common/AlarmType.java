package net.softm.data.kata.god.common;

import com.google.gson.annotations.SerializedName;

public enum AlarmType {
    @SerializedName("1")
    AndAllowWhileIdle("1"),
    @SerializedName("2")
    ExactAndAllowWhileIdle("2"),
    @SerializedName("3")
    Exact("3"),
    ;

    private String id;
    AlarmType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AlarmType value(String id) {
        AlarmType rtn = null;
        switch (id) {
            case "1":
                rtn = AlarmType.AndAllowWhileIdle;
                break;
            case "2":
                rtn = AlarmType.ExactAndAllowWhileIdle;
                break;
            case "3":
                rtn = AlarmType.Exact;
                break;
            default:
                break;
        }
        return rtn;
    }
}
