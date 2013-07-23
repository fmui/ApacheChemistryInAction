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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.manning.cmis.theblend.servlets.TheBlendException;
import com.manning.cmis.theblend.session.IdMapping;

/**
 * CMIS related helper methods and constants.
 */
public class CMISHelper {

	/**
	 * Operation Context that retrieves all information about an object.
	 */
	public static final OperationContext FULL_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		FULL_OPERATION_CONTEXT.setFilterString("*");
		FULL_OPERATION_CONTEXT.setIncludeAcls(true);
		FULL_OPERATION_CONTEXT.setIncludeAllowableActions(true);
		FULL_OPERATION_CONTEXT.setIncludePolicies(true);
		FULL_OPERATION_CONTEXT
				.setIncludeRelationships(IncludeRelationships.BOTH);
		FULL_OPERATION_CONTEXT.setRenditionFilterString("*");
		FULL_OPERATION_CONTEXT.setIncludePathSegments(true);
		FULL_OPERATION_CONTEXT.setOrderBy(null);
		FULL_OPERATION_CONTEXT.setCacheEnabled(true);
	}

	/**
	 * Operation Context that retrieves a minimum of information about
	 * an object.
	 */
	public static final OperationContext LIGHT_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		LIGHT_OPERATION_CONTEXT
				.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name");
		LIGHT_OPERATION_CONTEXT.setIncludeAcls(false);
		LIGHT_OPERATION_CONTEXT.setIncludeAllowableActions(false);
		LIGHT_OPERATION_CONTEXT.setIncludePolicies(false);
		LIGHT_OPERATION_CONTEXT
				.setIncludeRelationships(IncludeRelationships.NONE);
		LIGHT_OPERATION_CONTEXT.setRenditionFilterString("cmis:none");
		LIGHT_OPERATION_CONTEXT.setIncludePathSegments(true);
		LIGHT_OPERATION_CONTEXT.setOrderBy(null);
		LIGHT_OPERATION_CONTEXT.setCacheEnabled(true);
	}

	/**
	 * Operation Context that retrieves only the information that
	 * required for the browse page.
	 */
	public static final OperationContext BROWSE_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		BROWSE_OPERATION_CONTEXT
				.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name,"
						+ "cmis:contentStreamLength,cmis:contentStreamMimeType,"
						+ IdMapping.getRepositoryPropertyId("cmisbook:tags"));
		BROWSE_OPERATION_CONTEXT.setIncludeAcls(false);
		BROWSE_OPERATION_CONTEXT.setIncludeAllowableActions(true);
		BROWSE_OPERATION_CONTEXT.setIncludePolicies(false);
		BROWSE_OPERATION_CONTEXT
				.setIncludeRelationships(IncludeRelationships.NONE);
		BROWSE_OPERATION_CONTEXT.setRenditionFilterString("cmis:none");
		BROWSE_OPERATION_CONTEXT.setIncludePathSegments(true);
		BROWSE_OPERATION_CONTEXT.setOrderBy("cmis:name");
		BROWSE_OPERATION_CONTEXT.setCacheEnabled(false);
		BROWSE_OPERATION_CONTEXT.setMaxItemsPerPage(10);
	}

	/**
	 * Operation Context that retrieves versioning information.
	 */
	public static final OperationContext VERSION_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		VERSION_OPERATION_CONTEXT
				.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name,cmis:creationDate,"
						+ "cmis:versionLabel,cmis:versionSeriesCheckedOutId,"
						+ "cmis:isLatestVersion,cmis:isMajorVersion,cmis:isLatestMajorVersion");
		VERSION_OPERATION_CONTEXT.setIncludeAcls(false);
		VERSION_OPERATION_CONTEXT.setIncludeAllowableActions(false);
		VERSION_OPERATION_CONTEXT.setIncludePolicies(false);
		VERSION_OPERATION_CONTEXT
				.setIncludeRelationships(IncludeRelationships.NONE);
		VERSION_OPERATION_CONTEXT.setRenditionFilterString("cmis:none");
		VERSION_OPERATION_CONTEXT.setIncludePathSegments(true);
		VERSION_OPERATION_CONTEXT.setOrderBy(null);
		VERSION_OPERATION_CONTEXT.setCacheEnabled(false);
	}

	/**
	 * Gets a CMIS object from the repository.
	 * 
	 * @param session
	 *          the OpenCMIS session
	 * @param id
	 *          the id of the object
	 * @param context
	 *          the Operation Context
	 * @param what
	 *          string that describes the object, used for error
	 *          messages
	 * @return the CMIS object
	 * @throws TheBlendException
	 */
	public static CmisObject getCmisObject(final Session session,
			final String id, final OperationContext context,
			final String what) throws TheBlendException {
		if (session == null) {
			throw new IllegalArgumentException("Session must be set!");
		}

		if (id == null || id.length() == 0) {
			throw new TheBlendException("Invalid id for " + what + "!");
		}

		OperationContext oc = context;
		if (oc == null) {
			oc = session.getDefaultContext();
		}

		try {
			return session.getObject(id, oc);
		} catch (CmisObjectNotFoundException onfe) {
			throw new TheBlendException("The " + what + " does not exist!",
					onfe);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not retrieve the " + what
					+ ":" + cbe.getMessage(), cbe);
		}
	}

	public static Document getDocumet(final Session session,
			final String id, final OperationContext context,
			final String what) throws TheBlendException {
		// fetch the document object
		CmisObject cmisObject = getCmisObject(session, id, context, what);

		// if the object is not a document, throw an exception
		if (!(cmisObject instanceof Document)) {
			throw new TheBlendException("Object is not a document!");
		}

		return (Document) cmisObject;
	}

	public static Folder getFolder(final Session session,
			final String id, final OperationContext context, String what)
			throws TheBlendException {
		// fetch the folder object
		CmisObject cmisObject = getCmisObject(session, id, context, what);

		// if the object is not a folder, throw an exception
		if (!(cmisObject instanceof Folder)) {
			throw new TheBlendException("Object is not a folder!");
		}

		return (Folder) cmisObject;
	}

	public static CmisObject getCmisObjectByPath(final Session session,
			final String path, final OperationContext context,
			final String what) throws TheBlendException {
		if (session == null) {
			throw new IllegalArgumentException("Session must be set!");
		}

		if (path == null || !path.startsWith("/")) {
			throw new TheBlendException("Invalid path to " + what + "!");
		}

		OperationContext oc = context;
		if (oc == null) {
			oc = session.getDefaultContext();
		}

		try {
			return session.getObjectByPath(path, oc);
		} catch (CmisObjectNotFoundException onfe) {
			throw new TheBlendException("The " + what + " does not exist!",
					onfe);
		} catch (CmisBaseException cbe) {
			throw new TheBlendException("Could not retrieve the " + what
					+ ":" + cbe.getMessage(), cbe);
		}
	}

	public static Document getDocumetByPath(final Session session,
			final String path, final OperationContext context,
			final String what) throws TheBlendException {
		// fetch the document object
		CmisObject cmisObject = getCmisObjectByPath(session, path,
				context, what);

		// if the object is not a document, throw an exception
		if (!(cmisObject instanceof Document)) {
			throw new TheBlendException("Object is not a document!");
		}

		return (Document) cmisObject;
	}

	public static Folder getFolderByPath(final Session session,
			final String path, final OperationContext context, String what)
			throws TheBlendException {
		// fetch the folder object
		CmisObject cmisObject = getCmisObjectByPath(session, path,
				context, what);

		// if the object is not a folder, throw an exception
		if (!(cmisObject instanceof Folder)) {
			throw new TheBlendException("Object is not a folder!");
		}

		return (Folder) cmisObject;
	}

	/**
	 * Returns the creatable descendants types of the given type.
	 */
	public static List<ObjectType> getCreatableTypes(Session session,
			String rootTypeId) {
		List<ObjectType> result = new ArrayList<ObjectType>();

		// get the root type
		ObjectType rootType = null;
		try {
			rootType = session.getTypeDefinition(rootTypeId);
		} catch (CmisObjectNotFoundException e) {
			return result; // empty
		}

		// get the descendants and add the creatable subtypes
		List<Tree<ObjectType>> types = session.getTypeDescendants(
				rootTypeId, -1, false);
		addType(types, result);

		// finally, add the root type, if it is creatable
		boolean isCreatable = (rootType.isCreatable() == null ? true
				: rootType.isCreatable().booleanValue());
		if (isCreatable) {
			result.add(rootType);
		}

		// sort the list by display name
		Collections.sort(result, new Comparator<ObjectType>() {
			public int compare(ObjectType ot1, ObjectType ot2) {
				return ot1.getDisplayName().compareTo(ot2.getDisplayName());
			}
		});

		return result;
	}

	/**
	 * Helper for {@link #getCreatableTypes(Session, String)}.
	 */
	private static void addType(List<Tree<ObjectType>> types,
			List<ObjectType> resultList) {
		for (Tree<ObjectType> tt : types) {
			if (tt.getItem() != null) {
				boolean isCreatable = (tt.getItem().isCreatable() == null ? true
						: tt.getItem().isCreatable().booleanValue());

				if (isCreatable) {
					resultList.add(tt.getItem());
				}

				addType(tt.getChildren(), resultList);
			}
		}
	}
}
