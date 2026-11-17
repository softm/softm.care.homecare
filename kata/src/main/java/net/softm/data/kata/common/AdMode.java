package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum AdMode {
    @SerializedName("0")
    NONE("0"),
    @SerializedName("1")
    GOOGLE("1"),
    @SerializedName("2")
    FACEBOOK("2"),
    ;

    private String id;
    AdMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AdMode value(String id) {
        AdMode rtn = null;
        switch (id) {
            case "0":
                rtn = AdMode.NONE;
                break;
            case "1":
                rtn = AdMode.GOOGLE;
                break;
            case "2":
                rtn = AdMode.FACEBOOK;
                break;
            default:
                break;
        }
        return rtn;
    }
}
