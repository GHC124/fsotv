package com.fsotv.dto;

public class Reference {
	int id;
	String key;
	String value;
	String extras;
	String display;
	
	public Reference(){
		
	}
	
	public Reference(int id, String key, String value, String display, String extras) {
		super();
		this.id = id;
		this.key = key;
		this.value = value;
		this.display = display;
		this.extras = extras;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getExtras() {
		return extras;
	}
	public void setExtras(String extras) {
		this.extras = extras;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}
	
}
