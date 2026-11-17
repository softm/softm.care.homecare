package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum AppType {
    @SerializedName("1")
    NAVI("1"),
    @SerializedName("2")
    MUSIC("2"),
    ;

    private String id;
    AppType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AppType value(String id) {
        AppType rtn = null;
        switch (id) {
            case "1":
                rtn = AppType.NAVI;
                break;
            case "2":
                rtn = AppType.MUSIC;
                break;
            default:
                break;
        }
        return rtn;
    }
}
