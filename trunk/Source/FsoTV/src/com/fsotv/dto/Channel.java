package com.fsotv.dto;

public class Channel {
	private int idChannel;
	private String nameChannel;
	private String uri;
	private String thumnail;
	private String describes;
	private String idRealChannel;
	
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
	
	public Channel(){
		
	}

	public Channel(int idChannel, String nameChannel, String uri,
			String thumnail, String describes, String idRealChannel) {
		this.idChannel = idChannel;
		this.nameChannel = nameChannel;
		this.uri = uri;
		this.thumnail = thumnail;
		this.describes = describes;
		this.idRealChannel = idRealChannel;
	}

}
