package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum UserType {
    @SerializedName("0")
    FREE_USER("0"),
    @SerializedName("1")
    AD_VIEW_USER("1"),
    @SerializedName("2")
    BETA_USER("2"),
    @SerializedName("9")
    PAID_USER("9"),
    ;

    private String id;
    UserType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static UserType value(String id) {
        UserType rtn = null;
        switch (id) {
            case "0":
                rtn = UserType.FREE_USER;
                break;
            case "1":
                rtn = UserType.AD_VIEW_USER;
                break;
            case "2":
                rtn = UserType.BETA_USER;
                break;
            case "9":
                rtn = UserType.PAID_USER;
                break;
            default:
                break;
        }
        return rtn;
    }
}
