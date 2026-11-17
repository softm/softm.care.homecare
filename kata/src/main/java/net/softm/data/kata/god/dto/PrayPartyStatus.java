package net.softm.data.kata.god.dto;

import com.google.gson.annotations.SerializedName;

/**
 * 진행상태
 */
public enum PrayPartyStatus {
    @SerializedName("0")
    NONE("0"), /** NONE */
    @SerializedName("-1")
    NOTPARTY("-1"), /** 기도 불참 */
    @SerializedName("9")
    DOPARTY("9"), /** 기도 참가 */
    ;

    private String id;
    PrayPartyStatus(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static PrayPartyStatus value(String id) {
        PrayPartyStatus rtn = null;
        switch (id) {
            case "0":
                rtn = PrayPartyStatus.NONE;
                break;
            case "-1":
                rtn = PrayPartyStatus.NOTPARTY;
                break;
            case "9":
                rtn = PrayPartyStatus.DOPARTY;
                break;
            default:
                break;
        }
        return rtn;
    }
}
