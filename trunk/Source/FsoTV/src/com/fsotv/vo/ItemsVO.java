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
public abstract class ItemsVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1873828665792627179L;

	@SerializedName("id")
	private String id;

	@SerializedName("uploaded")
	private String uploaded;

	@SerializedName("uploader")
	private String uploader;

	@SerializedName("category")
	private String category;

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("thumbnail")
	private ThumbnailVO thumbnail;

	@SerializedName("duration")
	private String duration;
	
	@SerializedName("rating")
	private String rating;
	
	@SerializedName("ratingCount")
	private String ratingCount;
	
	@SerializedName("viewCount")
	private String viewCount;
	
	@SerializedName("commentCount")
	private String commentCount;
	
	public ItemsVO() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUploaded() {
		return uploaded;
	}

	public void setUploaded(String uploaded) {
		this.uploaded = uploaded;
	}

	public String getUploader() {
		return uploader;
	}

	public void setUploader(String uploader) {
		this.uploader = uploader;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the thumbnail
	 */
	public ThumbnailVO getThumbnail() {
		return thumbnail;
	}

	/**
	 * @param thumbnail the thumbnail to set
	 */
	public void setThumbnail(ThumbnailVO thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * @return the rating
	 */
	public String getRating() {
		return rating;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(String rating) {
		this.rating = rating;
	}

	/**
	 * @return the ratingCount
	 */
	public String getRatingCount() {
		return ratingCount;
	}

	/**
	 * @param ratingCount the ratingCount to set
	 */
	public void setRatingCount(String ratingCount) {
		this.ratingCount = ratingCount;
	}

	/**
	 * @return the viewCount
	 */
	public String getViewCount() {
		return viewCount;
	}

	/**
	 * @param viewCount the viewCount to set
	 */
	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}

	/**
	 * @return the commentCount
	 */
	public String getCommentCount() {
		return commentCount;
	}

	/**
	 * @param commentCount the commentCount to set
	 */
	public void setCommentCount(String commentCount) {
		this.commentCount = commentCount;
	}

	

	
}
