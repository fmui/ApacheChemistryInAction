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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class DeleteServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_ALL_VERSIONS = "allversions";

	public static final String ATTR_OBJECT = "object";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the object
		CmisObject cmisObject = CMISHelper.getCmisObject(session, id,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "object");

		request.setAttribute(ATTR_OBJECT, cmisObject);

		// show rename page
		dispatch("delete.jsp", "Delete " + cmisObject.getName()
				+ ". The Blend.", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);
		boolean allVersions = getBooleanParameter(request,
				PARAM_ALL_VERSIONS, false);
		String parentId = null;

		// fetch and delete the object
		try {
			CmisObject cmisObject = CMISHelper.getCmisObject(session, id,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "object");

			// get the (first) parent folder, if one exists
			// we want to redirect to the parents browse page later
			if (cmisObject instanceof FileableCmisObject) {
				List<Folder> parents = ((FileableCmisObject) cmisObject)
						.getParents();
				if (parents.size() > 0) {
					parentId = parents.get(0).getId();
				}
			}

			if (cmisObject instanceof Folder) {
				List<String> failedToDelete = ((Folder) cmisObject)
						.deleteTree(true, UnfileObject.DELETE, true);

				if (failedToDelete != null && !failedToDelete.isEmpty()) {
					throw new TheBlendException("Deletion failed!");
				}
			} else {
				cmisObject.delete(allVersions);
			}
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Deletion failed: "
					+ cbe.getMessage(), cbe);
		}

		if (parentId == null) {
			// show dashbord page
			redirect("dashboard", request, response);
		} else {
			// show browse page
			redirect(
					HTMLHelper.encodeUrlWithId(request, "browse", parentId),
					request, response);
		}
	}
}
