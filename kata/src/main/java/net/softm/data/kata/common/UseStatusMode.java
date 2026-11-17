package net.softm.data.kata.common;

public enum UseStatusMode {
    NONE("0"),
    ENABLE("1"),
    DISABLE("2"),
    ;

    private String id;
    UseStatusMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static UseStatusMode value(String id) {
        UseStatusMode rtn = null;
        switch (id) {
            case "0":
                rtn = UseStatusMode.NONE;
                break;
            case "1":
                rtn = UseStatusMode.ENABLE;
                break;
            case "2":
                rtn = UseStatusMode.DISABLE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
