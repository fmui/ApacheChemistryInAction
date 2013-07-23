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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;

public abstract class AbstractTheBlendServlet extends HttpServlet {
	//<start id="ch06_abstract_constants" />
	public static final String JSP_DIRECTORY = "/WEB-INF/jsp/";

	public static final String PAGE_INDEX = "";

	public static final String ATTR_TITLE = "title";

	private static final String HTTP_SESSION_SESSION = "session";

	private static final long serialVersionUID = 1L;
	//<end id="ch06_abstract_constants" />
	//<start id="ch06_abstract_doGet" />
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		// get OpenCMIS Session
		Session session = getOpenCMISSession(request, response);//<co id="ch06_get_session_tbd" />
		if (session == null) {
			// no session -> forward to index (login) page
			redirect("", request, response);//<co id="ch06_no_session" />
			return;
		}

		try {
			doGet(request, response, session);//<co id="ch06_call_doGet" />
		} catch (TheBlendException tbe) {
			error(tbe.getMessage(), tbe.getCause(), request, response);
		} catch (Exception e) {
			error(e.getMessage(), e, request, response);
		}
	}
	//<end id="ch06_abstract_doGet" />
	//<start id="ch06_abstract_doGet_cmis" />
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {
	}
	//<end id="ch06_abstract_doGet_cmis" />
	//<start id="ch06_abstract_doPosts" />
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		// get OpenCMIS Session
		Session session = getOpenCMISSession(request, response);
		if (session == null) {
			// no session -> forward to index (login) page
			redirect("", request, response);
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
	//<end id="ch06_abstract_doPosts" />
	//<start id="ch06_abstract_getOpenCMISSession" />
	protected Session getOpenCMISSession(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		Session session = null;

		HttpSession httpSession = request.getSession(false);//<co id="ch06_get_httpSession" />
		if (httpSession != null) {
			session = (Session) httpSession.
					getAttribute(HTTP_SESSION_SESSION);//<co id="ch06_get_cmis_session" />
		}

		return session;
	}

	protected void setOpenCMISSession(
			HttpServletRequest request,
			Session session) {
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute(HTTP_SESSION_SESSION, session);//<co id="ch06_set_cmis_session" />
	}
	//<end id="ch06_abstract_getOpenCMISSession" />
	/**
	 * Dispatches to a JSP page.
	 */
	//<start id="ch06_abstract_dispatch" />
	protected void dispatch(String jsp, String title,
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute(ATTR_TITLE, title);

		RequestDispatcher dispatcher = request
				.getRequestDispatcher(JSP_DIRECTORY + jsp);
		dispatcher.include(request, response);
	}

	/**
	 * Redirect to another page.
	 */
	protected void redirect(String url,
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		response.sendRedirect(url);
	}

	/**
	 * Forwards to an error message.
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void error(String msg, Throwable t,
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		request.setAttribute("message", msg);
		request.setAttribute("exception", t);

		// show error page
		dispatch("error.jsp", "Error.", request, response);
	}
	//<end id="ch06_abstract_dispatch" />
	//<start id="ch06_index_getStringParameter" />
	protected String getStringParameter(
			HttpServletRequest request,
			String name) {
		return request.getParameter(name);
	}
	
	protected int getIntParameter(
			HttpServletRequest request,
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
	//<end id="ch06_index_getStringParameter" />
}
