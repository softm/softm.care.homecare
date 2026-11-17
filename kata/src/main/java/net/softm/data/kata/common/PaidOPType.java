package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum PaidOPType {
    @SerializedName("0")
    NONE("0"),
    @SerializedName("1")
    PAID("1")
    ;

    private String id;
    PaidOPType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static PaidOPType value(String id) {
        PaidOPType rtn = null;
        switch (id) {
            case "0":
                rtn = PaidOPType.NONE;
                break;
            case "1":
                rtn = PaidOPType.PAID;
                break;
            default:
                break;
        }
        return rtn;
    }
}
