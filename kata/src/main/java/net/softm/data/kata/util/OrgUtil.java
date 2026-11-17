package net.softm.data.kata.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.OrgConst;

import java.lang.reflect.Type;
import java.util.List;

public class OrgUtil {
    private static final String TAG = "OrgUtil";
    private Context mContext;

    public static List<String> getGroups() {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(OrgConst.PREF_KEY_GROUPS,"[]"), listType);
    }

    public static void setGroups(String v) {
        WApplication.mInstance.setSharedPreferences(OrgConst.PREF_KEY_GROUPS,v);
    }

    public static String getOrg() {
        return WApplication.mInstance.getSharedPreferences(OrgConst.PREF_KEY_ORG,"");
    }

    public static void setOrg(String v) {
        WApplication.mInstance.setSharedPreferences(OrgConst.PREF_KEY_ORG,v);
    }

    public static String getOrgReg() {
        return WApplication.mInstance.getSharedPreferences(OrgConst.PREF_KEY_ORG_REG,"");
    }

    public static void setOrgReg(String v) {
        WApplication.mInstance.setSharedPreferences(OrgConst.PREF_KEY_ORG_REG,v);
    }

    public static String getProfileName() {
        return WApplication.mInstance.getSharedPreferences(OrgConst.PREF_KEY_PROFILE_NAME,"");
    }

    public static void setProfileName(String v) {
        WApplication.mInstance.setSharedPreferences(OrgConst.PREF_KEY_PROFILE_NAME,v);
    }
    public static String getNickName() {
        return WApplication.mInstance.getSharedPreferences(OrgConst.PREF_KEY_NICK_NAME,"");
    }

    public static void setNickName(String v) {
        WApplication.mInstance.setSharedPreferences(OrgConst.PREF_KEY_NICK_NAME,v);
    }

    public static String getUuid() {
        if ( WApplication.basicInfoDTO != null ) {
            return WApplication.basicInfoDTO.uuid;
        } else {
            return "";
        }
    }
}

