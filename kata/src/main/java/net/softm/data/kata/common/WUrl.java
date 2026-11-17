package net.softm.data.kata.common;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.OrgUtil;
import net.softm.data.kata.util.Utils;
import net.softm.lib.common.Connectivity;

public class WUrl {

    public static enum EnumDevMode {
        DEBUG,
        RDEBUG,
        DEV,
        LOCAL,
        LOCAL_ASSET,
        RELEASE,
        DEV_LOCAL,
    }
//    public static EnumDevMode devMode = EnumDevMode.LOCAL1;
//    public static EnumDevMode devMode = EnumDevMode.RELEASE;
//    public static EnumDevMode devMode = EnumDevMode.DEBUG;
//    public static EnumDevMode devMode = EnumDevMode.LOCAL2;
//      public static EnumDevMode devMode = EnumDevMode.DEV;
      public static EnumDevMode devMode = WUrl.EnumDevMode.valueOf(BuildConfig.BUILD_TYPE.toUpperCase());
//    static EnumDevMode devMode = EnumDevMode.DEV2;
//    static EnumDevMode devMode = EnumDevMode.OP;
    public static  String ADDR_DEV     = "http://www.softm.net/gdev"      ;//
    public static  String ADDR_DEBUG     = "http://www.softm.net/g"      ;//
    public static  String ADDR_LOCAL_ASSET  = "file:///android_asset"      ;//
    public static  String ADDR_LOCAL  = "http://127.0.0.1:5500"      ;//
    public static  String ADDR_RELEASE = "https://softm.github.io/g" + String.valueOf(BuildConfig.VERSION_CODE);//운영

      static {
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        if ( WUrl.devMode == WUrl.EnumDevMode.RELEASE || WUrl.devMode == WUrl.EnumDevMode.DEBUG ) {
            Const.SERVER_HOST = "https://softm.github.io";
            Const.PATH_NAME = "kata" + versionCode;
        } else if ( WUrl.devMode == EnumDevMode.LOCAL  ) {
//            Const.SERVER_HOST = "http://127.0.0.1:5500";
            Const.SERVER_HOST = "http://127.0.0.1:3000";
            Const.PATH_NAME = "";
        } else {
            Const.SERVER_HOST = "http://softm.net/katadev";
            Const.PATH_NAME = "";
        }

        ADDR_DEV = "http://softm.net/katadev";
//        ADDR_LOCAL = "http://127.0.0.1:5500";
        ADDR_LOCAL = "http://127.0.0.1:3000";
//        ADDR_DEBUG = "https://softm.github.io/kata" + versionCode;
//        ADDR_RELEASE = "https://softm.github.io/kata" + versionCode;
        ADDR_DEBUG = "https://softm.github.io/kata";
        ADDR_RELEASE = "https://softm.github.io/kata";
      }

    private static final String TAG = WUrl.class.getSimpleName();
    public static final String TIME_URL = "";


    //mWebView.loadUrl("http://127.0.0.1/loan/index.jsp");
//		mWebView.loadUrl("http://www.naver.com");
    //mWebView.loadUrl("http://192.168.219.135:8080/index.jsp");
    public static String mBaseUrl = null;

    public static ReqId value(String id) {
        ReqId rtn = null;
        switch (id) {
            case "BASIC_INFO":
                rtn = ReqId.BASIC_INFO;
                break;
            case "WEB_STATE":
                rtn = ReqId.WEB_STATE;
                break;
            case "WEB_MAIN":
                rtn = ReqId.WEB_MAIN;
                break;
            case "WEB_START":
                rtn = ReqId.WEB_START;
                break;
            case "WEB_PERMISSION":
                rtn = ReqId.WEB_PERMISSION;
                break;
            case "WEB_PERMISSION_GUIDE":
                rtn = ReqId.WEB_PERMISSION_GUIDE;
                break;
            case "WEB_ERROR":
                rtn = ReqId.WEB_ERROR;
                break;
            case "WEB_SEARCH":
                rtn = ReqId.WEB_SEARCH;
                break;
            case "WEB_LABORATORY":
                rtn = ReqId.WEB_LABORATORY;
                break;
            case "WEB_INDEX":
                rtn = ReqId.WEB_INDEX;
                break;
            case "WEB_SETTING":
                rtn = ReqId.WEB_SETTING;
                break;
            case "WEB_RUN_OPTION":
                rtn = ReqId.WEB_RUN_OPTION;
                break;
            case "WEB_ONOFF":
                rtn = ReqId.WEB_ONOFF;
                break;
            case "WEB_DEVELOPER_SUPPORT":
                rtn = ReqId.WEB_DEVELOPER_SUPPORT;
                break;
            case "WEB_QNA":
                rtn = ReqId.WEB_QNA;
                break;
            case "WEB_HOME_COMPANY":
                rtn = ReqId.WEB_HOME_COMPANY;
                break;
            case "WEB_REG_POI":
                rtn = ReqId.WEB_REG_POI;
                break;
            case "WEB_VERSION":
                rtn = ReqId.WEB_VERSION;
                break;
            case "WEB_NOTICE":
                rtn = ReqId.WEB_NOTICE;
                break;
            case "WEB_APP_IRON":
                rtn = ReqId.WEB_APP_IRON;
                break;
            case "WEB_GET_ENC_KEY":
                rtn = ReqId.WEB_GET_ENC_KEY;
                break;
            case "GET_CERT_URL":
                rtn = ReqId.GET_CERT_URL;
                break;
            case "WEB_PRAY":
                rtn = ReqId.WEB_PRAY;
                break;
            case "WEB_PROFILE":
                rtn = ReqId.WEB_PROFILE;
                break;
            default:
                break;
        }
        return rtn;
    }

    //    http://192.168.159.1:9080/co/CO000000.do
    public static enum ReqId {
        BASIC_INFO ("basic_info.json"),
        BASIC_SETTINGS ("basic_settings_"+ OrgUtil.getOrg() +".json"),
        WEB_STATE ("status.json"),
//        WEB_MAIN ("index.html"),
        WEB_MAIN (""),
        WEB_INSTALLED ("installed.html"),
//        WEB_START ("start.html"),
        WEB_START (""),
        WEB_PERMISSION ("permission.html"),
        WEB_PERMISSION_GUIDE ("permission_guide.html"),
        WEB_ERROR ("error.html"),
        WEB_VERSION (""),
        WEB_NOTICE (""),
        WEB_APP_IRON (""),
        WEB_GET_ENC_KEY (""),
        GET_CERT_URL (""),
        WEB_SEARCH ("search.html"),

        WEB_LABORATORY ("laboratory.html"),
        WEB_INDEX ("index.html"),
        WEB_SETTING ("setting.html"),
        WEB_RUN_OPTION ("run_option.html"),
        WEB_ONOFF ("onoff.html"),
        WEB_QNA ("qna.html"),
        WEB_HOME_COMPANY ("home_company.html"),
        WEB_DEVELOPER_SUPPORT ("developer_support.html"),
        WEB_REG_POI ("reg_poi.html"),
        WEB_PRAY ("pray.html"),
        WEB_PROFILE ("profile.html"),
        ;

        private String mUrl;

        public String getUrl() {
            return mUrl;
        }

        ReqId(String url){
            mUrl = url;
        }
        long versionCode = BuildConfig.VERSION_CODE;
        public String getReqUrl() {

            String rtn = null;
            switch (devMode) {
                case LOCAL:
//                    if ( this.name().equals(ReqId.WEB_START.name()) || this.name().equals(ReqId.WEB_PERMISSION.name()) ||
                    if ( this.name().equals(ReqId.WEB_PERMISSION.name()) ||
                            this.name().equals(ReqId.WEB_PERMISSION_GUIDE.name()) || this.name().equals(ReqId.WEB_ERROR.name())
                    ) { /** 로컬서버 설정으로 서버이용 : 설치시 무조건 로컬파일 */
                        mBaseUrl = WUrl.ADDR_LOCAL;
                    } else if (
                            this.name().equals(ReqId.BASIC_INFO.name()) || this.name().equals(ReqId.BASIC_SETTINGS.name()) || this.name().equals(ReqId.WEB_STATE.name()) ||
                                    this.name().equals(ReqId.WEB_SEARCH.name()) || this.name().equals(ReqId.WEB_HOME_COMPANY.name())
                    ) {
                        mBaseUrl = WUrl.ADDR_LOCAL;
                    } else if ( this.name().equals(ReqId.WEB_DEVELOPER_SUPPORT.name()) ) {
                        if ( Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance) ) {
                            mBaseUrl = WUrl.ADDR_LOCAL;
                        } else {
                            mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        }
                    } else { /** 로컬서버 설정으로 서버이용 */
                        // if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useServer ) {
//                        if (Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance)) {
                            mBaseUrl = WUrl.ADDR_LOCAL;
//                        } else {
//                            mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
//                        }
                        // } else {
                        //     mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        // }
                    }
                    rtn = mBaseUrl + "/" + mUrl;
                    break;
                case LOCAL_ASSET:
                    if ( this.name().equals(ReqId.BASIC_INFO.name()) || this.name().equals(ReqId.BASIC_SETTINGS.name()) || this.name().equals(ReqId.WEB_STATE.name()) ||
                         this.name().equals(ReqId.WEB_SEARCH.name()) || this.name().equals(ReqId.WEB_HOME_COMPANY.name())
                    ) {
                        mBaseUrl = WUrl.ADDR_DEV;
                    } else {
                        mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                    }
                    rtn = mBaseUrl + "/" + mUrl;
                    break;
                case DEV:
                case RDEBUG:
                    if ( this.name().equals(ReqId.WEB_START.name()) || this.name().equals(ReqId.WEB_PERMISSION.name()) ||
                         this.name().equals(ReqId.WEB_PERMISSION_GUIDE.name()) || this.name().equals(ReqId.WEB_ERROR.name())
                    ) { /** 개발 설정으로 서버이용 : 설치시 무조건 로컬파일 */
                        mBaseUrl = WUrl.ADDR_DEV;
                    } else if (
                        this.name().equals(ReqId.BASIC_INFO.name()) || this.name().equals(ReqId.BASIC_SETTINGS.name()) || this.name().equals(ReqId.WEB_STATE.name()) ||
                        this.name().equals(ReqId.WEB_SEARCH.name()) || this.name().equals(ReqId.WEB_HOME_COMPANY.name())
                    ) {
                        mBaseUrl = WUrl.ADDR_DEV;
                    } else if ( this.name().equals(ReqId.WEB_DEVELOPER_SUPPORT.name()) ) {
                        if ( Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance) ) {
                            mBaseUrl = WUrl.ADDR_DEV;
                        } else {
                            mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        }
                    } else { /** 개발 설정으로 서버이용 */
                     // if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useServer ) {
//                            if (Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance)) {
                                mBaseUrl = WUrl.ADDR_DEV;
//                            } else {
//                                mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
//                            }
                     // } else {
                     //     mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                     // }
                    }
                    rtn = mBaseUrl + "/" + mUrl;
                    break;
                case DEBUG:
                case RELEASE:
                    if ( this.name().equals(ReqId.WEB_START.name()) || this.name().equals(ReqId.WEB_PERMISSION.name()) ||
                         this.name().equals(ReqId.WEB_PERMISSION_GUIDE.name()) || this.name().equals(ReqId.WEB_ERROR.name())
                    ) { /** 설치시 무조건 로컬파일 */
//                        mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        mBaseUrl = WUrl.ADDR_RELEASE;
                    } else if (
                        this.name().equals(ReqId.BASIC_INFO.name()) || this.name().equals(ReqId.BASIC_SETTINGS.name()) || this.name().equals(ReqId.WEB_STATE.name()) ||
                        this.name().equals(ReqId.WEB_SEARCH.name()) || this.name().equals(ReqId.WEB_HOME_COMPANY.name())
                    ) {
                        mBaseUrl = WUrl.ADDR_RELEASE;
                    } else if ( this.name().equals(ReqId.WEB_DEVELOPER_SUPPORT.name()) ) {
                        if ( Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance) ) {
                            mBaseUrl = WUrl.ADDR_RELEASE;
                        } else {
                            mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        }
                    } else {
                        if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useServer ) {
//                            if (Connectivity.isConnected(WApplication.mInstance) && Utils.isServerConnected(WApplication.mInstance)) {
                                mBaseUrl = WUrl.ADDR_RELEASE;
//                            } else {
//                                mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
//                            }
                        } else {
                            mBaseUrl = WUrl.ADDR_LOCAL_ASSET;
                        }
                    }
                    rtn = mBaseUrl + "/" + mUrl;
                    break;
                default    :
                    break;
            }
            L.i(TAG,"getRequrl", "getReqUrl[" + this.name() + "] >> " + rtn );
            return rtn;
        }
    }
}
