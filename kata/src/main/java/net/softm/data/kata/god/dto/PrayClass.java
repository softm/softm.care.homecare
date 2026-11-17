package net.softm.data.kata.god.dto;

import static net.softm.data.kata.util.Utils.encdodeForWeb;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.util.Utils;

import java.util.List;
import java.util.Map;

/**
 * PrayClass
 * @author softm 
 */
public class PrayClass extends PrayBase {

	private static final String TAG = "PrayClass";
	public PrayClass() {
		super();
	}

	private String title;
	private String content;

	private PrayStatus prayStatus; //
	private PrayPartyStatus prayPartyStatus; //
	private PrayReqStatus prayReqStatus; //
	public boolean isParty; //

	public boolean isWait; //
	public boolean isIng; //
	public boolean isEnd; //

	private Timestamp prayDt; //
	private List<String> partyUuids ;
	private Map<String, PrayClassParty> parties ;

	private List<String> commentDocIds ;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getPrayDt() {
		return prayDt;
	}

	public void setPrayDt(Timestamp prayDt) {
		this.prayDt = prayDt;
	}
//	public void setPrayDt(String prayDt) {
//		this.prayDt = Fb.toTimestamp(prayDt);
//	}
	public Timestamp getwDate() {
		return wDate;
	}

	public void setwDate(Timestamp wDate) {
		this.wDate = wDate;
	}

	public Timestamp getuDate() {
		return uDate;
	}

	public void setuDate(Timestamp uDate) {
		this.uDate = uDate;
	}

	public Map<String, PrayClassParty> getParties() {
		return parties;
	}

	public void setParties(Map<String, PrayClassParty> parties) {
		this.parties = parties;
	}

	@Override
	public void gen() {
		super.gen();
		this.prayStatus = PrayStatus.BEFORE;
		this.prayPartyStatus = PrayPartyStatus.NONE;
		this.prayReqStatus = PrayReqStatus.NOT;
		genIsParty();
		genPrayStatus();
		genPrayReqStatus();
		genPrayPartyStatus();
		encodePayClass();
		encodeParties();
	}

	private void encodePayClass() {
		setTitle(encdodeForWeb(title));
		setContent(encdodeForWeb(content));
	}

	private void encodeParties() {
		if ( parties != null ) {
			for (Map.Entry<String, PrayClassParty> entry : parties.entrySet()) {
				PrayClassParty party = entry.getValue();
				party.gen();
			}
		}
	}

	public void genPrayStatus() {
		if ( this.prayDt.getSeconds() >= Timestamp.now().getSeconds() ) {
			this.prayStatus = PrayStatus.BEFORE;
			this.isWait = true;
		} else {
			if ( this.prayDt.getSeconds() + (WApplication.basicSettings.prayEndIntervalTimeSec) >= Timestamp.now().getSeconds() ) { /** 2시간동안은 진행중 **/
				this.prayStatus = PrayStatus.ING;
				this.isIng = true;
			} else {
				this.prayStatus = PrayStatus.END;
				this.isEnd = true;
			}
		}
	}

	public PrayStatus getPrayStatus() {
		return prayStatus;
	}

	public void setPrayStatus(PrayStatus procStatus) {
		this.prayStatus = procStatus;
	}

	public void genPrayReqStatus() {
		if( this.isParty ) {
			this.prayReqStatus = PrayReqStatus.DONE;
		} else {
			this.prayReqStatus = PrayReqStatus.NOT;
		}
	}

	public PrayReqStatus getPrayReqStatus() {
		return prayReqStatus;
	}

	public void setPrayReqStatus(PrayReqStatus prayReqStatus) {
		this.prayReqStatus = prayReqStatus;
	}

	public PrayPartyStatus getPrayPartyStatus() {
		return prayPartyStatus;
	}

	public void setPrayPartyStatus(PrayPartyStatus prayPartyStatus) {
		this.prayPartyStatus = prayPartyStatus;
	}

	public void genPrayPartyStatus() {
		if( this.parties!= null) {
			boolean doSet = false;
			for (Map.Entry<String, PrayClassParty> entry : parties.entrySet()) {
				String key = entry.getKey();  // 예: "abc01"
				Gson gson = new Gson();
				String json = gson.toJson(entry.getValue());  // HashMap을 JSON 문자열로 변환
				PrayClassParty party = gson.fromJson(json, PrayClassParty.class);  // 예: {"name": "name1", "regDt": "2025-01-01"}
				// "name"이 valueToCheck과 일치하는지 확인
				if (key.equals(WApplication.basicInfoDTO.uuid)) {
//					party.setPrayPartyStatus(party.getPrayPartyStatus());
//					System.out.println("값이 parties 객체에 포함되어 있습니다. key: " + key);
					if (Utils.isNotEmpty(party.getPrayPartyStatus()) ) {
						this.prayPartyStatus = party.getPrayPartyStatus();
						doSet = true;
					}
					break;
				}
			}
			if ( !doSet ) this.prayPartyStatus = PrayPartyStatus.NONE;
		} else {
			this.prayPartyStatus = PrayPartyStatus.NONE;
		}
	}
	public void genIsParty() {
		this.isParty = false;
		if( this.parties!= null) {
			for (Map.Entry<String, PrayClassParty> entry : parties.entrySet()) {
				String key = entry.getKey();  // 예: "abc01"
				Gson gson = new Gson();
				String json = gson.toJson(entry.getValue());  // HashMap을 JSON 문자열로 변환
				PrayClassParty party = gson.fromJson(json, PrayClassParty.class);  // 예: {"name": "name1", "regDt": "2025-01-01"}
				// "name"이 valueToCheck과 일치하는지 확인
				if (key.equals(WApplication.basicInfoDTO.uuid)) {
//					System.out.println("값이 parties 객체에 포함되어 있습니다. key: " + key);
					this.isParty = true;
					break;
				}
			}
		}
	}

	public void setParty(boolean party) {
		this.isParty = party;
	}

	public boolean isParty() {
		return isParty;
	}

	public List<String> getPartyUuids() {
		return partyUuids;
	}

	public void setPartyUuids(List<String> partyUuids) {
		this.partyUuids = partyUuids;
	}

	public List<String> getCommentDocIds() {
		return commentDocIds;
	}

	public void setCommentDocIds(List<String> commentDocIds) {
		this.commentDocIds = commentDocIds;
	}
}
