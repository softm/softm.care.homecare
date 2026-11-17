package net.softm.data.kata.god.dto;

/**
 * PrayClassParty
 * @author softm 
 */
public class PrayClassParty extends PrayBase {
	private static final String TAG = "PrayClassParty";

	public PrayClassParty() {
		super();
	}
	private PrayPartyStatus prayPartyStatus; //

	public PrayPartyStatus getPrayPartyStatus() {
		return prayPartyStatus;
	}

	public void setPrayPartyStatus(PrayPartyStatus prayPartyStatus) {
		this.prayPartyStatus = prayPartyStatus;
	}
}
