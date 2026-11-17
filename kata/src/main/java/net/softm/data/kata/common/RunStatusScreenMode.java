package net.softm.data.kata.common;

public enum RunStatusScreenMode {
    NONE("0"),
    ON("1"),
    OFF("2"),
    LOCK("3"),
    ;

    private String id;
    RunStatusScreenMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static RunStatusScreenMode value(String id) {
        RunStatusScreenMode rtn = null;
        switch (id) {
            case "0":
                rtn = RunStatusScreenMode.NONE;
                break;
            case "1":
                rtn = RunStatusScreenMode.ON;
                break;
            case "2":
                rtn = RunStatusScreenMode.OFF;
                break;
            case "3":
                rtn = RunStatusScreenMode.LOCK;
                break;
            default:
                break;
        }
        return rtn;
    }
}
