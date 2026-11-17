package net.softm.data.kata.dto;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * VersionDTOOut
 * 기본DTO
 * @author softm 
 */
public class AppInfoDTO extends BaseDTO {
	public AppInfoDTO() {
		super();
	}

	public ApplicationInfo detail; //
	public String name; //
	public String packageName; //
	public Drawable icon; //
	public String enCodeicon; //

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getEnCodeicon() {
		return enCodeicon;
	}

	public void setEnCodeicon(String enCodeicon) {
		this.enCodeicon = enCodeicon;
	}

	public ApplicationInfo getDetail() {
		return detail;
	}

	public void setDetail(ApplicationInfo detail) {
		this.detail = detail;
	}
}
