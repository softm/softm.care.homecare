package net.softm.data.kata.dto.base;

import android.content.Context;

import net.softm.data.kata.dto.BaseDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * BasicSettings
 * @author softm
 */
public class BasicSettings extends BaseDTO {

	public BasicSettings(Context context) {
		super();

		this.groups = new ArrayList<>();
		this.videos = new ArrayList<Video>();
		this.voices = new ArrayList<Voice>();

		this.prayEndIntervalTimeSec = 60 * 60 * 2; /** 예배완료 간격 : 2시간 */
		this.prayClassSelectTimeSec = 24 * 60 * 60 + ( 12 * 60 * 60 ); /** 기도모임 조회 기간 : 1일 + 12시간 */
	}
	/** 그룹 */
	public List<Group> groups;

	/** 비디오 */
	public List<Video> videos;

	/** 말씀 */
	public List<Voice> voices;

	/** 그룹 마스터 Id */
	public List<String> groupMasterUUIDs;
//	public List<String> forcePaidOPTypeUUIDs;

	/** 예배완료 간격 */
	public long prayEndIntervalTimeSec;

	/** 기도모임 조회 기간 */
	public long prayClassSelectTimeSec;
}
