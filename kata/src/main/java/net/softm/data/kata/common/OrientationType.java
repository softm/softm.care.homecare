package net.softm.data.kata.common;

public enum OrientationType {
    PORTRAIT("PORTRAIT","PORTRAIT"), //PORTRAIT
    PORTRAIT_UPSIDEDOWN("PORTRAIT_UPSIDEDOWN","PORTRAIT_UPSIDEDOWN"), //PORTRAIT
    LANDSCAPE_LEFT("LANDSCAPE_LEFT","LANDSCAPE_LEFT"), //LANDSCAPE_LEFT
    LANDSCAPE_RIGHT("LANDSCAPE_RIGHT","LANDSCAPE_RIGHT"), //LANDSCAPE_RIGHT
    UNDEFINED("UNDEFINED","UNDEFINED"), // UNDEFINED, // LANDSCAPE
    ;

    private String id;
    private String description;
    OrientationType(String id, String description) {
        this.id = id;
        this.description = description;
    }
    public String getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public static OrientationType value(String id) {
        OrientationType rtn = null;
        switch (id) {
            case "PORTRAIT":
                rtn = OrientationType.PORTRAIT;
                break;
            case "PORTRAIT_UPSIDEDOWN":
                rtn = OrientationType.PORTRAIT_UPSIDEDOWN;
                break;
            case "LANDSCAPE_LEFT":
                rtn = OrientationType.LANDSCAPE_LEFT;
                break;
            case "LANDSCAPE_RIGHT":
                rtn = OrientationType.LANDSCAPE_RIGHT;
                break;
            default:
                rtn = OrientationType.UNDEFINED;
                break;
        }
        return rtn;
    }
}
