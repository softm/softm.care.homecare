package net.softm.data.kata.common;

public enum BillingType {
    /** 평생 5000원 */
    INAPP_LIFETIME("INAPP_ONETIME","inapp_lifetime"),
    /** 월 구독 500원 */
    SUBS("SUBS","subs_01"),
    /** 기부 1000원 : 사용안함 */
    INAPP_CONSUME("INAPP_CONSUME","donate__01"),
    ;

    private String id;
    private String prodcutId;
    BillingType(String id, String prodcutId) {
        this.id = id;
        this.prodcutId = prodcutId;
    }
    public String getId() {
        return id;
    }
    public String getProdcutId() {
        return prodcutId;
    }
    public static BillingType value(String id) {
        BillingType rtn = null;
        switch (id) {
            case "INAPP_CONSUME":
                rtn = BillingType.INAPP_CONSUME;
                break;
            case "INAPP_ONETIME":
                rtn = BillingType.INAPP_LIFETIME;
                break;
            case "SUBS":
                rtn = BillingType.SUBS;
                break;
            default:
                break;
        }
        return rtn;
    }
}
