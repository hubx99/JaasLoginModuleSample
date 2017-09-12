package com.example.login.principal;

import java.security.Principal;

public class RolePrincipal implements Principal {
	private String name;

	public RolePrincipal(String name) {
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
		if (obj instanceof RolePrincipal) {
			return ((RolePrincipal) obj).getName().equals(this.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "RolePrincipal : " + this.name;
	}
}
