package net.softm.data.kata.god.dto;

import static net.softm.data.kata.util.Utils.encdodeForWeb;

import com.google.firebase.Timestamp;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.OrgUtil;

import java.util.List;

/**
 * PrayClass
 * @author softm 
 */
public class PrayBase {
	public PrayBase() {
		super();
		this.orgId = OrgUtil.getOrg();
		this.groups = OrgUtil.getGroups();
		this.profileName = OrgUtil.getProfileName();
		this.nickName = OrgUtil.getNickName();
	}

	public String docId; //
	public String uuid; //
	public Timestamp wDate; //
	public Timestamp uDate; //
	public List<String> groups ;
	public String orgId; //

	private String profileName; //
	private String nickName; //
	private String cellPhone; //
	private String email; //
	private String birthDay; //
	private String address; //
	private String sex; //
	private int age; //
	public boolean isOwn() {
		return isOwn;
	}

	public void setOwn(boolean own) {
		isOwn = own;
	}

	public boolean isOwn; //

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

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public void gen() {
		this.isOwn = this.uuid.equals(DeviceUtil.getUUID(WApplication.mInstance));
		setProfileName(encdodeForWeb(profileName));
		setNickName(encdodeForWeb(nickName));
    }

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(String birthDay) {
		this.birthDay = birthDay;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PrayBase other = (PrayBase) obj;
		return this.docId != null && this.docId.equals(other.docId) ; // docId가 같은지 비교
	}

	@Override
	public int hashCode() {
		return docId != null ? docId.hashCode() : 0; // docId를 기준으로 hashCode를 계산
	}
}
