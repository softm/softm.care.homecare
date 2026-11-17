package net.softm.data.kata.common;

public enum NaviStatus {
    NONE("N"),
    START("S"),
    RUNNING("I"),
    REFENCE("R"),
    END("E")
    ;

    private String id;
    NaviStatus(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static NaviStatus value(String id) {
        NaviStatus rtn = null;
        switch (id) {
            case "N":
                rtn = NaviStatus.NONE;
                break;
            case "S":
                rtn = NaviStatus.START;
                break;
            case "I":
                rtn = NaviStatus.RUNNING;
                break;
            case "R":
                rtn = NaviStatus.REFENCE;
                break;
            case "E":
                rtn = NaviStatus.END;
                break;
            default:
                break;
        }
        return rtn;
    }
}
