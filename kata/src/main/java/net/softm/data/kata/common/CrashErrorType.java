package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum CrashErrorType {
    @SerializedName("0")
    DEFAULT("0"),
    @SerializedName("1")
    GOOGLE_LOAD_AD("1"),
    @SerializedName("2")
    GOOGLE_PAID("2")
    ;

    private String id;
    CrashErrorType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static CrashErrorType value(String id) {
        CrashErrorType rtn = null;
        switch (id) {
            case "0":
                rtn = CrashErrorType.DEFAULT;
                break;
            case "1":
                rtn = CrashErrorType.GOOGLE_LOAD_AD;
                break;
            case "2":
                rtn = CrashErrorType.GOOGLE_PAID;
                break;
            default:
                break;
        }
        return rtn;
    }
}
