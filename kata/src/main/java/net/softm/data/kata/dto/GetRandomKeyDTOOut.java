package net.softm.data.kata.dto;

/**
 * GetRandomKeyDTOOut
 * 키패드 암호화 키교환
 * @author softm 
 */
public class GetRandomKeyDTOOut extends BaseDTO {
	public GetRandomKeyDTOOut() {
		super();
	}
	public byte[] mkey; //
	
	public String skey; //

	public byte[] getMkey() {
		return mkey;
	}

	public void setMkey(byte[] mkey) {
		this.mkey = mkey;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}
	
	
}
