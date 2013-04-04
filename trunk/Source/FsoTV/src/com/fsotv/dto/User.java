package com.fsotv.dto;

public class User {
	private String account;
	private String role;
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public User() {
		// TODO Auto-generated constructor stub
	}

	public User(String account, String role, String password) {
		this.account = account;
		this.role = role;
		this.password = password;
	}

}
