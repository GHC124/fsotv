package com.fsotv.dto;

public class Channel {
	/* properties */
	private int idChannel;
	private String nameChannel;
	private String uri;
	private String thumnail;
	private String describes;
	private String idRealChannel;
	int commentCount;
	int videoCount;
	int viewCount;
	int subscriberCount;
	
	
	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getVideoCount() {
		return videoCount;
	}

	public void setVideoCount(int videoCount) {
		this.videoCount = videoCount;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getSubscriberCount() {
		return subscriberCount;
	}

	public void setSubscriberCount(int subscriberCount) {
		this.subscriberCount = subscriberCount;
	}

	public String getDescribes() {
		return describes;
	}

	public void setDescribes(String describes) {
		this.describes = describes;
	}

	public int getIdChannel() {
		return idChannel;
	}

	public void setIdChannel(int idChannel) {
		this.idChannel = idChannel;
	}

	public String getNameChannel() {
		return nameChannel;
	}

	public void setNameChannel(String nameChannel) {
		this.nameChannel = nameChannel;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getThumnail() {
		return thumnail;
	}

	public void setThumnail(String thumnail) {
		this.thumnail = thumnail;
	}

	public String getIdRealChannel() {
		return idRealChannel;
	}

	public void setIdRealChannel(String idRealChannel) {
		this.idRealChannel = idRealChannel;
	}

	/* constructor */
	public Channel() {

	}

	public Channel(int idChannel, String nameChannel, String uri,
			String thumnail, String describes, String idRealChannel,
			int commentCount, int videoCount, int viewCount, int subscriberCount) {
		super();
		this.idChannel = idChannel;
		this.nameChannel = nameChannel;
		this.uri = uri;
		this.thumnail = thumnail;
		this.describes = describes;
		this.idRealChannel = idRealChannel;
		this.commentCount = commentCount;
		this.videoCount = videoCount;
		this.viewCount = viewCount;
		this.subscriberCount = subscriberCount;
	}

	
}
