package com.fsotv.dto;

public class Role {
	/* properties */

	private int idRole;
	private String nameRole;

	/* get - set properties */
	public int getIdRole() {
		return idRole;
	}

	public void setIdRole(int idRole) {
		this.idRole = idRole;
	}

	public String getNameRole() {
		return nameRole;
	}

	public void setNameRole(String nameRole) {
		this.nameRole = nameRole;
	}

	/* constructor */
	public Role() {
		// TODO Auto-generated constructor stub
	}

	public Role(int idRole, String nameRole) {
		this.idRole = idRole;
		this.nameRole = nameRole;
	}

}
