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

import com.manning.cmis.theblend.util.CMISHelper;
import com.manning.cmis.theblend.util.HTMLHelper;

public class MoveServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ID = "id";
	private static final String PARAM_SOURCE_ID = "sourceid";
	private static final String PARAM_TARGET_PATH = "targetpath";

	public static final String ATTR_OBJECT = "object";
	public static final String ATTR_PARENTS = "parents";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);

		// fetch the object
		CmisObject cmisObject = CMISHelper.getCmisObject(session, id,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "object");

		request.setAttribute(ATTR_OBJECT, cmisObject);

		// check if object is fileable
		if (!(cmisObject instanceof FileableCmisObject)) {
			throw new TheBlendException("Object is not fileable!");
		}

		FileableCmisObject fileableCmisObject = (FileableCmisObject) cmisObject;
		List<Folder> parents = fileableCmisObject.getParents();

		// check if the object is filed
		if (parents.size() < 1) {
			throw new TheBlendException("Object is not filed!");
		}

		request.setAttribute(ATTR_PARENTS, parents);

		// show move page
		dispatch("move.jsp", "Move " + cmisObject.getName() + ". The Blend.",
				request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response, Session session)
			throws ServletException, IOException, TheBlendException {

		String id = getRequiredStringParameter(request, PARAM_ID);
		String sourceId = getRequiredStringParameter(request, PARAM_SOURCE_ID);
		String path = getRequiredStringParameter(request, PARAM_TARGET_PATH);

		// fetch the object
		CmisObject cmisObject = CMISHelper.getCmisObject(session, id,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "object");

		// check if object is fileable
		if (!(cmisObject instanceof FileableCmisObject)) {
			throw new TheBlendException("Object is not fileable!");
		}

		FileableCmisObject fileableCmisObject = (FileableCmisObject) cmisObject;

		// fetch the target folder
		Folder targetFolder = CMISHelper.getFolderByPath(session, path,
				CMISHelper.LIGHT_OPERATION_CONTEXT, "target folder");

		// move it!
		fileableCmisObject.move(session.createObjectId(sourceId), targetFolder);

		// show browse page
		redirect(
				HTMLHelper.encodeUrlWithId(request, "browse",
						targetFolder.getId()), request, response);
	}
}
