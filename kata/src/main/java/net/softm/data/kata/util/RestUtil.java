package net.softm.data.kata.util;

import android.content.Context;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdMode;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.PaidOPType;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.BasicInfoDTO;
import net.softm.data.kata.dto.base.BasicSettings;
import net.softm.data.kata.kt.Callkt;

public class RestUtil {
    private static final String TAG = "RestUtil";

    public static void getBasicInfo(Context context) {
        getBasicInfo(context,null);
    }

    public static void getBasicInfo(Context context,Runnable runnable) {
        getBasicInfo(context,runnable,true);
    }

    public static void getBasicInfo(Context context,Runnable runnable, boolean startCheckService) {
        new Callkt("getBasicInfo").runIO((param)->{
//        new Thread(() -> {
            try {
                BasicInfoDTO basicInfoDTO = null;
                try {
                    basicInfoDTO = Utils.getJsonFromServer(WUrl.ReqId.BASIC_INFO.getReqUrl(), BasicInfoDTO.class);
                } catch ( Exception checkEx ) {
                    L.e(TAG,"getBasicInfo.Utils.getJsonFromServer",checkEx);
                    basicInfoDTO = null;
//                    WApplication.basicInfoDTO.adViewLicenseAddMaxTime;
                } finally {
                    WApplication.mInstance.basicInfoDTO = basicInfoDTO == null ? new BasicInfoDTO(context): basicInfoDTO;
                    WApplication.mInstance.basicInfoDTO.uuid = DeviceUtil.getUUID(context);

//                    if ( WApplication.basicInfoDTO != null ) {
                        if (Utils.isNotEmpty(LicenseUtil.getForceAdViewOPTypeAdViewUUIDs()) && LicenseUtil.isForceAdViewOPTypeAdViewUUIDs(WApplication.basicInfoDTO.uuid)) {
                            LicenseUtil.setAdViewOPType(AdViewOPType.ADVIEW);
                            AdUtil.setAdMode(AdUtil.getDefaultAdMode());
                        } else if (Utils.isNotEmpty(LicenseUtil.getForceAdViewOPTypeAdViewStopUUIDs()) && LicenseUtil.isForceAdViewOPTypeAdViewStopUUIDs(WApplication.basicInfoDTO.uuid)) {
                            LicenseUtil.setAdViewOPType(AdViewOPType.ADVIEW_STOP);
                            AdUtil.setAdMode(AdUtil.getDefaultAdMode());
                        }

                        if (Utils.isNotEmpty(LicenseUtil.getForcePaidOPTypeUUIDs()) && LicenseUtil.isForcePaidOPTypeUUIDs(WApplication.basicInfoDTO.uuid)) {
                            LicenseUtil.setPaidOPType(PaidOPType.PAID);
                        }

                        if (Utils.isNotEmpty(AdUtil.getForceAdModeUUIDs()) && AdUtil.getForceAdModeUUIDs().contains(WApplication.basicInfoDTO.uuid)) {
                            AdUtil.setAdMode(AdUtil.getDefaultAdMode());
                        }

                        /** 광고 보기 라이센스 모드가 있는데 광고모드가 없으면 */
                        if (
                            AdViewOPType.ADVIEW.equals( LicenseUtil.getAdViewOPType() ) || AdViewOPType.ADVIEW_STOP.equals( LicenseUtil.getAdViewOPType() ) &&
                            AdMode.NONE.equals(AdUtil.getAdMode())
                        ) {
                            AdUtil.setAdMode(AdUtil.getDefaultAdMode());
                        }

                        /**
                         * 배너 로딩 비정상 종료 발생시 AdMode.NONE - 20250308
                         * */
                        if ( AdUtil.isGoogleAdViewErrorOccurred() ) {
                            AdUtil.setAdMode(AdMode.NONE);
                        }

                        /**
                         * 결제 비정상 종료 발생시 PaidOPType.NONE - 20250313
                         * */
                        if ( LicenseUtil.isGooglePaidErrorOccurred() ) {
                            LicenseUtil.setPaidOPType(PaidOPType.NONE);
                        }

                        if ( !LicenseUtil.isTestUuids(WApplication.basicInfoDTO.uuid) ) {
                            if ( AdUtil.isExceptAdModeUUIDs(WApplication.basicInfoDTO.uuid) ) {
                                AdUtil.setAdMode(AdMode.NONE);
                            }
                        }

                        if ( LicenseUtil.isExceptPaidUUIDs(WApplication.basicInfoDTO.uuid) ) {
                            LicenseUtil.setPaidOPType(PaidOPType.NONE);
                        }
//                    }
                    LicenseUtil.fixUserType();

//                  if ( AdUtil.isAdLicense() ) {
//                      WApplication.mInstance.basicInfoDTO.userType = UserType.LICENSE_USER;
//                  } else
//                    if (UserLevel.LIFETIME  LicenseUtil.getUserType() ) {
                }

                if ( runnable != null ) {
                    Utils.runExecutorService(runnable);
                }

                if (basicInfoDTO != null) { /** BASIC_INFO url에서 직접 데이터가 수신된경우 */
                    RestUtil.setServerConnected(true);
                }
                /** checkService on/off */
                if ( startCheckService ) {
                    if (basicInfoDTO != null) { /** BASIC_INFO url에서 직접 데이터가 수신된경우 */
                        WorkerUtil.cancelCheckService(context);
                        if (Utils.hasPermissionForBasic(context) && MapUtil.isAppInstalled()) { /** 설치 첫 페이지 지나면.... */
                            if (
                                (
                                  /** 사용자 단위 설정
                                    1. {@link net.softm.startnavi.poweron.dto.BasicInfoDTO#checkService} === true 활성화 &&
                                        : checkService를 false로 설정시 아래의 두가지 사용자 설정 무효화
                                    2. checkServiceUUIDs 있거나, 연구소 checkServiceForUser가 활성화
                                        2-1. {@link net.softm.startnavi.poweron.dto.BasicInfoDTO#checkServiceUUIDs} Exists DeviceUtil.getUUID(MainActivity.this)
                                        2-2. 연구소 설정 - "체크서비스" 켜짐 => {@link MapUtil#isCheckServiceForUser()}
                                  */
                                    CheckServiceUtil.isCheckService() &&
                                    (
                                        CheckServiceUtil.isCheckServiceUUIDs(DeviceUtil.getUUID(context))
                                        ||
                                        MapUtil.isCheckServiceForUser()
                                    )
                                )
                                ||
                                 /** 전체 설정
                                 3. {@link net.softm.startnavi.poweron.dto.BasicInfoDTO#checkServiceAllEnable} === true 무조건 활성화
                                     : checkServiceAllEnable == true 이면 무조건 활성화
                                 */
                                CheckServiceUtil.isCheckServiceAllEnable()
                            ) {
                                if ( ServiceUtil.isStartAble() ) {
                                    WorkerUtil.startCheckService(context);
                                }
                            }
                        }
                    }
                }
//                runOnUiThread(() -> {
//                    ViewGroup adViewContainer = findViewById(R.id.ad_view_container);
//                    AdUtil.addBanner(adViewContainer,MainActivity.this);
//                });
            } catch (Exception e) {
//                e.printStackTrace();
                L.e(TAG,"getBasicInfo",e);
                WApplication.basicInfoDTO = new BasicInfoDTO(context);
            }
//        }).start();
            return null;
        });
    }

    private static void setServerConnected(boolean b) {
        try {
            WApplication.basicInfoDTO.serverConnected = b;
        } catch (Exception ex) {
        }
    }

    /** 서버가 살아있는지 여부 : 서버 루트에 basic_info.json이 존재하는지 여부 */
    public static boolean isServerConnected() {
        try {
            return WApplication.basicInfoDTO.serverConnected;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void getBasicSettings(Context context) {
        getBasicSettings(context,null);
    }

    public static void getBasicSettings(Context context,Runnable runnable) {
        getBasicSettings(context,runnable,true);
    }

    public static void getBasicSettings(Context context,Runnable runnable, boolean startCheckService) {
        if ( Utils.isEmpty(OrgUtil.getOrg()) ) return;

        new Callkt("getBasicSettings").runIO((param)->{
//        new Thread(() -> {
            try {
                BasicSettings basicSettings = null;
                try {
                    basicSettings = Utils.getJsonFromServer(WUrl.ReqId.BASIC_SETTINGS.getReqUrl(), BasicSettings.class);
                } catch ( Exception checkEx ) {
                    L.e(TAG,"BasicSettings.Utils.getJsonFromServer",checkEx);
                    basicSettings = null;
//                    WApplication.basicInfoDTO.adViewLicenseAddMaxTime;
                } finally {
                    WApplication.mInstance.basicSettings = basicSettings == null ? new BasicSettings(context): basicSettings;
                }

                if ( runnable != null ) {
                    Utils.runExecutorService(runnable);
                }

                /** checkService on/off */
                if ( startCheckService ) {

                }
            } catch (Exception e) {
//                e.printStackTrace();
                L.e(TAG,"getBasicInfo",e);
                WApplication.basicSettings = new BasicSettings(context);
            }
//        }).start();
            return null;
        });
    }


}

