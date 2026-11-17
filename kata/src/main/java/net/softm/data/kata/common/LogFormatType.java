package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum LogFormatType {
    @SerializedName("1")
    DEFAULT("1"),
    @SerializedName("2")
    SHORT("2"),
    ;

    private String id;
    LogFormatType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static LogFormatType value(String id) {
        LogFormatType rtn = null;
        switch (id) {
            case "1":
                rtn = LogFormatType.DEFAULT;
                break;
            case "2":
                rtn = LogFormatType.SHORT;
                break;
            default:
                break;
        }
        return rtn;
    }
}
