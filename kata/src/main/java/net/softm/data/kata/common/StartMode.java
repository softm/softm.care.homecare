package net.softm.data.kata.common;

public enum StartMode {
    NONE("0"),
    START("1"),
    WAIT("2"),
    ;

    private String id;
    StartMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static StartMode value(String id) {
        StartMode rtn = null;
        switch (id) {
            case "0":
                rtn = StartMode.NONE;
                break;
            case "1":
                rtn = StartMode.START;
                break;
            case "2":
                rtn = StartMode.WAIT;
                break;
            default:
                break;
        }
        return rtn;
    }
}
