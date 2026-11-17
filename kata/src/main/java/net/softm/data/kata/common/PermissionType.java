package net.softm.data.kata.common;

public enum PermissionType {
    LOCATION("LOCATION"),
    BACKGROUND_LOCATION("BACKGROUND_LOCATION"),

    NOTIFICAION_ACCESS("NOTIFICATION_ACCESS"),

    POST_NOTIFICATIONS("POST_NOTIFICATIONS"),
    WRITE_SETTING("WRITE_SETTING"),
    BLUETOOTH("BLUETOOTH"),
    ACCESSIBILITY("ACCESSIBILITY"),

    IGNORE_BATTERY("IGNORE_BATTERY"),
    OVERLAY("OVERLAY"),
    LOCATION_GPS("LOCATION_GPS"),
    USAGESTATS("USAGESTATS"),
    ALARM("ALARM"),
    ;

    private String id;
    PermissionType(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public static PermissionType value(String id) {
        PermissionType rtn = null;
        switch (id) {
            case "LOCATION":
                rtn = PermissionType.LOCATION;
                break;
            case "BACKGROUND_LOCATION":
                rtn = PermissionType.BACKGROUND_LOCATION;
                break;
            case "NOTIFICAION_ACCESS":
                rtn = PermissionType.NOTIFICAION_ACCESS;
                break;
            case "POST_NOTIFICATIONS":
                rtn = PermissionType.POST_NOTIFICATIONS;
                break;
            case "WRITE_SETTING":
                rtn = PermissionType.WRITE_SETTING;
                break;
            case "BLUETOOTH":
                rtn = PermissionType.BLUETOOTH;
                break;
            case "ACCESSIBILITY":
                rtn = PermissionType.ACCESSIBILITY;
                break;
            case "IGNORE_BATTERY":
                rtn = PermissionType.IGNORE_BATTERY;
                break;
            case "OVERLAY":
                rtn = PermissionType.OVERLAY;
                break;
            case "ALARM":
                rtn = PermissionType.ALARM;
                break;
            case "LOCATION_GPS":
                rtn = PermissionType.LOCATION_GPS;
            case "USAGESTATS":
                rtn = PermissionType.USAGESTATS;
                break;
            default:
                break;
        }
        return rtn;
    }
}
