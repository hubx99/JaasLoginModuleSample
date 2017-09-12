package com.example.login.principal;

import java.security.Principal;

public class UserPrincipal implements Principal {
	private String name;

	public UserPrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof UserPrincipal) {
			return ((UserPrincipal) obj).getName().equals(this.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "UserPrincipal : " + this.name;
	}
}