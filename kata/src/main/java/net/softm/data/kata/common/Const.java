package net.softm.data.kata.common;

import net.softm.data.kata.BuildConfig;

public class Const {

    /**
     * 네빙 종료시 Clear naving
     */
    public static final boolean USING_SET_NOTNAVING = true;

    /**
     * 서버 상태 정상
     */
    public static final String SERVER_TYPE_SUCCESS          = "S";
    /**
     * 서버 상태 공지사항
     */
    public static final String SERVER_TYPE_NOTICE           = "NOTICE";
    /**
     * 서버 상태 에러
     */
    public static final String SERVER_TYPE_ERROR            = "ERROR";

    public static String SERVER_HOST = "http://www.softm.net";
    public static String PATH_NAME = "g"+ BuildConfig.VERSION_CODE;
    /**
     * 서버 타입
     */
    public static final String INTENT_SERVER_TYPE = "intent_server_type";

    /**
     * 앱 종료
     */
    public static final String INTENT_APP_FINISH = "intent_app_finish";

    /**
     * 간편로그인
     */
    public static final String PREF_KEY_IS_SIMPLE_LOGIN = "EASY_PROC";

    /**
     * 파워 연결시 네비 시작 지연시간
     */
    public static final long POWER_CONNECTED_START_NAVI_DELAY_TIME = 100;

    /**
     * 부팅 완료후 네비 시작 지연시간
     */
    public static final long BOOT_COMPLETE_START_NAVI_DELAY_TIME = 1000;

    /**
     * 전원 연결 리시버 중복 호출 방지 타임.
     */
    public static final long POWER_CONNECTED_RECEIVER_CALL_INTERVAL_TIME = 1000;

    /**
     * 전원 연결 해제 리시버 중복 호출 방지 타임.
     */
    public static final long POWER_DISCONNECTED_RECEIVER_CALL_INTERVAL_TIME = 1000;

    /**
     * 네비 안내 완료 노티 제거 대기 시간
     */
    public static final long NAVI_END_NOTIFICATION_CANCEL_TIME = 60000;

    /**
     * 안내 취소 거리
     */
    public static final float NAVI_CANCEL_DISTANCE = 100; // 100m

    /**
     * UI 미제공 버전
     */
    public static final float UNSUPPORT_UI_SDK_INT = 19; // 19 이하 지원안함.

    /**
     * 네비 오버레이 팝업 카운트다운 시간
     * 팝업 대기시간 ( 초 )
     */
    public static int NAVI_OVERLAY_COUNTDOWN_TIME = -1; // 10 SEC
    public static final int NAVI_OVERLAY_WAIT_TIME = 20000; // 10 SEC

    /**
     * 안내 종료후 1분동안 다시 안내되는것을 방지함
     * 내비 재실행 방지시간
     */
    public static long NAVI_RENAVI_PREVENT_TIME = 3000; // 30 sec
//    public static final long NAVI_RENAVI_PREVENT_TIME = 60000*5; // 5 min

    /**
     * 목적지를 직접 안내를 시작하고 내비앱을 재호출하는 경우 기존의 목적지를 다시 실행하는 시간
     * 그 시간이 경과하면 집/회사,실행(BASIC) Destination을 실행함
     */
    public static long NAVI_DESTINATION_EXECUTION_HOLD_TIME = 1000 * 60 * 1; // 1 min
 // public static long NAVI_DESTINATION_EXECUTION_HOLD_TIME = 1000 * 60 * 60 * 24; // 1 day

    /**
     * 네비 오버레이 팝업 비활성화 대기 시간
     */
    public static int NAVI_OVERLAY_CLOSE_WAIT_TIME = 5000; // 10 SEC

    /**
     * 네비 오버레이 팝업 흐리게 애니메이션 Duration 시간
     */
    public static final int NAVI_OVERLAY_BLUR_DURATION_TIME = 3000; // 3 SEC

    public static final String PREF_KEY_APP_PACKAGE = "_APP_PACKAGE";
    public static final String PREF_KEY_APP_PACKAGE_ICON = "_APP_PACKAGE_ICON";

    public static final String PREF_KEY_MUSIC_APP_PACKAGE = "_MUSIC_APP_PACKAGE";
    public static final String PREF_KEY_MUSIC_APP_PACKAGE_ICON = "_MUSIC_APP_PACKAGE_ICON";
    public static final String PREF_KEY_MUSIC_PLAY_TITLE = "_MUSIC_PLAY_TITLE";

    public static final String PREF_KEY_BLUETOOTHS = "_BLUETOOTHS";
    public static final String PREF_KEY_BLUETOOTH = "_BLUETOOTH";
    public static final String PREF_KEY_WIFI = "_WIFI";
    public static final String PREF_KEY_OPEN_POPUP = "_OPEN_POPUP";
    public static final String PREF_KEY_FIRST_START = "_FIRST_START";
    public static final String PREF_KEY_PERMISSION_DODENIED_POST_NOTIFICATIONS = "_PERMISSION_DODENIED_POST_NOTIFICATIONS";
    public static final String PREF_KEY_APP_INSTALLED = "_APP_INSTALLED";
    public static final String PREF_KEY_SKIP_ACS_PERMISSION = "_SKIP_ACS_PERMISSION";
    public static final String PREF_KEY_APP_UPDATED = "_APP_UPDATED";
    public static final String PREF_KEY_APP_UPDATED_TIME = "_APP_UPDATED_TIME";
    public static final String PREF_KEY_APP_UPDATED_NOTI_CONFIRM = "_APP_UPDATED_NOTI_CONFIRM";

    public static final String PREF_KEY_POST_NOTIFICATIONS_CONFIRM_COUNT = "_POST_NOTIFICATIONS_CONFIRM_COUNT";

    public static final String PREF_KEY_INIT_TO_COUNT = "_INIT_TO_COUNT";

    public static final String PREF_KEY_APP_UPDATE_CHECK_TIME = "_APP_UPDATE_CHECK_TIME";
    public static final String PREF_KEY_APP_UPDATE_DELAY_TIME = "_APP_UPDATE_DELAY_TIME";

    public static final String PREF_KEY_AD_VIEW_CHECK_TIME = "_AD_VIEW_CHECK_TIME";
    public static final String PREF_KEY_LICENSE_EXPIRE_TIME = "_LICENSE_EXPIRE_TIME";

    public static final String PREF_KEY_POI_NAVI_EXPIRE_TIME = "_POI_NAVI_EXPIRE_TIME";

    public static final String PREF_KEY_REVIEW_REQUEST_CHECK_TIME = "_REVIEW_REQUEST_CHECK_TIME";

    /**
     * 비정상 종료 발생
     */
    public static final String PREF_KEY_CRASH_ERROR = "_CRASH_ERROR";

    /**
     * 구글배너 로드시 에러 여부
     */
    public static final String PREF_KEY_AD_VIEW_GOOGLE_ERROR = "_AD_VIEW_GOOGLE_ERROR";

    /**
     * 구글배너 로드시 에러 케이스 INDEX
     */
    public static final String PREF_KEY_AD_VIEW_ERROR_GOOGLE_CASE_INDEX = "_AD_VIEW_ERROR_GOOGLE_CASE_INDEX";


    /**
     * 구글결제 시 에러 여부
     */
    public static final String PREF_KEY_PAID_GOOGLE_ERROR = "_PAID_GOOGLE_ERROR";

    /**
     * 구글결제 시 에러 케이스 INDEX
     */
    public static final String PREF_KEY_PAID_ERROR_GOOGLE_CASE_INDEX = "_PAID_ERROR_GOOGLE_CASE_INDEX";

    /**
     * 접근성서비스 활성화 횟수 : 접근성이 죽었으니 다시 살린 횟수 이므로, 불안정성의 지표가 됨
     */
    public static final String PREF_KEY_ACCESSIBILITY_SERVICE_ENABLE_COUNT = "_ACCESSIBILITY_SERVICE_ENABLE_COUNT";
    /**
     * 시작,종료 동작중 중지 횟수 : 중간에 중지했다는 것은 불필요한 액션이었다는 의미
     * 락상태에서 화면이 풀리면서 실행 한 것인지
     * 언락상태에서 바로 화면으로 진앱해서 실행 한 것인지
     */
    public static final String PREF_KEY_ACTON_START_LOCK_CANCEL_COUNT = "_ACTON_START_LOCK_CANCEL_COUNT";
    public static final String PREF_KEY_ACTON_STOP_LOCK_CANCEL_COUNT = "_ACTON_STOP_LOCK_CANCEL_COUNT";
    public static final String PREF_KEY_ACTON_START_UNLOCK_CANCEL_COUNT = "_ACTON_START_UNLOCK_CANCEL_COUNT";
    public static final String PREF_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT = "_ACTON_STOP_UNLOCK_CANCEL_COUNT";

    public static final String PREF_KEY_OVERLAY_POPUP_X = "_OVERLAY_POPUP_X";
    public static final String PREF_KEY_OVERLAY_POPUP_Y = "_OVERLAY_POPUP_Y";
    public static final String PREF_KEY_ASS_OVERLAY_POPUP_X = "_ASS_OVERLAY_POPUP_X";
    public static final String PREF_KEY_ASS_OVERLAY_POPUP_Y = "_ASS_OVERLAY_POPUP_Y";

    public static final String PREF_KEY_OVERLAY_OBSERVER_POPUP_X = "_OVERLAY_OBSERVER_POPUP_X";
    public static final String PREF_KEY_OVERLAY_OBSERVER_POPUP_Y = "_OVERLAY_OBSERVER_POPUP_Y";

    public static final String PREF_KEY_CONNECTED_BATTERY = "_CONNECTED_BATTERY";
    public static final String PREF_KEY_CONNECTED_WIFI_SSID = "_CONNECTED_WIFI_SSID";
    public static final String PREF_KEY_CONNECTED_BLUETOOTH = "_CONNECTED_BLUETOOTH";

    public static final String PREF_KEY_LAST_CHECK_CRASH_TIME = "_LAST_CHECK_CRASH_TIME";

    public static final String PREF_KEY_LAST_CHECK_SERVICE_LOGTIME = "_LAST_CHECK_SERVICE_LOGTIME";

    public static final String PREF_KEY_LAST_CHECK_LIVE_USER_LOGTIME = "_LAST_CHECK_LIVE_USER_LOGTIME";

    public static final String PREF_KEY_NOTICE_ALERT_MUSIC_PLAYER = "_NOTICE_ALERT_MUSIC_PLAYER";

    /**
     *  @see NaviDestination
     */
    public static final String PREF_KEY_NAVI_DESTINATION = "_NAVI_DESTINATION";

    /**
     *  @see NaviMode
     */
    public static final String PREF_KEY_NAVI_MODE = "_NAVI_MODE";

    /**
     *  @see MapMode
     */
    public static final String PREF_KEY_MAP_MODE = "_MAP_MODE";

    /**
     *  @see ChargingMode
     */
    public static final String PREF_KEY_CHARGING_MODE = "_CHARGING_MODE";

    /**
     *  독립 조건
     */
    public static final String PREF_KEY_INDEPENDENCE_CONDITION = "_INDEPENDENCE_CONDITION";

    /**
     * 시작/종료시 On/Of
     */
    public static final String PREF_KEY_START_STOP_ON_OFF = "_START_STOP_ON_OFF";

    /**
     *  시작 모드
     * @see StartMode
     */
//    public static final String PREF_KEY_START_MODE = "_START_MODE";

    /**
     *  종료 모드
     * @see StopMode
     */
    public static final String PREF_KEY_STOP_MODE = "_STOP_MODE";

    /**
     *  앱 동작 여부
     * @see AppEnable
     */
    public static final String PREF_KEY_APP_ENABLE = "_APP_ENABLE";


    /**
     *  목적지 안내 진입후에만
     *  Only after entering destination guidance
     */
    public static final String PREF_KEY_ONLY_STOP_AFTER_NAVING = "_ONLY_STOP_AFTER_NAVING";


    /**
     *  볼륨 조절 여부
     */
    public static final String PREF_KEY_CONTROL_VOLUME = "_CONTROL_VOLUME";

    /**
     *  볼륨
     */
    public static final String PREF_KEY_VOLUME = "_VOLUME";

    /**
     *  충전시 실행
     */
    public static final String PREF_KEY_START_POWER = "_START_POWER";

    /**
     *  화면 상태 (시작시)
     * @see RunStatusScreenMode
     */
    public static final String PREF_KEY_RUN_STATUS_SCREEN_START_MODE = "_RUN_STATUS_SCREEN_START_MODE";

    /**
     *  화면 상태 (시작시) : 실행 체크 시간
     */
    public static final String PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START = "_RUN_STATUS_SCREEN_TIME_FOR_START";

    /**
     *  화면 상태 (종료시)
     * @see RunStatusScreenMode
     */
    public static final String PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE = "_RUN_STATUS_SCREEN_STOP_MODE";

    /**
     *  화면 상태 (종료시) : 실행 체크 시간
     */
    public static final String PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP = "_RUN_STATUS_SCREEN_TIME_FOR_STOP";

    /**
     *  실행 속력 조건
     */
    public static final String PREF_KEY_RUN_SPEED = "_RUN_SPEED";

    /**
     *  실행 속력 조건 : 제한시간 : 분
     */
    public static final String PREF_KEY_RUN_SPEED_LIMIT_TIME = "_RUN_SPEED_LIMIT_TIME";
    /**
     *  실행 속력 조건 : 속력 km/h
     */
    public static final String PREF_KEY_RUN_SPEED_LIMIT = "_RUN_SPEED_LIMIT";

    /**
     *  블루투스연결시 실행
     */
    public static final String PREF_KEY_START_BLUETOOTH = "_START_BLUETOOTH";

    /**
     *  무선와이파이연결시 실행
     */
    public static final String PREF_KEY_START_WIFI = "_START_WIFI";

    /**
     *  알림바 아이콘
     */
    public static final String PREF_KEY_NOTI_ICON = "_NOTI_ICON";
    /**
     *  디버그 플래그 : 1, 0
     */
    public static final String PREF_KEY_TEST = "_TEST";
    /**
     *  향상된 실행 - ANR(응답없음) 방지 : 1, 0
     */
    public static final String PREF_KEY_ENHANCED_EXECUTION = "_ENHANCED_EXECUTION";

    /**
     * 체크 서비스 체크
     */
    public static final String PREF_KEY_CHECK_SERVICE = "_CHECK_SERVICE" ;


    /**
     * 체크 서비스 체크 여부 노티
     */
    public static final String PREF_KEY_CHECK_SERVICE_CHECK_NOTI = "_CHECK_SERVICE_CHECK_NOTI";

    /**
     * 체크 서비스 체크 로그 발송 여부 노티
     */
    public static final String PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI = "_CHECK_SERVICE_CHECK_LOG_SEND_NOTI";

    /**
     *  팝업(오버레이)
     *  1 : 팝업유지, 0 : 팝업닫기
     */
    public static final String PREF_KEY_NAVI_POPUP_MODE = "_NAVI_POPUP_MODE";

    /**
     *  내비 자동실행
     *  1 : 실행, 0 : 안함
     */
    public static final String PREF_KEY_NAVI_AUTO_START = "_NAVI_AUTO_START";

    /**
     *  지오펜싱 사용여부
     */
    public static final String PREF_KEY_GEOFENCE = "_GEOFENCE";

    /**
     *  목적지 진입 반경 ( m )
     */
    public static final String PREF_KEY_GEOFENCE_RADIUS_IN_METERS = "_NAVI_GEOFENCE_RADIUS";

    /**
     *  팝업(오버레이) 대기시간 ( 초 )
     */
    public static final String PREF_KEY_NAVI_OVERLAY_WAIT_TIME = "_NAVI_OVERLAY_WAIT_TIME";

    /**
     * 안내 종료후 1분동안 다시 안내되는것을 방지함
     * 내비 재실행 방지시간
     */
    public static final String PREF_KEY_RENAVI_PREVENT_TIME = "_NAVI_RENAVI_PREVENT_TIME";

    /**
     * 목적지를 직접 안내를 시작하고 내비앱을 재호출하는 경우 기존의 목적지를 다시 실행하는 시간
     * 그 시간이 경과하면 집/회사,실행(BASIC) Destination을 실행함
     */
    public static final String PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME = "_NAVI_DESTINATION_EXECUTION_HOLD_TIME";

    /**
     * 출/퇴근시간 설정
     */
    public static final String PREF_KEY_WORK_GOTO_OFF_TIME = "_WORK_GOTO_OFF_TIME";

    /**
     * 출근시작시간
     */
    public static final String PREF_KEY_WORK_GOTO_TIME_FROM = "_WORK_GOTO_TIME_FROM";

    /**
     * 출근종료시간
     */
    public static final String PREF_KEY_WORK_GOTO_TIME_TO = "_WORK_GOTO_TIME_TO";

    /**
     * 퇴근시작시간
     */
    public static final String PREF_KEY_WORK_OFF_TIME_FROM = "_WORK_OFF_TIME_FROM";

    /**
     * 퇴근종료시간
     */
    public static final String PREF_KEY_WORK_OFF_TIME_TO = "_WORK_OFF_TIME_TO";

    /**
     * 내비안내 시작 시간
     */
    public static final String PREF_KEY_NAVI_START_TIME = "_NAVI_START_TIME";

    /**
     * 내비안내 시작 시간 - 마지막
     */
    public static final String PREF_KEY_NAVI_LAST_START_TIME = "_NAVI_LAST_START_TIME";

    /**
     * 내비안내 시작 시간 호출시간 : 오버레이 호출 타임.그후에 카운트 다운.
     */
    public static final String PREF_KEY_NAVI_START_CALL_TIME = "_NAVI_LAST_START_CALL_TIME";

    /**
     * 내비안내 종료 시간
     */
    public static final String PREF_KEY_NAVI_END_TIME = "_NAVI_END_TIME";

    /**
     * 내비중 Y / N : 노티를 통해 판단
     */
    public static final String PREF_KEY_NOTI_LISTENER_NAVING = "_NOTI_LISTENER_NAVING";

    /**
     * 내비안내 시작 시간 : 노티를 통해 판단
     */
    public static final String PREF_KEY_NOTI_LISTENER_NAVI_START_TIME = "_NOTI_LISTENER_NAVI_START_TIME";

    /**
     * 내비안내 종료 시간 : 노티를 통해 판단
     */
    public static final String PREF_KEY_NOTI_LISTENER_NAVI_END_TIME = "_NOTI_LISTENER_NAVI_END_TIME";

    /**
     * 내비중 Y / N
     */
    public static final String PREF_KEY_NAVING = "_NAVING";

    /**
     * 내비안내중 리셋 타임
     */
    public static final String PREF_KEY_NAVI_RESET_HOUR = "_NAVI_RESET_HOUR";

    /**
     * 설정 상태 변경 체크 인터벌 시간
     */
    public static final long SETTING_CHANGE_CHECK_INTERVAL_TIME = 200;
    public static final long ACTIVITY_CHANGE_CHECK_INTERVAL_TIME = 1500;

    /**
     * 백버튼 종료 유효 시간
     */
    public static final long BACK_BUTTON_VALID_FINISH_TIME = 2000;

    /**
     *  내비 위치 정보 사용 여부
     *  1 : 사용, 0 : 안함
     */
    public static final String PREF_KEY_NAVI_USING_LOCATION = "_NAVI_USING_LOCATION";

    /**
     *  내비 알람 접근 허용 사용 여부
     *  1 : 사용, 0 : 안함
     */
    public static final String PREF_KEY_NAVI_USING_NOTI_ACCESS = "_NAVI_USING_NOTI_ACCESS";

    /**
     *  내비 노티피케이션 바 사용 여부
     *  1 : 사용, 0 : 안함
     */
    public static final String PREF_KEY_NAVI_USING_NOTI_BAR = "_NAVI_USING_NOTI_BAR";

    /**
     *  블루투스 is Enabled
     */
    public static final String SYS_INFO_BLUETOOTH_ENABLE = "_BLUETOOTH_ENABLE";

    /**
     *  등록한 집/회사 주소 이용.
     *  1 : 사용, 0 : 안함
     */
    public static final String PREF_KEY_USING_REG_HOME = "_USING_REG_HOME";

    public static final String PREF_KEY_POI_HOME = "_POI_HOME";
    public static final String PREF_KEY_POI_COMPANY = "_POI_COMPANY";
    public static final String PREF_KEY_POI_FAVORITE = "_POI_FAVORITE";
    public static final String PREF_KEY_POIS_FAVORITE = "_POIS_FAVORITE";
    public static final String PREF_KEY_POI_RECENT = "_POI_RECENT";
    public static final String PREF_KEY_POIS_RECENT = "_POIS_RECENT";

    /**
     * 화면 켜진 상태(시작시) : 실행 : 체크 시간
     */
    public static int RUN_STATUS_SCREEN_TIME_FOR_START = 15000; // 15 SEC

    /**
     * 화면 켜진 상태(종료시) : 실행 : 체크 시간
     */
    public static int RUN_STATUS_SCREEN_TIME_FOR_STOP = 15000; // 15 SEC

    /**
     * 실행 속력 조건 : 제한시간 : 분
     */
    public static int RUN_SPEED_LIMIT_TIME = 5; // 5 분
    /**
     * 실행 속력 조건 : 속력 km/h
     */
    public static int RUN_SPEED_LIMIT = 5; // 5 km/h

    /**
     * 앱 업데이트 미루기 시간
     */
    public static long APP_UPDATE_DELAY_TIME = 1000 * 60 * 60 * 24 * 7; // 7일
//  public static long APP_UPDATE_DELAY_TIME = 1000 * 60 * 5; // 5분
//  public static long APP_UPDATE_DELAY_TIME = 1000 * 60 * 1; // 1분

    /**
     * 사용자 타입
     * @see UserType
     */
    public static final String PREF_KEY_USER_TYPE = "_USER_TYPE";

    /**
     * 사용자 레벨
     * @see UserLevel
     */
    public static final String PREF_KEY_USER_LEVEL = "_USER_LEVEL";

    /**
     * 결제 정보 : 구독
     */
    public static final String PREF_KEY_PURCHASE_SUBS_ORIGINALJSON = "_PURCHASE_SUBS_ORIGINALJSON";

    /**
     * 결제 정보 : 평생
     */
    public static final String PREF_KEY_PURCHASE_LIFETIME_ORIGINALJSON = "_PURCHASE_LIFETIME_ORIGINALJSON";

    /**
     * 구독 토큰
     */
    public static final String PREF_KEY_PURCHASE_TOKEN_SUBS = "_PURCHASE_TOKEN_SUBS";

    /**
     * 평생 토큰
     */
    public static final String PREF_KEY_PURCHASE_TOKEN_LIFETIME = "_PURCHASE_TOKEN_LIFETIME";

    /**
     * 구독여부
     */
    public static final String PREF_KEY_LICENSE_SUBS = "_LICENSE_SUBS";

    /**
     * 평생여부
     */
    public static final String PREF_KEY_LICENSE_LIFETIME = "_LICENSE_LIFETIME";


    public static void init(){
        NAVI_OVERLAY_COUNTDOWN_TIME = WUrl.devMode == WUrl.EnumDevMode.DEV?0:3000; // 10 SEC
        NAVI_RENAVI_PREVENT_TIME    = WUrl.devMode == WUrl.EnumDevMode.DEV?0:3000; // 30 SEC
    }
}