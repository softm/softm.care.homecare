package net.softm.data.kata.common;

public enum AppEnable {
    DISABLE("0"),
    ENABLE("1"),
    ;

    private String id;
    AppEnable(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AppEnable value(String id) {
        AppEnable rtn = null;
        switch (id) {
            case "0":
                rtn = AppEnable.DISABLE;
                break;
            case "1":
                rtn = AppEnable.ENABLE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
