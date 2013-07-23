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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.session.IdMapping;
import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;
import com.manning.cmis.theblend.util.TheBlendHelper;

public class ArtworkServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_ARTWORK_WHAT = "what";
	private static final String PARAM_ARTWORK_ID = "artworkid";
	private static final String PARAM_ARTWORK_PATH = "artworkpath";

	public static final String ATTR_OBJECT = "object";
	public static final String ATTR_ARTWORK = "artwork";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the document object
		Document doc = CMISHelper.getDocumet(session, id,
				CMISHelper.FULL_OPERATION_CONTEXT, "document");

		if (!doc
				.getType()
				.getPropertyDefinitions()
				.containsKey(
						IdMapping.getRepositoryPropertyId("cmisbook:artwork"))) {
			throw new TheBlendException(
					"Document has no cmisbook:artwork property!");
		}

		request.setAttribute(ATTR_OBJECT, doc);

		// get the if of the artwork document, if it exists
		String artworkId = TheBlendHelper.getArtworkId(session, doc);
		request.setAttribute(ATTR_ARTWORK, artworkId);

		// show artwork page
		dispatch("artwork.jsp", "Artwork of " + doc.getName()
				+ " .The Blend.", request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);
		String what = getRequiredStringParameter(request,
				PARAM_ARTWORK_WHAT);

		Document artwork = null;
		String artworkId = null;

		if (what.equalsIgnoreCase("id")) {
			artworkId = getStringParameter(request, PARAM_ARTWORK_ID);
			artwork = CMISHelper.getDocumet(session, artworkId,
					CMISHelper.FULL_OPERATION_CONTEXT, "document");
		} else if (what.equalsIgnoreCase("path")) {
			String artworkPath = getStringParameter(request,
					PARAM_ARTWORK_PATH);
			artwork = CMISHelper.getDocumetByPath(session, artworkPath,
					CMISHelper.FULL_OPERATION_CONTEXT, "document");
			artworkId = artwork.getId();
		} else if (what.equalsIgnoreCase("remove")) {
			artworkId = null;
		} else {
			throw new TheBlendException("What?", null);
		}

		// check artwork
		if (artwork != null) {
			String artworkMimeType = artwork.getContentStreamMimeType();

			if (artworkMimeType == null) {
				throw new TheBlendException("Artwork has no content!");
			}

			if (!artworkMimeType.toLowerCase().startsWith("image/")) {
				throw new TheBlendException("Artwork in not an image!");
			}
		}

		ObjectId newId = session.createObjectId(id);

		// fetch the document object
		Document doc = CMISHelper.getDocumet(session, id,
				CMISHelper.FULL_OPERATION_CONTEXT, "document");

		// update the artwork property
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(
				IdMapping.getRepositoryPropertyId("cmisbook:artwork"),
				artworkId);

		try {
			CmisObject newObject = doc.updateProperties(properties);
			newId = newObject;
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not update artwork id!", cbe);
		}

		redirect(
				HTMLHelper.encodeUrlWithId(request, "show", newId.getId()),
				request, response);
	}
}
