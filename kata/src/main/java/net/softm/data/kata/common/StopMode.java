package net.softm.data.kata.common;

public enum StopMode {
    NONE("0"),
    FORCE("1"),
    RECENT("2"),
    RECENT_ALL("3")
    ;

    private String id;
    StopMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static StopMode value(String id) {
        StopMode rtn = null;
        switch (id) {
            case "0":
                rtn = StopMode.NONE;
                break;
            case "1":
                rtn = StopMode.FORCE;
                break;
            case "2":
                rtn = StopMode.RECENT;
                break;
            case "3":
                rtn = StopMode.RECENT_ALL;
                break;
            default:
                break;
        }
        return rtn;
    }
}
