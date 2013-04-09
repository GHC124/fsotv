package com.fsotv.dto;

public class Video {
	private int idVideo;
	private int idCategory;
	private String nameVideo;
	private String uri;
	private String thumnail;
	private String describes;
	private String account;
	private int typeVideo;
	private String idRealVideo;
	private long duration;
	private int viewCount;
	private int favoriteCount;
	
	public int getIdVideo() {
		return idVideo;
	}

	public void setIdVideo(int idVideo) {
		this.idVideo = idVideo;
	}

	public int getIdCategory() {
		return idCategory;
	}

	public void setIdCategory(int idCategory) {
		this.idCategory = idCategory;
	}

	public String getNameVideo() {
		return nameVideo;
	}

	public void setNameVideo(String nameVideo) {
		this.nameVideo = nameVideo;
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

	public String getDescribes() {
		return describes;
	}

	public void setDescribes(String describes) {
		this.describes = describes;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public int getTypeVideo() {
		return typeVideo;
	}

	public void setTypeVideo(int typeVideo) {
		this.typeVideo = typeVideo;
	}

	public String getIdRealVideo() {
		return idRealVideo;
	}

	public void setIdRealVideo(String idRealVideo) {
		this.idRealVideo = idRealVideo;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public Video() {
		// TODO Auto-generated constructor stub
	}

	public Video(int idVideo, int idCategory, String nameVideo, String uri,
			String thumnail, String describes, String account, int typeVideo,
			String idRealVideo, long duration, int viewCount, int favoriteCount) {
		super();
		this.idVideo = idVideo;
		this.idCategory = idCategory;
		this.nameVideo = nameVideo;
		this.uri = uri;
		this.thumnail = thumnail;
		this.describes = describes;
		this.account = account;
		this.typeVideo = typeVideo;
		this.idRealVideo = idRealVideo;
		this.duration = duration;
		this.viewCount = viewCount;
		this.favoriteCount = favoriteCount;
	}


}
