package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum ReviewOPType {
    @SerializedName("0")
    STOP("0"),
    @SerializedName("1")
    START("1")
    ;

    private String id;
    ReviewOPType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static ReviewOPType value(String id) {
        ReviewOPType rtn = null;
        switch (id) {
            case "0":
                rtn = ReviewOPType.STOP;
                break;
            case "1":
                rtn = ReviewOPType.START;
                break;
            default:
                break;
        }
        return rtn;
    }
}
