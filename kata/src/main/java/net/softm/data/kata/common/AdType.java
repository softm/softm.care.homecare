package net.softm.data.kata.common;

import com.google.gson.annotations.SerializedName;

public enum AdType {
    @SerializedName("0")
    NONE("0"),
    @SerializedName("1")
    BANNER("1"),
    @SerializedName("2")
    INLINE("2"),
    @SerializedName("3")
    INTERSTITIAL("3"),
    @SerializedName("4")
    REWARDED("4"),
    @SerializedName("5")
    REWARDEDINTERSTITIAL("5"),
    @SerializedName("6")
    WAIT_BANNER("6"),
    @SerializedName("7")
    EXIT_BANNER("7")
    ;

    private String id;
    AdType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AdType value(String id) {
        AdType rtn = null;
        switch (id) {
            case "0":
                rtn = AdType.NONE;
                break;
            case "1":
                rtn = AdType.BANNER;
                break;
            case "2":
                rtn = AdType.INLINE;
                break;
            case "3":
                rtn = AdType.INTERSTITIAL;
                break;
            case "4":
                rtn = AdType.REWARDED;
                break;
            case "5":
                rtn = AdType.REWARDEDINTERSTITIAL;
                break;
            case "6":
                rtn = AdType.EXIT_BANNER;
                break;
            default:
                break;
        }
        return rtn;
    }
}
