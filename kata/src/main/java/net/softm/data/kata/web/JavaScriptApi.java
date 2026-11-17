package net.softm.data.kata.web;


import net.softm.lib.common.WLog;

import org.json.JSONException;
import org.json.JSONObject;

public class JavaScriptApi {
	public static final int API_UNKNOWN = -1;
	/**
	 * 시작 - 실행
	 */
	public static final int API_001 = 1;

	/**
	 * 시작 - 종료
	 */
	public static final int API_002 = 2;

	/**
	 * Settings Start
	 */
	public static final int API_003 = 3;
	/**
	 * 권한 체크
	 */
	public static final int API_123 = 123;

	/**
	 * 권한 - 체크
	 */
	public static final int API_004 = 4;

	/**
	 * GPS 위치
	 */
	public static final int API_005 = 5;

	/**
	 * 안내 시작
	 */
	public static final int API_006 = 6;

	/**
	 * 안내 시작 - NaviDestination
	 * @see net.softm.data.kata.common.NaviDestination
	 */
	public static final int API_007 = 7;

	/**
	 * alert(경고창)
	 */
	public static final int API_100 = 100;
	/**
	 * conFirm(승인창)
	 */
	public static final int API_101 = 101;
	/**
	 * 단순 메시지창
	 */
	public static final int API_102 = 102;
	/**
	 * 팝업호출1
	 */
	public static final int API_103 = 103;
	/**
	 * 팝업닫기1
	 */
	public static final int API_106 = 106;
	/**
	 * 모바일웹열기
	 */
	public static final int API_107 = 107;

	/**
	 * 키보드 호출
	 */
	public static final int API_108 = 108;
	/**
	 * 보안키패드 호출
	 */
	public static final int API_109 = 109;
	/**
	 * GoMain
	 */
	public static final int API_110 = 110;
	/**
	 * 사진촬영
	 */
	public static final int API_114 = 114;
	/**
	 * 프로그래스 on
	 */
	public static final int API_115 = 115;
	/**
	 * 프로그래스 off
	 */
	public static final int API_116 = 116;
	/**
	 * 프로그래스 on (타이머 적용)
	 */
	public static final int API_117 = 117;
	/**
	 * 프로그래스 off (타이머 적용)
	 */
	public static final int API_118 = 118;
	/**
	 * 앱강제 종료
	 */
	public static final int API_119 = 119;
	/**
	 * 데이터 저장 : set Shared Preference
	 */
	public static final int API_120 = 120;
	/**
	 * 데이터 불러오기
	 */
	public static final int API_121 = 121;
	/**
	 * 전화걸기
	 */
	public static final int API_126 = 126;

	/**
	 * uuid 전달
	 */
	public static final int API_142 = 142;

	public static final int API_300 = 300;
	public static final int API_333 = 333;

	/**
	 * Get Url
	 * @param WUrl.ReqId.
	 */
	public static final int API_301 = 301;

	/**
	 * api_302_isPermission
	 * LOCATION
	 * USAGESTATS
	 * DRAWOVERLAYS
	 * ACCESS_FINE_LOCATION
	 */
	public static final int API_302 = 302;
	public static final int API_303 = 303;
	public static final int API_304 = 304;
	public static final int API_305 = 305;
	public static final int API_315 = 315;
	public static final int API_306 = 306;
	public static final int API_307 = 307;
	public static final int API_308 = 308;
	public static final int API_400 = 400;
	public static final int API_401 = 401;
	public static final int API_402 = 402;
	public static final int API_500 = 500;
	public static final int API_501 = 501;
	public static final int API_502 = 502;
	public static final int API_503 = 503;
	public static final int API_504 = 504;
    public static final int API_334 = 334;
    public static final int API_777 = 777;
    public static final int API_111 = 111;
    public static final int API_112 = 112;
    public static final int API_113 = 113;
    public static final int API_700 = 700;
    public static final int API_701 = 701;
    public static final int API_800 = 800;
    public static final int API_801 = 801;
    public static final int API_802 = 802;
    public static final int API_803 = 803;
    public static final int API_804 = 804;
    public static final int API_805 = 805;
    public static final int API_806 = 806;
    public static final int API_807 = 807;
    public static final int API_808 = 808;

	public static final int API_810 = 810;
	public static final int API_811 = 811;
	public static final int API_812 = 812;
    public static final int API_900 = 900;
    public static final int API_901 = 901;
    public static final int API_902 = 902;

	public static final int API_910 = 910;
    public static final int API_820 = 820;
    public static final int API_821 = 821;

    /**
	 * JSON 요청 문자열에서 API 구분값을 얻어온다.
	 * @param json JSON 요청 문자열
	 * @return API 구분값
	 */
	public static int getApi(JSONObject json) {
		int api = API_UNKNOWN;
		try {
			JSONObject header = json.getJSONObject("header");
			String apiStr = header.getString("api");
			api = Integer.parseInt(apiStr);
		} catch (NumberFormatException|JSONException e) {
			WLog.printException(e);
		}

		return api;
	}
}