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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.manning.cmis.theblend.util.CMISHelper;

public class DownloadServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_SAVE = "save";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// get OpenCMIS Session
		Session session = getOpenCMISSession(request, response);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Not logged in!");
			return;
		}

		String id = getStringParameter(request, PARAM_ID);
		String save = getStringParameter(request, PARAM_SAVE);

		// fetch the document object
		CmisObject cmisObject = null;
		try {
			cmisObject = session.getObject(id,
					CMISHelper.LIGHT_OPERATION_CONTEXT);
		} catch (CmisObjectNotFoundException onfe) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Document not found!");
			return;
		} catch (CmisBaseException cbe) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error: " + cbe.getMessage());
			return;
		}

		if (!(cmisObject instanceof Document)) {
			// object is not a document -> no content
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Object is not a document!");
			return;
		}

		Document doc = (Document) cmisObject;
		ContentStream contentStream = doc.getContentStream();

		if (contentStream == null) {
			// document has no content
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Document has no content!");
			return;
		}

		InputStream in = contentStream.getStream();
		try {
			// set MIME type
			String mimeType = contentStream.getMimeType();
			if (mimeType == null || mimeType.length() == 0) {
				// if the repository didn't send a MIME type,
				// use a generic one
				mimeType = "application/octet-stream";
			}

			response.setContentType(mimeType);

			// if the 'save' parameter is set, ask the browser to open a
			// download dialog by setting a Content-Disposition attachment
			// header
			if (Boolean.parseBoolean(save)) {
				// set filename
				String filename = contentStream.getFileName();
				if (filename == null) {
					// if the repository didn't send a filename, use the
					// document name
					filename = doc.getName();
				}

				// !!! A real application should implement RFC 2231 !!!
				// this is good enough for a demo
				response.setHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");
			} else {
				response.setHeader("Content-Disposition", "inline");
			}

			// push out content
			OutputStream out = response.getOutputStream();

			byte[] buffer = new byte[64 * 1024];
			int b;
			while ((b = in.read(buffer)) > -1) {
				out.write(buffer, 0, b);
			}

			out.flush();
		} finally {
			// VERY IMPORTANT:
			// always close the content stream
			in.close();
		}
	}
}