package net.softm.data.kata.dto;

/**
 * DefaultDTOOut
 * 기본DTO
 * @author softm 
 */
public class DefaultDTOOut extends BaseDTO {
	public DefaultDTOOut() {
		super();
	}

	public String state; //

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
