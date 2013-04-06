/**
 * 
 */
package com.fsotv.vo;

import com.google.gson.annotations.SerializedName;

/**
 * @author CuongVM1
 * 
 */
public class ThumbnailVO {
	@SerializedName("sqDefault")
	private String smallDefault;

	@SerializedName("hqDefault")
	private String highDefault;
	
	public ThumbnailVO() {
		
	}

	public String getSmallDefault() {
		return smallDefault;
	}

	public void setSmallDefault(String smallDefault) {
		this.smallDefault = smallDefault;
	}

	public String getHighDefault() {
		return highDefault;
	}

	public void setHighDefault(String highDefault) {
		this.highDefault = highDefault;
	}
}
