package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum AdViewOPType {
    @SerializedName("0")
    NONE("0"),
    @SerializedName("1")
    ADVIEW_STOP("1"),
    @SerializedName("2")
    ADVIEW("2")
    ;

    private String id;
    AdViewOPType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AdViewOPType value(String id) {
        AdViewOPType rtn = null;
        switch (id) {
            case "0":
                rtn = AdViewOPType.NONE;
                break;
            case "1":
                rtn = AdViewOPType.ADVIEW_STOP;
                break;
            case "2":
                rtn = AdViewOPType.ADVIEW;
                break;
            default:
                break;
        }
        return rtn;
    }
}
