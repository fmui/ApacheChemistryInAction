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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.chemistry.opencmis.client.api.Session;

import com.manning.cmis.theblend.session.OpenCMISSessionFactory;

/**
 * Index page.
 */
public class IndexServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_LOGOUT = "logout";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (getStringParameter(request, PARAM_LOGOUT) != null) {
			HttpSession httpSession = request.getSession(false);
			if (httpSession != null) {
				httpSession.invalidate();
			}
		}

		// just show index page
		dispatch("index.jsp", "The Blend.", request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String username = getStringParameter(request, PARAM_USERNAME);
		String password = getStringParameter(request, PARAM_PASSWORD);

		try {
			Session session = OpenCMISSessionFactory.createOpenCMISSession(
					username, password);
			setOpenCMISSession(request, session);

			redirect(PAGE_DASHBOARD, request, response);
		} catch (Exception e) {
			error("Could not create OpenCMIS session: " + e, e, request,
					response);
			return;
		}

		// show index page
		dispatch("index.jsp", "The Blend.", request, response);
	}
}
