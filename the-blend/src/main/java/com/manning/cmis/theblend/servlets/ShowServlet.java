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
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.manning.cmis.theblend.session.IdMapping;
import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;
import com.manning.cmis.theblend.util.TheBlendHelper;

public class ShowServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_ADD_TAG = "addtag";
	private static final String PARAM_REMOVE_TAG = "removetag";

	public static final String ATTR_DOCUMENT = "document";
	public static final String ATTR_VERSIONS = "versions";
	public static final String ATTR_ARTWORK = "artwork";
	public static final String ATTR_TRACKS = "tracks";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the document object
		Document doc = CMISHelper.getDocumet(session, id,
				CMISHelper.FULL_OPERATION_CONTEXT, "document");

		// refresh
		try {
			// refresh only if the document hasn't been fetched or
			// refreshed within the last minute
			doc.refreshIfOld(60 * 1000);
		} catch (CmisObjectNotFoundException onfe) {
			throw new TheBlendException("Document doesn't exist anymore!",
					onfe);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not retrieve the document!",
					cbe);
		}

		request.setAttribute(ATTR_DOCUMENT, doc);

		// check if the object is versionable
		request.setAttribute(ATTR_VERSIONS, null);
		DocumentType doctype = (DocumentType) doc.getType();
		if (doctype.isVersionable() == null) {
			// the repository did not indicate if this document type
			// supports
			// versioning -> not spec compliant
			// we assume it is not versionable
		} else if (doctype.isVersionable().booleanValue()) {
			List<Document> versions = doc
					.getAllVersions(CMISHelper.VERSION_OPERATION_CONTEXT);
			request.setAttribute(ATTR_VERSIONS, versions);
		}

		// get the if of the artwork document, if it exists
		String artworkId = TheBlendHelper.getArtworkId(session, doc);
		request.setAttribute(ATTR_ARTWORK, artworkId);

		// if this is an album, get the tracks
		if (doc
				.getType()
				.getPropertyDefinitions()
				.containsKey(
						IdMapping.getRepositoryPropertyId("cmisbook:tracks"))) {
			List<Document> tracks = TheBlendHelper.getTracks(session, doc);
			request.setAttribute(ATTR_TRACKS, tracks);
		}

		// show browse page
		dispatch("show.jsp", doc.getName() + " .The Blend.", request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		String addTag = getStringParameter(request, PARAM_ADD_TAG);
		if (addTag != null) {
			addTag = addTag.trim();
			if (addTag.length() == 0) {
				addTag = null;
			}
		}

		String removeTag = getStringParameter(request, PARAM_REMOVE_TAG);
		if (removeTag != null) {
			removeTag = removeTag.trim();
			if (removeTag.length() == 0) {
				removeTag = null;
			}
		}

		ObjectId newId = session.createObjectId(id);

		if (addTag != null || removeTag != null) {

			// fetch the document object
			Document doc = CMISHelper.getDocumet(session, id,
					CMISHelper.FULL_OPERATION_CONTEXT, "document");

			// check if the type of the object defines the property
			// "cmisbook:tags"
			// if not, ignore the request
			if (doc
					.getType()
					.getPropertyDefinitions()
					.containsKey(
							IdMapping.getRepositoryPropertyId("cmisbook:tags"))) {

				List<String> oldTags = doc.getPropertyValue(IdMapping
						.getRepositoryPropertyId("cmisbook:tags"));

				List<String> newTags = new ArrayList<String>();

				if (oldTags != null) {
					newTags.addAll(oldTags);
				}

				if (removeTag != null) {
					newTags.remove(removeTag);
				}

				if (addTag != null) {
					newTags.add(addTag);
				}

				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(
						IdMapping.getRepositoryPropertyId("cmisbook:tags"),
						newTags);

				try {
					CmisObject newObject = doc.updateProperties(properties);
					newId = newObject;
				} catch (CmisBaseException cbe) {
					throw new TheBlendException("Could not update tags!", cbe);
				}
			}
		}

		redirect(
				HTMLHelper.encodeUrlWithId(request, "show", newId.getId()),
				request, response);
	}
}
