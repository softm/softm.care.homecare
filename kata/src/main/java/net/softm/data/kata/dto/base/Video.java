package net.softm.data.kata.dto.base;

import net.softm.data.kata.dto.BaseDTO;

/**
 * Group
 * Group
 * @author softm 
 */
public class Video extends BaseDTO {
	public Video() {
		super();
	}

	public String id; //
	public String url; //
	public String videoId; //
	public String title; //
	public String wDate; //

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getwDate() {
		return wDate;
	}

	public void setwDate(String wDate) {
		this.wDate = wDate;
	}
}
