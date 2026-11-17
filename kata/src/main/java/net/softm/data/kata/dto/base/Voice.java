package net.softm.data.kata.dto.base;

import net.softm.data.kata.dto.BaseDTO;

/**
 * Group
 * Group
 * @author softm 
 */
public class Voice extends BaseDTO {
	public Voice() {
		super();
	}

	public String id; //
	public String title; //
	public String verse; //
	public String content; //
	public String wDate; //

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVerse() {
		return verse;
	}

	public void setVerse(String verse) {
		this.verse = verse;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getwDate() {
		return wDate;
	}

	public void setwDate(String wDate) {
		this.wDate = wDate;
	}
}
