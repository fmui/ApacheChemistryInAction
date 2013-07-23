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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import com.manning.cmis.theblend.session.IdMapping;
import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;
import com.manning.cmis.theblend.util.TheBlendHelper;

public class AlbumServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_PARENT_PATH = "parentpath";
	private static final String PARAM_PARENT_ID = "parentid";
	private static final String PARAM_NAME = "name";
	private static final String PARAM_TRACK_WHAT = "what";
	private static final String PARAM_TRACK_ID = "trackid";
	private static final String PARAM_TRACK_PATH = "trackpath";

	public static final String ATTR_OBJECT = "object";
	public static final String ATTR_TRACKS = "tracks";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the album object
		Document album = CMISHelper.getDocumet(session, id,
				CMISHelper.FULL_OPERATION_CONTEXT, "album");

		// check if the object has the cmisbook:tracks property
		if (!album
				.getType()
				.getPropertyDefinitions()
				.containsKey(
						IdMapping.getRepositoryPropertyId("cmisbook:tracks"))) {
			throw new TheBlendException(
					"Document has no cmisbook:tracks property!");
		}

		request.setAttribute(ATTR_OBJECT, album);

		// get the tracks
		List<Document> tracks = TheBlendHelper.getTracks(session, album);
		request.setAttribute(ATTR_TRACKS, tracks);

		// show album page
		dispatch("album.jsp", album.getName() + " .The Blend.", request,
				response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		// find out what to do
		int action = getIntParameter(request, PARAM_ACTION, 0);
		if (action < 1 && action > 3) {
			throw new TheBlendException("Unknown action!");
		}

		ObjectId newId = null;

		if (action == 1) {
			// create new album
			newId = createAlbum(session, request);
		} else {
			// album update, get its id
			String id = getRequiredStringParameter(request, PARAM_ID);

			// fetch the album object
			Document album = CMISHelper.getDocumet(session, id,
					CMISHelper.FULL_OPERATION_CONTEXT, "album");

			// check if the object has the cmisbook:tracks property
			if (!album
					.getType()
					.getPropertyDefinitions()
					.containsKey(
							IdMapping.getRepositoryPropertyId("cmisbook:tracks"))) {
				error("Document has no cmisbook:tracks property!", null,
						request, response);
				return;
			}

			List<String> tracks = null;
			if (action == 2) {
				// update track list
				tracks = getTrackList(request);
			} else if (action == 3) {
				// add track to album
				List<String> orgTracks = album.getPropertyValue(IdMapping
						.getRepositoryPropertyId("cmisbook:tracks"));
				tracks = addTrack(session, request, orgTracks);
			}

			// update the track list
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(
					IdMapping.getRepositoryPropertyId("cmisbook:tracks"),
					tracks);

			try {
				CmisObject newObject = album.updateProperties(properties);
				newId = newObject;
			} catch (CmisBaseException cbe) {
				throw new TheBlendException("Could not update track list!",
						cbe);
			}
		}

		// return to album page
		redirect(
				HTMLHelper.encodeUrlWithId(request, "album", newId.getId()),
				request, response);
	}

	protected ObjectId createAlbum(Session session,
			HttpServletRequest request) throws TheBlendException {
		String parentId = getStringParameter(request, PARAM_PARENT_ID);
		String parentPath = getStringParameter(request, PARAM_PARENT_PATH);
		String name = getStringParameter(request, PARAM_NAME);

		if (name == null || name.length() == 0) {
			name = "The Blend";
		}

		// fetch the parent folder
		Folder parent = null;
		if (parentId != null) {
			parent = CMISHelper.getFolder(session, parentId,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");
		} else if (parentPath != null) {
			parent = CMISHelper.getFolderByPath(session, parentPath,
					CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");
		} else {
			parent = CMISHelper.getFolder(session,
					getApplicationRootFolderId(request),
					CMISHelper.LIGHT_OPERATION_CONTEXT, "parent folder");
		}

		// create a document without content
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, name);
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				IdMapping.getRepositoryTypeId("cmisbook:album"));

		return session.createDocument(properties, parent, null, null);
	}

	protected List<String> getTrackList(HttpServletRequest request) {
		List<TrackPosition> trackList = new ArrayList<AlbumServlet.TrackPosition>();

		// get the track list
		int x = 0;
		String trackId = getStringParameter(request, "id_" + x);
		while (trackId != null) {
			if (getIntParameter(request, "remove_" + x, 0) != 1) {
				int pos = getIntParameter(request, "pos_" + x, 0);
				trackList.add(new TrackPosition(trackId, pos));
			}
			x++;
			trackId = getStringParameter(request, "id_" + x);
		}

		// sort the track list
		Collections.sort(trackList, new Comparator<TrackPosition>() {
			@Override
			public int compare(TrackPosition t1, TrackPosition t2) {
				if (t1.getPosition() < t2.getPosition()) {
					return -1;
				} else if (t1.getPosition() > t2.getPosition()) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		// prepare the track list for update
		List<String> tracks = new ArrayList<String>();
		for (TrackPosition tp : trackList) {
			tracks.add(tp.getTrackId());
		}

		return tracks;
	}

	protected List<String> addTrack(Session session,
			HttpServletRequest request, List<String> orgsTracks)
			throws TheBlendException {

		String what = getRequiredStringParameter(request,
				PARAM_TRACK_WHAT);

		Document track = null;

		if (what.equalsIgnoreCase("id")) {
			String trackId = getStringParameter(request, PARAM_TRACK_ID);
			track = CMISHelper.getDocumet(session, trackId,
					CMISHelper.FULL_OPERATION_CONTEXT, "track");
		} else if (what.equalsIgnoreCase("path")) {
			String trackPath = getStringParameter(request, PARAM_TRACK_PATH);
			track = CMISHelper.getDocumetByPath(session, trackPath,
					CMISHelper.FULL_OPERATION_CONTEXT, "track");
		} else {
			throw new TheBlendException("What?");
		}

		// check track MIME type
		String trackMimeType = track.getContentStreamMimeType();

		if (trackMimeType == null) {
			throw new TheBlendException("Track has no content!");
		}

		if (!trackMimeType.toLowerCase().startsWith("audio/")) {
			throw new TheBlendException("Track in not an audio file!");
		}

		if (orgsTracks.contains(track.getId())) {
			// the track is already in the track list
			return orgsTracks;
		} else {
			// add track to track list
			List<String> tracks = new ArrayList<String>(orgsTracks);
			tracks.add(track.getId());
			return tracks;
		}
	}

	protected static class TrackPosition {
		final private String trackId;
		final private int position;

		public TrackPosition(String trackId, int position) {
			this.trackId = trackId;
			this.position = position;
		}

		public String getTrackId() {
			return trackId;
		}

		public int getPosition() {
			return position;
		}
	}
}
