package com.fsotv.dto;

public class Subscribe {
	private String account;
	private String idChannel;
	private String nameChannel;
	

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getIdChannel() {
		return idChannel;
	}

	public void setIdChannel(String idChannel) {
		this.idChannel = idChannel;
	}

	public String getNameChannel() {
		return nameChannel;
	}

	public void setNameChannel(String nameChannel) {
		this.nameChannel = nameChannel;
	}

	public Subscribe() {
		// TODO Auto-generated constructor stub
	}

	public Subscribe(String account, String idChannel, String nameChannel) {
		this.account = account;
		this.idChannel = idChannel;
		this.nameChannel = nameChannel;
	}

}
