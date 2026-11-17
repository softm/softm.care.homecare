package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum UserLevel {
    @SerializedName("0")
    FREE("0"),
    @SerializedName("1")
    AD_VIEW("1"),
    @SerializedName("2")
    BETA("2"),
    @SerializedName("8")
    SUBS("8"),
    @SerializedName("9")
    LIFETIME("9"),
    ;

    private String id;
    UserLevel(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static UserLevel value(String id) {
        UserLevel rtn = null;
        switch (id) {
            case "0":
                rtn = UserLevel.FREE;
                break;
            case "1":
                rtn = UserLevel.AD_VIEW;
                break;
            case "8":
                rtn = UserLevel.SUBS;
                break;
            case "9":
                rtn = UserLevel.LIFETIME;
                break;
            default:
                break;
        }
        return rtn;
    }
}
