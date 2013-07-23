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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.session.OpenCMISSessionFactory;
import com.manning.cmis.theblend.util.CMISHelper;

/**
 * Dashboard page.
 */
public class DashboardServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	public static final String ATTR_CQL = "cql";
	public static final String ATTR_UPDATED = "recentlyUpdated";
	public static final String ATTR_CHILDREN = "children";
	public static final String ATTR_PATH = "path";
	public static final String ATTR_DOC_TYPES = "docTypes";

	private static final OperationContext RECENTLY_UPDATED_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		RECENTLY_UPDATED_OPERATION_CONTEXT
				.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name,cmis:lastModificationDate");

		RECENTLY_UPDATED_OPERATION_CONTEXT
				.setIncludeAllowableActions(false);
		RECENTLY_UPDATED_OPERATION_CONTEXT.setIncludeAcls(false);
		RECENTLY_UPDATED_OPERATION_CONTEXT
				.setOrderBy("cmis:lastModificationDate DESC");
	}

	private static final int BROWSE_PAGE_SIZE = 10;
	private static final int RECENTLY_UPDATED_PAGE_SIZE = 10;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		// get the first 10 children of the application root folder
		List<CmisObject> children = new ArrayList<CmisObject>();
		try {
			String appRootFolderId = getApplicationRootFolderId(request);
			Folder appRootFolder = (Folder) session.getObject(
					appRootFolderId, CMISHelper.LIGHT_OPERATION_CONTEXT);

			for (CmisObject child : appRootFolder.getChildren(
					CMISHelper.BROWSE_OPERATION_CONTEXT).getPage(
					BROWSE_PAGE_SIZE)) {
				children.add(child);
			}

			request.setAttribute(ATTR_CHILDREN, children);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException(
					"Could not retrieve application root children!", cbe);
		}

		// get the recently updated documents
		List<CmisObject> recentlyUpdated = new ArrayList<CmisObject>();
		try {
			QueryStatement stmt = session
					.createQueryStatement("IN_TREE(?)");
			stmt.setString(1, getApplicationRootFolderId(request));

			// build query for recently updated documents
			ItemIterable<CmisObject> results = session.queryObjects(
					"cmis:document", stmt.toQueryString(), false,
					RECENTLY_UPDATED_OPERATION_CONTEXT);

			request.setAttribute(
					ATTR_CQL,
					"SELECT "
							+ RECENTLY_UPDATED_OPERATION_CONTEXT.getFilterString()
							+ " FROM cmis:documet WHERE " + stmt.toQueryString()
							+ " ORDER BY "
							+ RECENTLY_UPDATED_OPERATION_CONTEXT.getOrderBy());

			// get the first page
			ItemIterable<CmisObject> resultPage = results
					.getPage(RECENTLY_UPDATED_PAGE_SIZE);

			for (CmisObject result : resultPage) {
				recentlyUpdated.add(result);
			}

			request.setAttribute(ATTR_UPDATED, recentlyUpdated);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException(
					"Could not retrieve updated documents!", cbe);
		}

		// add application root path
		request.setAttribute(ATTR_PATH,
				OpenCMISSessionFactory.getApplicationRootFolderPath());

		// add creatable types
		request.setAttribute(ATTR_DOC_TYPES,
				getCreatableDocumentTypes(request, response));

		// show dashbord page
		dispatch("dashboard.jsp", "Your Dashboard. The Blend.", request,
				response);
	}
}
