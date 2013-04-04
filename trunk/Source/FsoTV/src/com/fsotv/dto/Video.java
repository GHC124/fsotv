package com.fsotv.dto;

public class Video {
	private int idVideo;
	private int idChannel;
	private String nameVideo;
	private String uri;
	private String thumnail;
	private String describes;
	private String account;
	private int typeVideo;

	public int getIdVideo() {
		return idVideo;
	}

	public void setIdVideo(int idVideo) {
		this.idVideo = idVideo;
	}

	public int getIdChannel() {
		return idChannel;
	}

	public void setIdChannel(int idChannel) {
		this.idChannel = idChannel;
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

	public Video() {
		// TODO Auto-generated constructor stub
	}

	public Video(int idVideo, int idChannel, String nameVideo, String uri,
			String thumnail, String describes, String account, int typeVideo) {
		this.idVideo = idVideo;
		this.idChannel = idChannel;
		this.nameVideo = nameVideo;
		this.uri = uri;
		this.thumnail = thumnail;
		this.describes = describes;
		this.account = account;
		this.typeVideo = typeVideo;
	}

}
