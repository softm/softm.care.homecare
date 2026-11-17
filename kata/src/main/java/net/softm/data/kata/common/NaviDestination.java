package net.softm.data.kata.common;

public enum NaviDestination {
    NONE("-1"),
    BASIC("0"),
    HOME("1"),
    COMPANY("2"),
    FAVORITE("3"),
    DRIVE("4"),
    NOTI("5"),
    APP("9");
    private String id;
    NaviDestination(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static NaviDestination value(String id) {
        NaviDestination rtn = null;
        switch (id) {
            case "1":
                rtn = NaviDestination.HOME;
                break;
            case "2":
                rtn = NaviDestination.COMPANY;
                break;
            case "3":
                rtn = NaviDestination.FAVORITE;
                break;
            case "4":
                rtn = NaviDestination.DRIVE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
