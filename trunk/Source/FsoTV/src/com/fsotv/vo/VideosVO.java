/**
 * 
 */
package com.fsotv.vo;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * @author CuongVM1
 *
 */
public class VideosVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8835148669703740749L;

	@SerializedName("apiVersion")
	private String apiVersion;
	
	@SerializedName("data")
	private VideoDataVO videoData;
	
	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	/**
	 * @return the videoData
	 */
	public VideoDataVO getVideoData() {
		return videoData;
	}

	/**
	 * @param videoData the videoData to set
	 */
	public void setVideoData(VideoDataVO videoData) {
		this.videoData = videoData;
	}

		
}
