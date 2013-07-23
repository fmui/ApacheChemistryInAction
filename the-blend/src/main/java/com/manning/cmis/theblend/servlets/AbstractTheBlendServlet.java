/**
 * Copyright 2012 Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manning.cmis.theblend.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.manning.cmis.theblend.session.OpenCMISSessionFactory;
import com.manning.cmis.theblend.util.CMISHelper;

public abstract class AbstractTheBlendServlet extends HttpServlet {

	public static final String JSP_DIRECTORY = "/WEB-INF/jsp/";

	public static final String PAGE_INDEX = "";
	public static final String PAGE_DASHBOARD = "dashboard";
	public static final String PAGE_BROWSE = "browse";
	public static final String PAGE_SHOW = "show";

	public static final String ATTR_TITLE = "title";

	private static final String HTTP_SESSION_SESSION = "session";
	private static final String HTTP_SESSION_APP_ROOT = "root";
	private static final String HTTP_SESSION_DOC_TYPES = "doctypes";
	private static final String HTTP_SESSION_FOLDER_TYPES = "foldertypes";

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {

		// get OpenCMIS Session
		Session session = getOpenCMISSession(request, response);
		if (session == null) {
			// no session -> forward to index (login) page
			redirect(PAGE_INDEX, request, response);
			return;
		}

		try {
			doGet(request, response, session);
		} catch (TheBlendException tbe) {
			error(tbe.getMessage(), tbe.getCause(), request, response);
		} catch (Exception e) {
			error(e.getMessage(), e, request, response);
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {

		// get OpenCMIS Session
		Session session = getOpenCMISSession(request, response);
		if (session == null) {
			// no session -> forward to index (login) page
			redirect(PAGE_INDEX, request, response);
			return;
		}

		try {
			doPost(request, response, session);
		} catch (TheBlendException tbe) {
			error(tbe.getMessage(), tbe.getCause(), request, response);
		} catch (Exception e) {
			error(e.getMessage(), e, request, response);
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {
	}

	/**
	 * Gets the OpenCMIS session from the HTTP session.
	 * 
	 * @return the OpenCMIS session or <code>null</code> if no OpenCMIS
	 *         is set
	 */
	protected Session getOpenCMISSession(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {
		Session session = null;

		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			session = (Session) httpSession
					.getAttribute(HTTP_SESSION_SESSION);
		}

		return session;
	}

	@SuppressWarnings("unchecked")
	protected List<ObjectType> getCreatableFolderTypes(
			HttpServletRequest request, HttpServletResponse response) {
		List<ObjectType> types = null;

		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			types = (List<ObjectType>) httpSession
					.getAttribute(HTTP_SESSION_FOLDER_TYPES);
		}

		return types;
	}

	@SuppressWarnings("unchecked")
	protected List<ObjectType> getCreatableDocumentTypes(
			HttpServletRequest request, HttpServletResponse response) {
		List<ObjectType> types = null;

		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			types = (List<ObjectType>) httpSession
					.getAttribute(HTTP_SESSION_DOC_TYPES);
		}

		return types;
	}

	protected String getApplicationRootFolderId(
			HttpServletRequest request) {
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			return (String) httpSession.getAttribute(HTTP_SESSION_APP_ROOT);
		}

		return null;
	}

	protected void setOpenCMISSession(HttpServletRequest request,
			Session session) {
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute(HTTP_SESSION_SESSION, session);

		// get the id of the application root folder and
		// store it in the HTTP session
		String applicationRootFolderId = createApplicationRootFolder(
				session,
				OpenCMISSessionFactory.getApplicationRootFolderPath())
				.getId();

		httpSession.setAttribute(HTTP_SESSION_APP_ROOT,
				applicationRootFolderId);

		// get all creatable document types and
		// store them in the HTTP session
		List<ObjectType> documentTypes = CMISHelper.getCreatableTypes(
				session, BaseTypeId.CMIS_DOCUMENT.value());

		httpSession.setAttribute(HTTP_SESSION_DOC_TYPES, documentTypes);

		// get all creatable folder types and
		// store them in the HTTP session
		List<ObjectType> folderTypes = CMISHelper.getCreatableTypes(
				session, BaseTypeId.CMIS_FOLDER.value());

		httpSession.setAttribute(HTTP_SESSION_FOLDER_TYPES, folderTypes);
	}

	/**
	 * Gets the application root folder and if it doesn't exist, creates
	 * it.
	 */
	protected Folder createApplicationRootFolder(Session session,
			String path) {
		try {
			// get the folder
			return (Folder) session.getObjectByPath(path,
					CMISHelper.LIGHT_OPERATION_CONTEXT);
		} catch (CmisObjectNotFoundException nfe) {
			// folder doesn't exist -> create it

			int x = path.lastIndexOf('/');

			Folder parent = null;
			if (x == 0) {
				parent = session.getRootFolder();
			} else {
				parent = createApplicationRootFolder(session,
						path.substring(0, x));
			}

			// create folder
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.NAME, path.substring(x + 1));

			return parent.createFolder(properties, null, null, null,
					CMISHelper.LIGHT_OPERATION_CONTEXT);
		}
	}

	/**
	 * Dispatches to a JSP page.
	 */
	protected void dispatch(String jsp, String title,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute(ATTR_TITLE, title);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher(JSP_DIRECTORY + jsp);
		dispatcher.include(request, response);
	}

	/**
	 * Redirect to another page.
	 */
	protected void redirect(String url, HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {

		response.sendRedirect(url);
	}

	/**
	 * Forwards to an JSP that displays the given error message.
	 */
	protected void error(String message, Throwable t,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("message", message);
		request.setAttribute("exception", t);

		// show error page
		dispatch("error.jsp", "Error.", request, response);
	}

	/**
	 * Gets a string parameter.
	 */
	protected String getStringParameter(HttpServletRequest request,
			String name) {
		return request.getParameter(name);
	}

	/**
	 * Gets a string parameter and throws an exception if it is not set.
	 */
	protected String getRequiredStringParameter(
			HttpServletRequest request, String name)
			throws TheBlendException {
		String value = request.getParameter(name);

		if (value == null || value.length() == 0) {
			throw new TheBlendException("The parameter " + name
					+ " is not set!");
		}

		return value;
	}

	/**
	 * Gets an integer parameter. If it is not set or invalid (not a
	 * number), it returns the given default value.
	 */
	protected int getIntParameter(HttpServletRequest request,
			String name, int defValue) {
		String value = getStringParameter(request, name);
		if (value == null) {
			return defValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return defValue;
		}
	}

	/**
	 * Gets a boolen parameter. If it is not set, it returns the given
	 * default value.
	 */
	protected boolean getBooleanParameter(HttpServletRequest request,
			String name, boolean defValue) {
		String value = getStringParameter(request, name);
		if (value == null) {
			return defValue;
		}

		return Boolean.parseBoolean(value);
	}
}
