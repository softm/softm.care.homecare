package net.softm.data.kata.god.dto;

import com.google.gson.annotations.SerializedName;

/**
 * 진행상태
 */
public enum PrayStatus {
    @SerializedName("0")
    BEFORE("0"), /** 대기중 */
    @SerializedName("1")
    ING("1"), /** 예배중 */
    @SerializedName("9")
    END("9"), /** 예배종료 */
    ;

    private String id;
    PrayStatus(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static PrayStatus value(String id) {
        PrayStatus rtn = null;
        switch (id) {
            case "0":
                rtn = PrayStatus.BEFORE;
                break;
            case "1":
                rtn = PrayStatus.ING;
                break;
            case "9":
                rtn = PrayStatus.END;
                break;
            default:
                break;
        }
        return rtn;
    }
}
