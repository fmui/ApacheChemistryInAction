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
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

public class TestServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		// --- get parameters ---

		String id = getStringParameter(request, "id");
		if (id == null) {
			id = getApplicationRootFolderId(request);
		}

		int skip = getIntParameter(request, "skip", 0);

		// --- fetch folder object ---

		OperationContext foc = session.createOperationContext();
		foc.setFilterString("cmis:name,cmis:path");
		foc.setIncludeAcls(false);
		foc.setIncludeAllowableActions(false);
		foc.setIncludePolicies(false);
		foc.setIncludeRelationships(IncludeRelationships.NONE);
		foc.setRenditionFilterString("cmis:none");
		foc.setIncludePathSegments(true);
		foc.setOrderBy(null);
		foc.setCacheEnabled(true);

		CmisObject object = null;
		try {
			object = session.getObject(id, foc);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not retrieve folder!", cbe);
		}

		Folder folder = null;
		if (object instanceof Folder) {
			folder = (Folder) object;
		} else {
			throw new TheBlendException("Object is not a folder!");
		}

		request.setAttribute("folder", folder);

		// --- fetch children ---

		OperationContext coc = session.createOperationContext();
		coc.setFilterString("cmis:objectId,cmis:baseTypeId,"
				+ "cmis:name,cmis:contentStreamLength,"
				+ "cmis:contentStreamMimeType");
		coc.setIncludeAcls(false);
		coc.setIncludeAllowableActions(true);
		coc.setIncludePolicies(false);
		coc.setIncludeRelationships(IncludeRelationships.NONE);
		coc.setRenditionFilterString("cmis:none");
		coc.setIncludePathSegments(true);
		coc.setOrderBy("cmis:name");
		coc.setCacheEnabled(false);
		coc.setMaxItemsPerPage(10);

		ItemIterable<CmisObject> children = folder.getChildren(coc);
		ItemIterable<CmisObject> page = children.skipTo(skip).getPage(10);

		List<CmisObject> childrenPage = new ArrayList<CmisObject>();

		try {
			for (CmisObject child : page) {
				childrenPage.add(child);
			}
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not fetch children!");
		}

		request.setAttribute("page", childrenPage);

		// --- determine paging links ---

		request.setAttribute("isFirstPage", skip == 0);
		request.setAttribute("isLastPage", !page.getHasMoreItems());

		// --- fetch parent ---

		Folder parent = null;
		if (!folder.isRootFolder()) {
			parent = folder.getParents(coc).get(0);
		}

		request.setAttribute("parent", parent);

		// --- show browse page ---

		dispatch("browse.jsp", folder.getName() + ". The Blend.",
				request, response);
	}
}
