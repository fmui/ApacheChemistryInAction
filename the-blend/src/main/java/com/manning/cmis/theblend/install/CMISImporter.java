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
package com.manning.cmis.theblend.install;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

/**
 * Imports the content of a Zip file into the repository.
 */
public class CMISImporter {

	private static final OperationContext IMPORT_OPERATION_CONTEXT = new OperationContextImpl();

	static {
		IMPORT_OPERATION_CONTEXT
				.setFilterString("cmis:objectId,cmis:objectTypeId,cmis:name");
		IMPORT_OPERATION_CONTEXT.setIncludeAcls(false);
		IMPORT_OPERATION_CONTEXT.setIncludeAllowableActions(false);
		IMPORT_OPERATION_CONTEXT.setIncludePolicies(false);
		IMPORT_OPERATION_CONTEXT
				.setIncludeRelationships(IncludeRelationships.NONE);
		IMPORT_OPERATION_CONTEXT.setRenditionFilter(null);
		IMPORT_OPERATION_CONTEXT.setIncludePathSegments(true);
		IMPORT_OPERATION_CONTEXT.setOrderBy(null);
		IMPORT_OPERATION_CONTEXT.setCacheEnabled(true);
	}

	private Session session;
	private ZipFile zipFile;
	private String applicationRoot;

	/**
	 * CMIS Importer
	 * 
	 * @param session
	 *            Valid OpenCMIS session
	 * @param zipFile
	 *            the Zip file
	 * @param applicationRoot
	 *            the application root folder path
	 */
	public CMISImporter(Session session, ZipFile zipFile, String applicationRoot) {
		if (session == null) {
			throw new IllegalArgumentException("Session not set!");
		}
		if (zipFile == null) {
			throw new IllegalArgumentException("ZipFile not set!");
		}

		this.session = session;
		this.zipFile = zipFile;

		if (applicationRoot == null) {
			this.applicationRoot = "";
		} else if (applicationRoot.endsWith("/")) {
			this.applicationRoot = applicationRoot.substring(0,
					applicationRoot.length() - 1);
		} else {
			this.applicationRoot = applicationRoot;
		}
	}

	/**
	 * Runs the import.
	 */
	public void runImport(ImportProgress progress) {
		progress.startImport();

		Set<String> processed = new HashSet<String>();

		try {
			// create the application root
			createApplicationRoot();

			// iterate over the Zip file
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> entryEnumeration = (Enumeration<ZipEntry>) zipFile
					.entries();

			while (entryEnumeration.hasMoreElements()) {
				ZipEntry entry = entryEnumeration.nextElement();

				try {
					// don't try to re-process folders
					if (processed.contains(entry.getName())) {
						continue;
					}

					processed.add(entry.getName());

					// extract path
					String[] path = entry.getName().split("/");

					// ignore files starting with "."
					if (path[path.length - 1].startsWith(".")) {
						continue;
					}

					// ignore files ending with ".properties"
					if (path[path.length - 1].endsWith(".properties")) {
						continue;
					}

					progress.startFile(entry.getName());

					// don't touch existing objects
					if (exists(path)) {
						progress.message("already existed");
						progress.endFile(entry.getName());
						continue;
					}

					// check the parent and create it, if necessary
					ObjectId parent = checkAndCreateParentFolder(path);

					// create object
					if (entry.isDirectory()) {
						createFolder(parent, path[path.length - 1]);
						progress.message("folder created");
					} else {
						createDocument(parent, path[path.length - 1], entry);
						progress.message("document created");
					}
				} catch (Exception e) {
					progress.message("Exception: " + e.toString());
				}

				progress.endFile(entry.getName());
			}
		} finally {
			try {
				zipFile.close();
			} catch (Exception ie) {
				// ignore
			}

			progress.endImport();
		}
	}

	/**
	 * Creates the application root folder.
	 */
	private void createApplicationRoot() {
		String[] path = applicationRoot.split("/");

		if (path.length < 2) {
			return;
		}

		ObjectId parent = session.getRootFolder();

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < path.length; i++) {
			sb.append('/');
			sb.append(path[i]);

			try {
				parent = session.getObjectByPath(sb.toString(),
						IMPORT_OPERATION_CONTEXT);
			} catch (CmisObjectNotFoundException notFound) {
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(PropertyIds.NAME, path[i]);
				properties.put(PropertyIds.OBJECT_TYPE_ID,
						BaseTypeId.CMIS_FOLDER.value());

				parent = session.createFolder(properties, parent);
			}
		}
	}

	/**
	 * Checks the parent folder of the object that should be created. If the
	 * folder doesn't exits, it creates the folder.
	 */
	private ObjectId checkAndCreateParentFolder(String[] path) {
		// check for the application root folder
		if (path.length < 2) {
			return session.getObjectByPath((applicationRoot.length() == 0 ? "/"
					: applicationRoot), IMPORT_OPERATION_CONTEXT);
		}

		// prepare parent path
		String[] parentPath = new String[path.length - 1];
		System.arraycopy(path, 0, parentPath, 0, parentPath.length);

		try {
			// if parent folder exits, return its object id
			return session.getObjectByPath(buildRepositoryPath(parentPath),
					IMPORT_OPERATION_CONTEXT);
		} catch (CmisObjectNotFoundException notFound) {
			// parent folder doesn't exit -> create it
			ObjectId parent = checkAndCreateParentFolder(parentPath);
			return createFolder(parent, parentPath[parentPath.length - 1]);
		}
	}

	/**
	 * Creates a folder.
	 */
	private ObjectId createFolder(ObjectId parent, String name) {
		// set up properties
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, name);
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				BaseTypeId.CMIS_FOLDER.value());

		return session.createFolder(properties, parent);
	}

	/**
	 * Creates a document.
	 */
	private ObjectId createDocument(ObjectId parent, String name, ZipEntry entry)
			throws Exception {

		// set up properties
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, name);
		properties.put(PropertyIds.OBJECT_TYPE_ID,
				BaseTypeId.CMIS_DOCUMENT.value());

		// copy stream to temp file
		File contentFile = File.createTempFile("theblend", "tmp");
		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				contentFile));

		InputStream stream = zipFile.getInputStream(entry);

		try {
			byte[] buffer = new byte[64 * 1024];
			int b;
			while ((b = stream.read(buffer)) > -1) {
				out.write(buffer, 0, b);
			}
		} catch (Exception e) {
			try {
				contentFile.delete();
			} catch (Exception ie) {
				// ignore
			}
			throw e;
		} finally {
			try {
				out.close();
			} catch (Exception ie) {
				// ignore
			}
		}

		TikaProperties tikaProperties = new TikaProperties(contentFile);
		properties.put(PropertyIds.OBJECT_TYPE_ID, tikaProperties
				.findDocumentType(session).getId());
		tikaProperties.enrichProperties(session, properties);

		String mimetype = tikaProperties.getMIMEType();

		addAdditionalProperties(entry, properties);

		// create document
		InputStream tmpStream = new BufferedInputStream(new FileInputStream(
				contentFile));
		try {
			String filename = (String) properties.get(PropertyIds.NAME);
			long filesize = contentFile.length();

			ContentStream contentStream = session.getObjectFactory()
					.createContentStream(filename, filesize, mimetype,
							tmpStream);

			return session.createDocument(properties, parent, contentStream,
					null);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				tmpStream.close();
			} catch (Exception ie) {
				// ignore
			}
			try {
				contentFile.delete();
			} catch (Exception ie) {
				// ignore
			}
		}
	}

	/**
	 * Adds additional properties.
	 */
	protected void addAdditionalProperties(ZipEntry parentEntry,
			Map<String, Object> properties) {
		ZipEntry entry = zipFile
				.getEntry(parentEntry.getName() + ".properties");
		if (entry == null) {
			return;
		}

		try {
			java.util.Properties props = new java.util.Properties();
			props.load(zipFile.getInputStream(entry));

			String typeId = props.getProperty(PropertyIds.OBJECT_TYPE_ID);
			if (typeId == null) {
				typeId = (String) properties.get(PropertyIds.OBJECT_TYPE_ID);
			}

			ObjectType typeDef = session.getTypeDefinition(typeId);

			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd'T'hh:mm:ssZ");

			for (Object key : props.keySet()) {
				String propId = key.toString();

				PropertyDefinition<?> propDef = typeDef
						.getPropertyDefinitions().get(propId);
				if (propDef == null
						|| propDef.getUpdatability() == Updatability.READONLY) {
					continue;
				}

				String[] values = props.getProperty(propId).split("\n");

				switch (propDef.getPropertyType()) {
				case INTEGER:
					List<BigInteger> propValueInt = new ArrayList<BigInteger>();
					for (String value : values) {
						propValueInt.add(new BigInteger(value));
					}
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, propValueInt.get(0));
					} else {
						properties.put(propId, propValueInt);
					}
					break;
				case DECIMAL:
					List<BigDecimal> propValueDec = new ArrayList<BigDecimal>();
					for (String value : values) {
						propValueDec.add(new BigDecimal(value));
					}
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, propValueDec.get(0));
					} else {
						properties.put(propId, propValueDec);
					}
					break;
				case BOOLEAN:
					List<Boolean> propValueBool = new ArrayList<Boolean>();
					for (String value : values) {
						propValueBool.add(Boolean.valueOf(value));
					}
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, propValueBool.get(0));
					} else {
						properties.put(propId, propValueBool);
					}
					break;
				case DATETIME:
					List<GregorianCalendar> propValueDateTime = new ArrayList<GregorianCalendar>();
					for (String value : values) {
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTime(sdf.parse(value));
						propValueDateTime.add(cal);
					}
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, propValueDateTime.get(0));
					} else {
						properties.put(propId, propValueDateTime);
					}
					break;
				case STRING:
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, values[0]);
					} else {
						properties.put(propId, Arrays.asList(values));
					}
					break;
				case ID:
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, values[0]);
					} else {
						properties.put(propId, Arrays.asList(values));
					}
					break;
				case HTML:
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, values[0]);
					} else {
						properties.put(propId, Arrays.asList(values));
					}
					break;
				case URI:
					if (propDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propId, values[0]);
					} else {
						properties.put(propId, Arrays.asList(values));
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the object exits.
	 */
	private boolean exists(String[] path) {
		try {
			session.getObjectByPath(buildRepositoryPath(path),
					IMPORT_OPERATION_CONTEXT);
		} catch (CmisObjectNotFoundException notFound) {
			return false;
		}

		return true;
	}

	/**
	 * Return the repository path from the given path fragments.
	 */
	private String buildRepositoryPath(String[] path) {
		StringBuilder parentPath = new StringBuilder(applicationRoot);

		for (int i = 0; i < path.length; i++) {
			parentPath.append('/');
			parentPath.append(path[i]);
		}

		return parentPath.toString();
	}

	public static interface ImportProgress {

		void startImport();

		void endImport();

		void startFile(String name);

		void endFile(String name);

		void message(String msg);
	}
}
