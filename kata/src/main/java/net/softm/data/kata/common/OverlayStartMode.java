package net.softm.data.kata.common;

public enum OverlayStartMode {
    NONE("0"),
    START("1"),
    STOP("2"),
    WAIT("3"),
    MAIN("4");

    private String id;
    OverlayStartMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static OverlayStartMode value(String id) {
        OverlayStartMode rtn = null;
        switch (id) {
            case "0":
                rtn = OverlayStartMode.NONE;
                break;
            case "1":
                rtn = OverlayStartMode.START;
                break;
            case "2":
                rtn = OverlayStartMode.STOP;
                break;
            case "3":
                rtn = OverlayStartMode.WAIT;
                break;
            case "4":
                rtn = OverlayStartMode.MAIN;
                break;
            default:
                break;
        }
        return rtn;
    }
}
