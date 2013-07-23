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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class RenameServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_NEWNAME = "newname";

	public static final String ATTR_OBJECT = "object";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the object
		CmisObject cmisObject = CMISHelper.getCmisObject(session, id,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "object");

		request.setAttribute(ATTR_OBJECT, cmisObject);

		// show rename page
		dispatch("rename.jsp", "Rename " + cmisObject.getName()
				+ ". The Blend.", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);
		String newname = getRequiredStringParameter(request, PARAM_NEWNAME);
		String parentId = null;

		// fetch the object
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

		// rename the object
		try {
			// update the cmis:name property
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.NAME, newname);

			cmisObject.updateProperties(properties);

		} catch (CmisObjectNotFoundException onfe) {
			throw new TheBlendException("Object doesn't exist!", onfe);
		} catch (CmisNameConstraintViolationException ncve) {
			throw new TheBlendException("The new name is invalid or "
					+ "an object with this name "
					+ "already exists in this folder!", ncve);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Rename failed: " + cbe.getMessage(),
					cbe);
		}

		if (parentId == null) {
			// show dashbord page
			redirect("dashboard", request, response);
		} else {
			// show browse page
			redirect(HTMLHelper.encodeUrlWithId(request, "browse", parentId),
					request, response);
		}
	}
}
