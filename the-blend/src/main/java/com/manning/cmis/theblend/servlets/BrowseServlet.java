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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class BrowseServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_PATH = "path";
	private static final String PARAM_SKIP = "skip";

	private static final String PARAM_PARENT = "parent";
	private static final String PARAM_NAME = "name";
	private static final String PARAM_TYPE_ID = "typeid";

	public static final String ATTR_FOLDER = "folder";
	public static final String ATTR_PAGE = "page";
	public static final String ATTR_SKIP = "skip";
	public static final String ATTR_TOTAL = "total";
	public static final String ATTR_HAS_MORE = "hasMore";
	public static final String ATTR_PARENT = "parent";
	public static final String ATTR_DOC_TYPES = "docTypes";
	public static final String ATTR_FOLDER_TYPES = "folderTypes";

	private static final int BROWSE_PAGE_SIZE = 10;

	/**
	 * Show folder content.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getStringParameter(request, PARAM_ID);
		String path = getStringParameter(request, PARAM_PATH);
		int skip = getIntParameter(request, PARAM_SKIP, 0);

		request.setAttribute(ATTR_SKIP, skip);

		Folder folder = null;

		if (id != null) {
			folder = CMISHelper.getFolder(session, id,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "folder");
		} else if (path != null) {
			folder = CMISHelper.getFolderByPath(session, path,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "folder");
		} else {
			folder = CMISHelper.getFolder(session,
					getApplicationRootFolderId(request),
					CMISHelper.LIGHT_OPERATION_CONTEXT, "folder");
		}

		request.setAttribute(ATTR_FOLDER, folder);

		// get the folder children
		// note: this creates only ItemIterable objects and does not
		// contact the repository
		ItemIterable<CmisObject> children = folder
				.getChildren(CMISHelper.BROWSE_OPERATION_CONTEXT);

		// get only a page
		ItemIterable<CmisObject> childrenPage = children.skipTo(
				skip * BROWSE_PAGE_SIZE).getPage(BROWSE_PAGE_SIZE);

		// fetch the children from the repository
		List<CmisObject> page = new ArrayList<CmisObject>();
		try {
			for (CmisObject child : childrenPage) {
				page.add(child);
			}
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not retrieve the children of '"
					+ folder.getName() + "'!", cbe);
		}

		request.setAttribute(ATTR_PAGE, page);

		// get the total number of children in this folder
		// note: if the repository doesn't know the total number, this
		// method returns -1
		request.setAttribute(ATTR_TOTAL, childrenPage.getTotalNumItems());

		// check if there is at least one more pages
		request.setAttribute(ATTR_HAS_MORE, childrenPage.getHasMoreItems());

		// get parent folder
		Folder parent = null;
		if (!folder.isRootFolder()) {
			parent = folder.getParents(CMISHelper.BROWSE_OPERATION_CONTEXT)
					.get(0);
		}

		request.setAttribute(ATTR_PARENT, parent);

		// add creatable types
		request.setAttribute(ATTR_DOC_TYPES,
				getCreatableDocumentTypes(request, response));
		request.setAttribute(ATTR_FOLDER_TYPES,
				getCreatableFolderTypes(request, response));

		// show browse page
		dispatch("browse.jsp", folder.getName() + ". The Blend.", request,
				response);
	}

	/**
	 * Creates a folder.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String parentId = getRequiredStringParameter(request, PARAM_PARENT);
		String typeId = getRequiredStringParameter(request, PARAM_TYPE_ID);
		String name = getStringParameter(request, PARAM_NAME);

		if (name == null || name.length() == 0) {
			redirect(HTMLHelper.encodeUrlWithId(request, "browse", parentId),
					request, response);
			return;
		}

		// fetch the parent folder
		Folder parent = CMISHelper.getFolder(session, parentId,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");

		// set name and type of the new folder
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, name);
		properties.put(PropertyIds.OBJECT_TYPE_ID, typeId);

		// create the folder
		try {
			parent.createFolder(properties);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not create folder: "
					+ cbe.getMessage(), cbe);
		}

		redirect(HTMLHelper.encodeUrlWithId(request, "browse", parentId),
				request, response);
	}
}
