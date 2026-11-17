package net.softm.data.kata.dto;

import android.content.Context;

import net.softm.data.kata.common.AdLoadType;
import net.softm.data.kata.common.AdMode;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.LogFormatType;
import net.softm.data.kata.common.PaidOPType;
import net.softm.data.kata.common.ReviewOPType;
import net.softm.data.kata.util.DeviceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * VersionDTOOut
 * 기본DTO
 * @author softm 
 */
public class BasicInfoDTO extends BaseDTO {

	public BasicInfoDTO(Context context) {
		super();
		this.serverConnected= false;
		this.useServerStateCheck= false;
		this.useServer= true;
		this.useRewardForWaitOverlay= false;
		this.useRewardButtonForWaitOverlay= false;
		this.adViewOPType = AdViewOPType.ADVIEW;
		this.adMode=AdMode.NONE;
		this.defaultAdMode=AdMode.GOOGLE;
		this.waitTimeForNotAdLicense=3000L;
		this.delayMusicEnable=3000L;
		this.adTypeForMainBanner=AdType.NONE;
		this.adTypeForPopupBanner=AdType.NONE;
		this.adTypeForWaitBanner=AdType.NONE;
		this.adTypeForWaitReward =AdType.NONE;
		this.adTypeForWaitBannerLoadType = AdLoadType.DIRECT;
		this.adViewIntervalTime = 1000L * 60 * 60 * 24 * 1; /** 광고 표시 인터벌 */
		this.adViewLicenseAddTime = 1000L * 60 * 60 * 24 * 2; /** 광고 추가 시간  */
		this.adViewLicenseAddMaxTime = 1000L * 60 * 60 * 24 * 365; /** 최대 광고 추가 시간 */

		this.reviewOPType= ReviewOPType.STOP;
		this.reviewRequestFirstIntervalTime = 1000L * 60 * 60 * 24 * 1;
		this.reviewRequestIntervalTime = 1000L * 60 * 60 * 24 * 3;
		this.appReleaseUpdtedTime = 0;

		this.crashLog = true;
		this.crashLogFormat = LogFormatType.DEFAULT;

		this.checkServiceLogIntervalTime = 1000L * 60 * 60 * 24 * 1;
		this.checkCrashIntervalTime = 1000L * 60;
		this.checkServiceAllEnable = false;
		this.checkService = false;
		this.checkServiceLog = false;
		this.checkServiceLogFormat = LogFormatType.DEFAULT;

		this.checkServiceForCrash = false;
		this.checkServiceLogForCrash = false;
		this.checkServiceLogFormatForCrash = LogFormatType.DEFAULT;
		this.checkServiceDelaySecForCrash = 10;

		this.checkServiceRepeatInterval = 15;
		this.checkServiceRepeatIntervalTimeUnit = TimeUnit.MINUTES;
		this.checkServiceFlexInterval = 5;
		this.checkServiceFlexIntervalTimeUnit = TimeUnit.MINUTES;

		this.checkServiceInitialDelay = 0;
		this.checkServiceInitialDelayTimeUnit = TimeUnit.SECONDS;

		this.doRegUserInfo = false;
		this.userInfoLogFormat = LogFormatType.DEFAULT;


		this.checkServiceCheckNoti = false;
		this.checkServiceCheckLogSendNoti= false;

//		this.checkGrantInfo = new ArrayList<>(Arrays.asList("notification_access","accessibility","ignore_battery","location"));
		this.checkGrantInfo = new ArrayList<>(Arrays.asList("notification_access"));

		this.googleAdViewErrorCase = new ArrayList<>();
		this.googleAdViewErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "com.google.android.gms.ads.internal.ClientApi")));
		this.googleAdViewErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "AdUtil.addBanner", "BaseAdView.loadAd")));
		this.googleAdViewErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "AdUtil.loadRewardAd", "RewardedAd.load")));
		this.googleAdViewErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.NullPointerException", "TestActivity.onClick_btnTests")));

		this.googlePaidErrorCase = new ArrayList<>();
//		this.googlePaidErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "com.google.android.gms.ads.internal.ClientApi")));
//		this.googlePaidErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "AdUtil.addBanner", "BaseAdView.loadAd")));
//		this.googlePaidErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.VerifyError", "AdUtil.loadRewardAd", "RewardedAd.load")));
//		this.googlePaidErrorCase.add(new ArrayList<>(Arrays.asList("java.lang.NullPointerException", "TestActivity.onClick_btnTests")));

		this.uuid = DeviceUtil.getUUID(context);

		this.purchaseLogFormat = LogFormatType.DEFAULT;
	}
	public boolean useServerStateCheck;
	/** http 서버를 이용할지, Local리소스를 로드할지 여부 */
	public boolean useServer;

	/** 대기 오버레이 화면에서 리워드기능 사용 여부 ( 상단 메시지 및 버튼 기능 ) */
	public boolean useRewardForWaitOverlay;

	/** 대기 오버레이 화면에서 리워드기능 사용 여부 - 버튼 ( 상단 메시지 및 버튼 기능 ) */
	public boolean useRewardButtonForWaitOverlay;

	/** 디바이스 uuid*/
	public String uuid;

	/** 앱 업데이트 타임 : 서버의 basic_info.json에서 값이 할당됨, index.html에서 업데이트 시간을 표시하기 위해 이용됨 */
	public long appReleaseUpdtedTime;

	/** 라이센스가 없는 사용자에게 할당되는 기본 대기시간 */
	public long waitTimeForNotAdLicense;

	/** Google AdView Error Case */
	public List<List<String>> googleAdViewErrorCase;

	/** Google 결제 Error Case */
	public List<List<String>> googlePaidErrorCase;

	/** 베타 라이센스를 적용할 UUID */
	public List<String> betaLicensedUUIDs;

	/** 광고 제외 UUID */
	public List<String> exceptAdModeUUIDs;

	/** 유료 제외 UUID */
	public List<String> exceptPaidUUIDs;

	/** 테스트 UUID */
	public List<String> testUuids;

	/** 음악 플레이어를 활성화하는 지연 시간 */
	public long delayMusicEnable;

	/** 광고 표시 재시청 간격 */
	public long adViewIntervalTime;
	/** 광고 시청에 대한 라이센스 보상 시간 */
	public long adViewLicenseAddTime;
	/** 광고 시청에 대한 보상시간 누적 최대 시간 */
	public long adViewLicenseAddMaxTime;

	/** 서버가 살아있는지 여부 : 서버 루트에 basic_info.json이 존재하는지 여부 */
	public boolean serverConnected;

	/** 유료 결제 라이센스 운영 형태 */
	public PaidOPType paidOPType;
	/** 유료 결제 라이센스 운영 형태를 강제로 Paid로 적용할 uuid */
	public List<String> forcePaidOPTypeUUIDs;

	/** 광고보기 라이센스 운영 형태 */
	public AdViewOPType adViewOPType;
	/** 광고보기 라이센스 운영 형태를 강제로 ADVIEW로 적용할 uuid */
	public List<String> forceAdViewOPTypeAdViewUUIDs;
	/** 광고보기 라이센스 운영 형태를 강제로 ADVIEW_STOP로 적용할 uuid */
	public List<String> forceAdViewOPTypeAdViewStopUUIDs;

	/** 광고 모드 */
	public AdMode adMode;

	/** 기본 광고 모드 */
	public AdMode defaultAdMode;

	/** 광고를 모드를 defaultAdMode로 적용할 uuid */
	public List<String> forceAdModeUUIDs;

	/** 광고 타입 : 메인화면 (MainActivity) */
	public AdType adTypeForMainBanner;
	/** 광고 타입 : 팝업화면 (WebPopActivity) */
	public AdType adTypeForPopupBanner;
	/** 광고 타입 : 대기화면 (WaitOverlayViewService) */
	public AdType adTypeForWaitBanner;
	/** 광고 타입 : 대기화면 보상광고 (WaitOverlayViewService) */
	public AdType adTypeForWaitReward;

	/** 광고 로드 방식 */
	public AdLoadType adTypeForWaitBannerLoadType;

	/** 리뷰 운영 방식 */
	public ReviewOPType reviewOPType;
	/** 설치 후 처음 리뷰 요청 간격 */
	public long reviewRequestFirstIntervalTime;
	/** 설치 후 처음 리뷰 요청 그 후 간격 */
	public long reviewRequestIntervalTime;

	/** 크래시 로깅 여부 */
	public boolean crashLog;
	/** 크래시 로깅 포맷 */
	public LogFormatType crashLogFormat;


	/** 사용자 사용 여부 체크 */
	public boolean checkLiveUser;
	/** 사용자 사용 여부 체크 로깅 간격 */
	public long checkLiveUserLogIntervalTime;
	public LogFormatType liveUserLogFormat;

	/** Service 체크 여부 - 전체 활성화 - true의 경우만 의미있음 : 조작 주의 */
	public boolean checkServiceAllEnable;
	/** Service 체크 여부 */
	public boolean checkService;
	/** Service 체크 실행 대상 uuid */
	public List<String> checkServiceUUIDs;

	/** Service 체크 로깅*/
	public boolean checkServiceLog;
	/** Service 체크 로깅 간격 */
	public long checkServiceLogIntervalTime;

	/** 비정상 종료 체크 간격 */
	public long checkCrashIntervalTime;

	/** Service 체크 로깅 포맷 */
	public LogFormatType checkServiceLogFormat;
	/** Service 체크 로깅 대상 UUID */
	public List<String> checkServiceLogUUIDs;

	/** Service 체크 : 반복 시간 */
	public long checkServiceRepeatInterval;
	/** Service 체크 : 반복 시간 단위 */
	public TimeUnit checkServiceRepeatIntervalTimeUnit;

	/** Service 체크 : 유연 시간 */
	public long checkServiceFlexInterval;
	/** Service 체크 : 유연 시간 단위 */
	public TimeUnit checkServiceFlexIntervalTimeUnit;
	/** Service 체크 : 시작 지연 시간 */
	public long checkServiceInitialDelay;
	/** Service 체크 : 시작 지연 시간 단위 */
	public TimeUnit checkServiceInitialDelayTimeUnit;

	/** 크래쉬 상황시 Service 체크 */
	public boolean checkServiceForCrash;
	/** 크래쉬 상황시 Service 체크 로깅 */
	public boolean checkServiceLogForCrash;
	/** 크래쉬 상황시 Service 체크 로깅 포맷 */
	public LogFormatType checkServiceLogFormatForCrash;
	/** 크래쉬 상황시 Service 체크 로깅 지연 시간 */
	public int checkServiceDelaySecForCrash;

	/** 사용자 등록 */
	public boolean doRegUserInfo;
	/** 사용자 등록 로깅 포맷 */
	public LogFormatType userInfoLogFormat;

	/** 권한 체크 순서 */
	public List<String> checkGrantInfo;

	/** Service 체크 실행시 노티 표시 여부 */
	public boolean checkServiceCheckNoti;
	/** Service 체크 실행시 노티 표시할 uuid */
	public List<String> checkServiceCheckNotiUUIDs;

	/** Service 체크 실행시 로그를 보냈는지를 노티 표시 여부 */
	public boolean checkServiceCheckLogSendNoti;
	/** Service 체크 실행시 로그를 보냈는지를 노티 표시할 uuid */
	public List<String> checkServiceCheckLogSendNotiUUIDs;

	/** Purchase 로깅 포맷 */
	public LogFormatType purchaseLogFormat;


}
