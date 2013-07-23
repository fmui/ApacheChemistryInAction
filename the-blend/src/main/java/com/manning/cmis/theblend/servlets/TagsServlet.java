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

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;

import com.manning.cmis.theblend.session.IdMapping;

public class TagsServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_TAG = "tag";
	private static final String PARAM_SKIP = "skip";

	public static final String ATTR_TAG = "tag";
	public static final String ATTR_CQL = "cql";
	public static final String ATTR_RESULTS = "results";

	private static final int QUERY_PAGE_SIZE = 20;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException {

		String tag = getStringParameter(request, PARAM_TAG);
		int skip = getIntParameter(request, PARAM_SKIP, 0);

		request.setAttribute(ATTR_TAG, tag);

		if (tag != null) {
			tag = tag.trim();

			QueryStatement stmt = session
					.createQueryStatement("SELECT cmis:objectId, cmis:objectTypeId, "
							+ "cmis:name, cmis:lastModificationDate, "
							+ IdMapping
									.getRepositoryPropertyQueryName("cmisbook:tags")
							+ " AS tags "
							+ "FROM "
							+ IdMapping
									.getRepositoryTypeQueryName("cmisbook:taggable")
							+ " "
							+ "WHERE IN_TREE(?) "
							+ "AND ? = ANY "
							+ IdMapping
									.getRepositoryPropertyQueryName("cmisbook:tags")
							+ " " + "ORDER BY cmis:lastModificationDate DESC");
			stmt.setString(1, getApplicationRootFolderId(request));
			stmt.setString(2, tag);

			request.setAttribute(ATTR_CQL, stmt.toQueryString());

			ItemIterable<QueryResult> results = stmt.query(false);

			// get only a page
			List<Map<String, Object>> resultsList = new ArrayList<Map<String, Object>>();
			ItemIterable<QueryResult> resultsPage = results.skipTo(
					skip * QUERY_PAGE_SIZE).getPage(QUERY_PAGE_SIZE);

			for (QueryResult qr : resultsPage) {
				Map<String, Object> row = new HashMap<String, Object>();

				row.put("cmis:name",
						qr.getPropertyValueByQueryName("cmis:name"));
				row.put("cmis:objectId",
						qr.getPropertyValueByQueryName("cmis:objectId"));

				ObjectType type = session.getTypeDefinition((String) qr
						.getPropertyValueByQueryName("cmis:objectTypeId"));
				row.put("type", type.getDisplayName());

				row.put("cmis:lastModificationDate", qr
						.getPropertyValueByQueryName("cmis:lastModificationDate"));

				row.put("cmisbook:tags",
						qr.getPropertyMultivalueByQueryName("tags")); // using the
																													// alias

				resultsList.add(row);
			}

			request.setAttribute(ATTR_RESULTS, resultsList);
		}

		// show tags page
		dispatch("tags.jsp", "Tags. The Blend.", request, response);
	}

}
