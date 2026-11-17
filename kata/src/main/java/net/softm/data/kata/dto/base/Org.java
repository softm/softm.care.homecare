package net.softm.data.kata.dto.base;

import net.softm.data.kata.dto.BaseDTO;

/**
 * Org
 * @author softm 
 */
public class Org extends BaseDTO {
	public Org() {
		super();
	}

	public String id; //
	public String name; //

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
