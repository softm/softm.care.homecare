package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum AdLoadType {
    @SerializedName("1")
    DIRECT("1"),
    @SerializedName("2")
    INDIRECT("2"),
    ;

    private String id;
    AdLoadType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AdLoadType value(String id) {
        AdLoadType rtn = null;
        switch (id) {
            case "1":
                rtn = AdLoadType.DIRECT;
                break;
            case "2":
                rtn = AdLoadType.INDIRECT;
                break;
            default:
                break;
        }
        return rtn;
    }
}
