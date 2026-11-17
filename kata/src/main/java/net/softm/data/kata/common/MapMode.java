package net.softm.data.kata.common;

public enum MapMode {
    TMAP("1"),
    GOOGLE("2")
    ;

    private String id;
    MapMode(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static MapMode value(String id) {
        MapMode rtn = null;
        switch (id) {
            case "1":
                rtn = MapMode.TMAP;
                break;
            case "2":
                rtn = MapMode.GOOGLE;
                break;
            default:
                break;
        }
        return rtn;
    }
}
