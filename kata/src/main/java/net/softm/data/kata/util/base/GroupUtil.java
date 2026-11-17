package net.softm.data.kata.util.base;

import android.content.Context;

import com.android.billingclient.api.Purchase;
import com.google.gson.Gson;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.PaidOPType;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.ParamCallback2;
import net.softm.data.kata.common.UserLevel;
import net.softm.data.kata.common.UserType;
import net.softm.data.kata.dto.PurchaseDTO;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.BillingUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.Utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupUtil {
    private static final String TAG = "GroupUtil";

    /** 그룹 마스터 uuid */
    public static List<String> getGroupMasterUUIDs() {
        try {
            return WApplication.basicSettings.groupMasterUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static boolean isGroupMasterUUIDs(String uuid) {
        try {
            return getGroupMasterUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

}

