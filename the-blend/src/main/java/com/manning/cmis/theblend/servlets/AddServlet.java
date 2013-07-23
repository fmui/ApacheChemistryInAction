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

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.manning.cmis.theblend.session.OpenCMISSessionFactory;
import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class AddServlet extends AbstractAddServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_PARENT_PATH = "parentpath";
	private static final String PARAM_PARENT_ID = "parentid";
	private static final String PARAM_TYPE_ID = "typeid";

	public static final String ATTR_PATH = "path";
	public static final String ATTR_DOC_TYPES = "docTypes";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException {

		// add application root path
		request.setAttribute(ATTR_PATH,
				OpenCMISSessionFactory.getApplicationRootFolderPath());

		// add creatable types
		request.setAttribute(ATTR_DOC_TYPES,
				getCreatableDocumentTypes(request, response));

		// show add page
		dispatch("add.jsp", "Add something new. The Blend.", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		// check for multipart content
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (!isMultipart) {
			// we expected content -> return to add page
			dispatch("add.jsp", "Add something new. The Blend.", request,
					response);
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		File uploadedFile = null;
		String parentId = null;
		String parentPath = null;
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

					if (PARAM_PARENT_ID.equalsIgnoreCase(name)) {
						parentId = item.getString();
					} else if (PARAM_PARENT_PATH.equalsIgnoreCase(name)) {
						parentPath = item.getString();
					} else if (PARAM_TYPE_ID.equalsIgnoreCase(name)) {
						properties.put(PropertyIds.OBJECT_TYPE_ID,
								item.getString());
					}
				} else {
					String name = item.getName();
					if (name == null) {
						name = "file";
					} else {
						// if the browser provided a path instead of a file name,
						// strip off the path
						int x = name.lastIndexOf('/');
						if (x > -1) {
							name = name.substring(x + 1);
						}
						x = name.lastIndexOf('\\');
						if (x > -1) {
							name = name.substring(x + 1);
						}

						name = name.trim();
						if (name.length() == 0) {
							name = "file";
						}
					}

					properties.put(PropertyIds.NAME, name);

					uploadedFile = File.createTempFile("blend", "tmp");
					item.write(uploadedFile);
				}
			}
		} catch (Exception e) {
			throw new TheBlendException("Upload failed: " + e, e);
		}

		if (uploadedFile == null) {
			throw new TheBlendException("No content!");
		}

		try {
			// prepare the content stream
			ContentStream contentStream = null;
			try {
				String objectTypeId = (String) properties
						.get(PropertyIds.OBJECT_TYPE_ID);
				contentStream = prepareContentStream(session, uploadedFile,
						objectTypeId, properties);
			} catch (Exception e) {
				throw new TheBlendException("Upload failed: " + e, e);
			}

			// find the parent folder
			// (we don't deal with unfiled documents here)
			Folder parent = null;
			if (parentId != null) {
				parent = CMISHelper.getFolder(session, parentId,
						CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");
			} else {
				parent = CMISHelper.getFolderByPath(session, parentPath,
						CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");
			}

			// create the document
			try {
				newId = session.createDocument(properties, parent,
						contentStream, null);
			} catch (CmisBaseException cbe) {
				throw new TheBlendException("Could not create document: "
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
		redirect(
				HTMLHelper.encodeUrlWithId(request, "show", newId.getId()),
				request, response);
	}
}
