package net.softm.data.kata.common;

import android.os.BatteryManager;

public enum ChargingMode {
//    ALL_CHARGING("1"), // 모두
//    WIRED_CHARGING("2"), // 유선
//    WIRELESS_CHARGING("3"), // 무선

    //15
    ALL_CHARGING( "1",BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB | BatteryManager.BATTERY_PLUGGED_WIRELESS | BatteryManager.BATTERY_PLUGGED_DOCK), // 모두
    // 3
    WIRED_CHARGING("2",BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB ), // 유선
    // 4
    WIRELESS_CHARGING("3",BatteryManager.BATTERY_PLUGGED_WIRELESS), // 무선
    ;

    private String id;
    private int plugged;
    ChargingMode(String id, int plugged) {
        this.id = id;
        this.plugged = plugged;
    }
    public String getId() {
        return id;
    }
    public int getPlugged() {
        return plugged;
    }
    public static ChargingMode value(String id) {
        ChargingMode rtn = null;
        switch (id) {
            case "1":
                rtn = ChargingMode.ALL_CHARGING;
                break;
            case "2":
                rtn = ChargingMode.WIRED_CHARGING;
                break;
            case "3":
                rtn = ChargingMode.WIRELESS_CHARGING;
                break;
            default:
                break;
        }
        return rtn;
    }
}
