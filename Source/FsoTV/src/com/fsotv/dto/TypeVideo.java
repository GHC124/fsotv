package com.fsotv.dto;

public class TypeVideo {

	private int idType;
	private String nameType;

	public int getIdType() {
		return idType;
	}

	public void setIdType(int idType) {
		this.idType = idType;
	}

	public String getNameType() {
		return nameType;
	}

	public void setNameType(String nameType) {
		this.nameType = nameType;
	}

	public TypeVideo() {
		// TODO Auto-generated constructor stub
	}

	public TypeVideo(int idType, String nameType) {
		this.idType = idType;
		this.nameType = nameType;
	}

}
