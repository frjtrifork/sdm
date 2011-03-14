package com.trifork.sdm.replication.gui.controllers;


import static com.trifork.sdm.replication.db.properties.Database.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.trifork.sdm.replication.db.properties.Transactional;
import com.trifork.sdm.replication.gui.annotations.Whitelist;
import com.trifork.sdm.replication.gui.models.*;


@Singleton
public class UserController extends AbstractController {

	private static final long serialVersionUID = 6245011626700765816L;

	private final UserDao users;
	private final Set<String> whitelist;

	private final AuditLog audit;

	private final PageRenderer renderer;


	@Inject
	public UserController(@Whitelist Set<String> whitelist, UserDao users, AuditLog audit, PageRenderer renderer) {

		this.whitelist = whitelist;
		this.users = users;
		this.audit = audit;
		this.renderer = renderer;
	}


	@Override
	@Transactional(ADMINISTRATION)
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// Check if the user requested a form to create
			// a new user.

			if (request.getRequestURI().endsWith("/new")) {
				getNew(request, response);
			}
			else if (request.getParameter("id") == null) {
				// If the ID parameter is null, we list all
				// users. If it is present we show a specific user.

				getList(request, response);
			}
			else {
				getEdit(request, response);
			}
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
	}


	@Override
	@Transactional(ADMINISTRATION)
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String method = request.getParameter("method");

			if ("DELETE".equals(method)) {
				getDelete(request, response);
			}
			else {
				getCreate(request, response);
			}
		}
		catch (Throwable e) {
			throw new ServletException(e);
		}
	}


	protected void getCreate(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

		// Get the new administrator's info from the
		// HTML form.

		String newUserName = request.getParameter("name");
		String newUserCPR = request.getParameter("cpr");
		String newUserCVR = request.getParameter("firm");

		User user = users.create(newUserName, newUserCPR, newUserCVR);

		if (!whitelist.contains(newUserCVR)) {
			// TODO: Log and write the CVR.
		}
		else if (user != null) {
			audit.write("New administrator created (new_user_cpr=%s, new_user_cvr=%s). Created by user_cpr=%s.", newUserCPR, newUserCVR, getUserCPR(request));
		}

		redirect(request, response, "/admin/users");
	}


	protected void getDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		String id = request.getParameter("id");

		User deletedUser = users.find(id);

		if (deletedUser != null) {
			String userCPR = getUserCPR(request);
			users.destroy(id);
			audit.write("Administrator '%s (ID=%s)' was deleted by user %s.", deletedUser.getName(), deletedUser.getId(), userCPR);
		}

		redirect(request, response, "/admin/users");
	}


	protected void getList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, SQLException {
		Map<String, Object> root = new HashMap<String, Object>();

		root.put("users", users.findAll());

		renderer.render("/user/list.ftl", root, request, response);
	}


	protected void getNew(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		// List the white listed firms (CVR).

		Map<String, Object> root = new HashMap<String, Object>();

		root.put("firms", whitelist);

		renderer.render("/user/new.ftl", root, request, response);
	}


	protected void getEdit(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ServletException {
		Map<String, Object> root = new HashMap<String, Object>();

		String id = request.getParameter("id");

		User user = users.find(id);
		root.put("user", user);

		renderer.render("/user/edit.ftl", root, request, response);
	}
}