package com.example.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;

import com.example.login.principal.RolePrincipal;
import com.example.login.principal.UserPrincipal;

public class TenantLoginModule implements LoginModule {
	private CallbackHandler callbackHandler;
	private Subject subject;
	private UserPrincipal userPrincipal;
	private List<RolePrincipal> rolePrincipals;
	private String loginId;
	private List<String> userGroups;

	private String jndiResource;
	private UserQuery userQuery;

	private boolean debug;
	private Consumer<String> logger;

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {
		this.callbackHandler = callbackHandler;
		this.subject = subject;
		this.jndiResource = (String) options.get("JNDI_RESOURCE");
		this.userQuery = new UserQuery();
		userQuery.setUserTable((String) options.get("USER_TABLE"));
		userQuery.setUserNameColumn((String) options.get("USER_NAME_COLUMN"));
		userQuery.setTenantIdColumn((String) options.get("TENANT_ID_COLUMN"));
		userQuery.setUserIdColumn((String) options.get("USER_ID_COLUMN"));
		userQuery.setUserCredColumn((String) options.get("USER_CREDENTIAL_COLUMN"));
		userQuery.setRoleTable((String) options.get("ROLE_TABLE"));
		userQuery.setRoleNameColumn((String) options.get("ROLE_NAME_COLUMN"));

		debug = Boolean.TRUE.toString().equals(options.get("debug"));
		if (debug) {
			logger = (s) -> {
				System.out.println(s);
			};
		} else {
			logger = (s) -> {
			};
		}
	}

	@Override
	public boolean login() throws LoginException {
		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("tenant+id");
		callbacks[1] = new PasswordCallback("password", true);

		try {
			callbackHandler.handle(callbacks);

			String name = ((NameCallback) callbacks[0]).getName();
			String pass = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());
			logger.accept("NameCallback: " + name);
			logger.accept("PasswordCallback: " + pass);

			if (name == null || pass == null) {
				throw new IllegalArgumentException("tenant id pass required");
			}

			String[] ids = name.split("\n");
			if (ids.length != 2) {
				throw new IllegalArgumentException("id format is invalid. format: [tenant\\nid]");
			}

			Context context = (Context) new InitialContext().lookup("java:comp/env");
			DataSource ds = (DataSource) context.lookup(jndiResource);

			try (Connection con = ds.getConnection()) {
				con.setAutoCommit(false);
				logger.accept("tenant: " + ids[0]);
				logger.accept("userid: " + ids[1]);

				boolean authed = selectUser(con, ids[0], ids[1], pass);
				logger.accept("auth success: " + authed);
				if (authed) {
					selectRole(con, ids[0], ids[1]);
					logger.accept(ids[0] + ", " + ids[1] + ": " + userGroups);
					return true;
				}
			}
		} catch (Exception e) {
			throw new LoginException(e.getMessage());
		}
		throw new FailedLoginException();
	}

	@Override
	public boolean commit() throws LoginException {
		Optional.ofNullable(loginId).ifPresent(uid -> {
			userPrincipal = new UserPrincipal(loginId);
			subject.getPrincipals().add(userPrincipal);
		});
		Optional.ofNullable(userGroups).filter(groups -> !groups.isEmpty()).ifPresent(groups -> {
			rolePrincipals = groups.stream().map(group -> new RolePrincipal(group)).collect(Collectors.toList());
			subject.getPrincipals().addAll(rolePrincipals);
		});
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return false;
	}

	@Override
	public boolean logout() throws LoginException {
		subject.getPrincipals().remove(userPrincipal);
		subject.getPrincipals().removeAll(rolePrincipals);
		return true;
	}

	private boolean selectUser(Connection con, String tenantId, String id, String pass) throws SQLException {
		String query = userQuery.buildUserQuery();
		logger.accept(query);
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setString(1, tenantId);
			stmt.setString(2, id);
			stmt.setString(3, pass);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					loginId = rs.getString(1);
					return true;
				}
			}
		}
		return false;
	}

	private void selectRole(Connection con, String tenantId, String id) throws SQLException {
		String query = userQuery.buildRoleQuery();
		logger.accept(query);
		try (PreparedStatement stmt = con.prepareStatement(query)) {
			stmt.setString(1, tenantId);
			stmt.setString(2, id);
			userGroups = new ArrayList<>();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String role = rs.getString(1);
					if (role != null && !role.isEmpty()) {
						userGroups.add(role);
					}
				}
			}
		}
	}
}