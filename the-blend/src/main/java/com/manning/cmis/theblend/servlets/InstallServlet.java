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
import java.io.PrintWriter;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Session;

import com.manning.cmis.theblend.install.CMISImporter;
import com.manning.cmis.theblend.install.CMISImporter.ImportProgress;
import com.manning.cmis.theblend.session.OpenCMISSessionFactory;

public class InstallServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_PASSWORD = "password";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// show installation login page
		dispatch("install.jsp", "Install. The Blend.", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String username = getStringParameter(request, PARAM_USERNAME);
		String password = getStringParameter(request, PARAM_PASSWORD);

		// create an OpenCMIS session
		final Session session;
		try {
			session = OpenCMISSessionFactory.createOpenCMISSession(username,
					password);
		} catch (Exception e) {
			error("Could not create session!", e, request, response);
			return;
		}

		getServletContext().getRealPath("/WEB-INF/data.zip");

		// open data Zip file
		final ZipFile zipFile;
		try {
			zipFile = new ZipFile(getServletContext().getRealPath(
					"/WEB-INF/data.zip"));
		} catch (Exception e) {
			error("Could not open data file!", e, request, response);
			return;
		}

		response.setContentType("text/plain");
		PrintWriter pw = response.getWriter();
		
		String path = OpenCMISSessionFactory.getApplicationRootFolderPath();

		CMISImporter importer = new CMISImporter(session, zipFile, path);
		importer.runImport(new Progress(pw));
		
		pw.flush();
	}

	public static class Progress implements ImportProgress {

		private PrintWriter pw;

		public Progress(PrintWriter pw) {
			this.pw = pw;
		}

		@Override
		public void startImport() {
			pw.println("Starting import...");
			pw.flush();
		}

		@Override
		public void endImport() {
			pw.println("Import finished.");
			pw.flush();
		}

		@Override
		public void startFile(String name) {
			pw.print("Importing '" + name + "'...");
			pw.flush();
		}

		@Override
		public void endFile(String name) {
			pw.println(".");
			pw.flush();
		}

		@Override
		public void message(String msg) {
			pw.print(msg);
			pw.flush();
		}
	}
}
