package net.softm.data.kata.common;

public enum AppRunPoint {
    WORK_EXECUTOR("WORK_EXECUTOR"),
    DIRECT("DIRECT");
    private String id;
    AppRunPoint(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static AppRunPoint value(String id) {
        AppRunPoint rtn = null;
        switch (id) {
            case "WORK_EXECUTOR":
                rtn = AppRunPoint.WORK_EXECUTOR;
                break;
            case "DIRECT":
                rtn = AppRunPoint.DIRECT;
                break;
            default:
                break;
        }
        return rtn;
    }
}
