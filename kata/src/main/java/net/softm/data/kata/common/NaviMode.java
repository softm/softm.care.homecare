package net.softm.data.kata.common;

public enum NaviMode {
    HOMETOCOMPANY("1"),
    HOMETOFAVORITE("2"),
    DRIVE("3")
    ;

    private String id;
    NaviMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static NaviMode value(String id) {
        NaviMode rtn = null;
        switch (id) {
            case "1":
                rtn = NaviMode.HOMETOCOMPANY;
                break;
            case "2":
                rtn = NaviMode.HOMETOFAVORITE;
                break;
            case "3":
                rtn = NaviMode.DRIVE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
