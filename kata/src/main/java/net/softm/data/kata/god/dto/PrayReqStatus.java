package net.softm.data.kata.god.dto;

import com.google.gson.annotations.SerializedName;

/**
 * 진행상태
 */
public enum     PrayReqStatus {
    @SerializedName("0")
    NOT("0"), /** 미신청 */

    @SerializedName("1")
    DONE("1"), /** 신청 */
    ;

    private String id;
    PrayReqStatus(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static PrayReqStatus value(String id) {
        PrayReqStatus rtn = null;
        switch (id) {
            case "0":
                rtn = PrayReqStatus.NOT;
                break;
            case "1":
                rtn = PrayReqStatus.DONE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
