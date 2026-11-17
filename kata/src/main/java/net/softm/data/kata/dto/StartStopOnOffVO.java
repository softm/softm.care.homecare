package net.softm.data.kata.dto;

/**
 * StartStopOnOffVO
 */
public class StartStopOnOffVO extends BaseDTO {
	public StartStopOnOffVO() {
		super();
	}

	private StartStop bluetooth;
	private StartStop wifi;
	private StartStop tethering;
	private StartStop airplane_mode;
	private StartStop screen_on;
	private StartStop lock;
	private StartStop completed_lock;
	private StartStop play_music;
	private StartStop home_screen;
	private StartStop power_off;

	//
//	                        "bluetooth:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"wifi:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"tethering:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"airplane_mode:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"screen_on:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"unlock:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"play_music:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"home_screen:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//									"power_off:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}}"+


	public StartStop getWifi() {
		return wifi;
	}

	public void setWifi(StartStop wifi) {
		this.wifi = wifi;
	}

	public StartStop getBluetooth() {
		return bluetooth;
	}

	public void setBluetooth(StartStop bluetooth) {
		this.bluetooth = bluetooth;
	}

	public StartStop getTethering() {
		return tethering;
	}

	public void setTethering(StartStop tethering) {
		this.tethering = tethering;
	}

	public StartStop getAirplaneMode() {
		return airplane_mode;
	}

	public void setAirplaneMode(StartStop airplane_mode) {
		this.airplane_mode = airplane_mode;
	}

	public StartStop getScreenOn() {
		return screen_on;
	}

	public void setScreen_on(StartStop screen_on) {
		this.screen_on = screen_on;
	}

	public StartStop getLock() {
		return lock;
	}
	public StartStop getCompletedLock() {
		return completed_lock;
	}

	public void setLock(StartStop lock) {
		this.lock = lock;
	}
	public void setCompletedLock(StartStop completed_lock) {
		this.completed_lock = completed_lock;
	}

	public StartStop getPlayMusic() {
		return play_music;
	}

	public void setPlayMusic(StartStop play_music) {
		this.play_music = play_music;
	}

	public StartStop getHomeScreen() {
		return home_screen;
	}

	public void setHomeScreen(StartStop home_screen) {
		this.home_screen = home_screen;
	}

	public StartStop getPowerOff() {
		return power_off;
	}

	public void setPowerOff(StartStop power_off) {
		this.power_off = power_off;
	}

	public class StartStop {
		//		{start:{use_yn:false,onoff:'on'}
		private StartUseYnOnOff start;
		private StartUseYnOnOff stop;

		public StartUseYnOnOff getStart() {
			return start;
		}

		public void setStart(StartUseYnOnOff start) {
			this.start = start;
		}

		public StartUseYnOnOff getStop() {
			return stop;
		}

		public void setStop(StartUseYnOnOff stop) {
			this.stop = stop;
		}
	}

	public class StartUseYnOnOff {
		//		{start:{use_yn:false,onoff:'on'}
		private boolean use_yn;
		private String onoff;

		public boolean isUseYn() {
			return use_yn;
		}
		public boolean isOn() {
			return onoff.equals("on");
		}
		public boolean isOff() {
			return onoff.equals("off");
		}

		public void setUseYn(boolean use_yn) {
			this.use_yn = use_yn;
		}

		public String getOnoff() {
			return onoff;
		}

		public boolean getEnabled() {
			return isOn();
		}

		public void setOnoff(String onoff) {
			this.onoff = onoff;
		}
	}
}
