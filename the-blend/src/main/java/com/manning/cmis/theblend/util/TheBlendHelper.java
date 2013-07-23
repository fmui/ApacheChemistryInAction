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
package com.manning.cmis.theblend.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.session.IdMapping;

/**
 * The Blend specific helper methods.
 */
public class TheBlendHelper {

	/**
	 * Returns the id of the artwork document of a given document.
	 * 
	 * A non-null value is returned only if
	 * <ul>
	 * <li>the document has an cmisbook:artwork property</li>
	 * <li>this property is set</li>
	 * <li>the property value points to an existing document</li>
	 * <li>this document has content and its MIME type starts with
	 * "image/"</li>
	 * </ul>
	 * 
	 * @param session
	 *          OpenCMIS session
	 * @param cmisObject
	 *          the object
	 * @return the id of artwork document or <code>null</code> if no
	 *         artwork is set or it is invalid
	 */
	public static String getArtworkId(Session session,
			CmisObject cmisObject) {
		ObjectType type = cmisObject.getType();

		if (!type.getPropertyDefinitions().containsKey(
				IdMapping.getRepositoryPropertyId("cmisbook:artwork"))) {
			return null;
		}

		String artworkId = cmisObject.getPropertyValue(IdMapping
				.getRepositoryPropertyId("cmisbook:artwork"));
		if (artworkId == null) {
			return null;
		}

		CmisObject artworkObject = null;
		try {
			artworkObject = session.getObject(artworkId,
					CMISHelper.FULL_OPERATION_CONTEXT);
		} catch (CmisBaseException cbe) {
			// we couldn't get the artwork object for some reason
			return null;
		}

		if (!(artworkObject instanceof Document)) {
			return null;
		}

		String artworkMimeType = ((Document) artworkObject)
				.getContentStreamMimeType();

		if (artworkMimeType == null) {
			return null;
		}

		if (!artworkMimeType.toLowerCase().startsWith("image/")) {
			return null;
		}

		return artworkId;
	}

	/**
	 * Returns tracks of an album.
	 * 
	 * @param session
	 *          OpenCMIS session
	 * @param albumObject
	 *          the album object
	 * @return a list of track documents, or an empty list if the object
	 *         is not an album or the album has no tracks
	 */
	public static List<Document> getTracks(Session session,
			CmisObject albumObject) {
		List<Document> tracks = new ArrayList<Document>();

		@SuppressWarnings("unchecked")
		List<String> trackIds = (List<String>) albumObject
				.getPropertyValue(IdMapping
						.getRepositoryPropertyId("cmisbook:tracks"));

		if (trackIds != null) {
			for (String trackId : trackIds) {
				try {
					CmisObject track = session.getObject(trackId,
							CMISHelper.FULL_OPERATION_CONTEXT);
					if (track instanceof Document) {
						tracks.add((Document) track);
					}
				} catch (CmisBaseException cbe) {
					// ignore
				}
			}
		}

		return tracks;
	}
}
