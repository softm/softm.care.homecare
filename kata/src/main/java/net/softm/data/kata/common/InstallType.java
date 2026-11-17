package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum InstallType {
    @SerializedName("0")
    NEW_INSTALL("0"),
    @SerializedName("1")
    RE_INSTALL("1"),
    @SerializedName("2")
    UPDATE_INSTALL("2"),
    ;

    private String id;
    InstallType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static InstallType value(String id) {
        InstallType rtn = null;
        switch (id) {
            case "0":
                rtn = InstallType.NEW_INSTALL;
                break;
            case "1":
                rtn = InstallType.RE_INSTALL;
                break;
            case "2":
                rtn = InstallType.UPDATE_INSTALL;
                break;
            default:
                break;
        }
        return rtn;
    }
}
