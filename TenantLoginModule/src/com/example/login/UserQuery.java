package com.example.login;

public class UserQuery {
	private String userTable;
	private String tenantIdColumn;
	private String userIdColumn;
	private String userNameColumn;
	private String userCredColumn;
	private String roleTable;
	private String roleNameColumn;

	public void setUserTable(String userTable) {
		this.userTable = userTable;
	}

	public void setTenantIdColumn(String tenantIdColumn) {
		this.tenantIdColumn = tenantIdColumn;
	}

	public void setUserIdColumn(String userIdColumn) {
		this.userIdColumn = userIdColumn;
	}

	public void setUserNameColumn(String userNameColumn) {
		this.userNameColumn = userNameColumn;
	}

	public void setUserCredColumn(String userCredColumn) {
		this.userCredColumn = userCredColumn;
	}

	public void setRoleTable(String roleTable) {
		this.roleTable = roleTable;
	}

	public void setRoleNameColumn(String roleNameColumn) {
		this.roleNameColumn = roleNameColumn;
	}

	public String buildUserQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(userNameColumn).append(" FROM ").append(userTable);
		sb.append(" WHERE ").append(tenantIdColumn).append("=? AND ");
		sb.append(userIdColumn).append("=? AND ").append(userCredColumn).append("=?");
		return sb.toString();
	}

	public String buildRoleQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(roleNameColumn).append(" FROM ").append(roleTable);
		sb.append(" WHERE ").append(tenantIdColumn).append("=? AND ");
		sb.append(userIdColumn).append("=?");
		return sb.toString();
	}
}
