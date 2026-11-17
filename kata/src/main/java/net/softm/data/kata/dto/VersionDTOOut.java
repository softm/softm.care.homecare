package net.softm.data.kata.dto;

/**
 * VersionDTOOut
 * 기본DTO
 * @author softm 
 */
public class VersionDTOOut extends BaseDTO {
	public VersionDTOOut() {
		super();
	}

	public String errCd; //

//	JSONObject json = new JSONObject(version);
//	JSONObject rsData = json.getJSONObject("rsData");
//	JSONObject head = rsData.getJSONObject("head");
//	ERR_CD = head.getString("ERR_CD");
//

	public String getErrCd() {
		return errCd;
	}

	public void setErrCd(String state) {
		this.errCd = errCd;
	}
}
