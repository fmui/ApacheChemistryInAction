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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class AddVersionServlet extends AbstractAddServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_DOC_ID = "id";
	private static final String PARAM_MAJOR = "major";

	public static final String ATTR_OBJECT = "object";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getStringParameter(request, PARAM_DOC_ID);

		// fetch the document object
		Document doc = CMISHelper.getDocumet(session, id,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "document");

		request.setAttribute(ATTR_OBJECT, doc);

		// show add version page
		dispatch("addversion.jsp", "Add new version. The Blend.", request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		// check for multipart content
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			// show add version page
			dispatch("addversion.jsp", "Add new version. The Blend.", request,
					response);
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		File uploadedFile = null;
		String docId = null;
		boolean major = true;
		ObjectId newId = null;

		// process the request
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(50 * 1024 * 1024);

			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);

			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();

				if (item.isFormField()) {
					String name = item.getFieldName();

					if (PARAM_DOC_ID.equalsIgnoreCase(name)) {
						docId = item.getString();
					} else if (PARAM_MAJOR.equalsIgnoreCase(name)) {
						major = Boolean.parseBoolean(item.getString());
					}
				} else {
					properties.put(PropertyIds.NAME, item.getName());

					uploadedFile = File.createTempFile("blend", "tmp");
					item.write(uploadedFile);
				}
			}
		} catch (Exception e) {
			throw new TheBlendException("Upload failed: " + e, e);
		}

		if (uploadedFile == null) {
			throw new TheBlendException("No content!", null);
		}

		try {
			// find the document
			Document doc = CMISHelper.getDocumet(session, docId,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "document");

			// check out document and get Private Working Copy
			Document pwc = null;
			try {
				// check out
				ObjectId pwcId = doc.checkOut();

				// the PWC must be a document object
				pwc = (Document) session.getObject(pwcId,
						CMISHelper.LIGHT_OPERATION_CONTEXT);
			} catch (CmisBaseException cbe) {
				throw new TheBlendException("Checkout failed!", cbe);
			}

			// prepare the content stream
			ContentStream contentStream = null;
			try {
				contentStream = prepareContentStream(session, uploadedFile, doc
						.getType().getId(), properties);
			} catch (Exception e) {
				throw new TheBlendException("Upload failed: " + e, e);
			}

			// create new version
			try {
				newId = pwc.checkIn(major, properties, contentStream, null);
			} catch (CmisBaseException cbe) {
				throw new TheBlendException("Could not create new version: "
						+ cbe.getMessage(), cbe);
			} finally {
				try {
					contentStream.getStream().close();
				} catch (IOException ioe) {
					// ignore
				}
			}
		} finally {
			// delete temp file
			uploadedFile.delete();
		}

		// show the newly created document
		redirect(HTMLHelper.encodeUrlWithId(request, "show", newId.getId()),
				request, response);
	}
}
