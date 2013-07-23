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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TikaProperties {

	private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private Metadata metadata;
	private ObjectType docType;

	public TikaProperties(File file) throws IOException, SAXException,
			TikaException {
		TikaInputStream tikaStream = TikaInputStream.get(file);
		metadata = new Metadata();
		ContentHandler handler = new DefaultHandler();
		Parser parser = new AutoDetectParser();
		ParseContext context = new ParseContext();
		try {
			parser.parse(tikaStream, handler, metadata, context);
		} finally {
			try {
				tikaStream.close();
			} catch (Exception ie) {
				// ignore
			}
		}
	}

	/**
	 * Sets the document type.
	 */
	public void setDocumentType(ObjectType docType) {
		this.docType = docType;
	}

	/**
	 * Identifies the document type from content.
	 */
	public ObjectType findDocumentType(Session session) {
		// get the type id for the MIME type
		String newTypeId = TikaMappingService
				.getRepositoryTypeIdFromMIMEType(getMIMEType());

		// check if type exists in the repository
		try {
			docType = session.getTypeDefinition(newTypeId);
		} catch (CmisObjectNotFoundException e) {
			// type not found -> fall back to cmis:document
			docType = session.getTypeDefinition(BaseTypeId.CMIS_DOCUMENT
					.value());
		}

		return docType;
	}

	/**
	 * Returns the MIME type.
	 */
	public String getMIMEType() {
		String mimetype = metadata.get(Metadata.CONTENT_TYPE);
		if (mimetype == null) {
			mimetype = "application/octet-stream";
		}

		return mimetype;
	}

	/**
	 * Adds the extracted metadata to the properties.
	 */
	public void enrichProperties(Session session,
			Map<String, Object> properties) {
		if (docType == null) {
			findDocumentType(session);
		}

		// set document type
		properties.put(PropertyIds.OBJECT_TYPE_ID, docType.getId());

		// iterate over the metadata that Tika extracted and add it to
		// the document, if the document type supports them
		for (String metadataName : metadata.names()) {
			String propertyId = TikaMappingService
					.getPropertyIdFromTikaMetadata(metadataName);
			if (propertyId == null) {
				// there is no mapping for this property
				continue;
			}

			PropertyDefinition<?> propertyDef = docType
					.getPropertyDefinitions().get(propertyId);
			if (propertyDef == null) {
				// the document type doen't support this property
				continue;
			}

			try {
				switch (propertyDef.getPropertyType()) {
				case STRING:
				case ID:
				case HTML:
				case URI:
					if (propertyDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propertyId, metadata.get(metadataName));
					} else {
						properties.put(propertyId,
								Arrays.asList(metadata.getValues(metadataName)));
					}
					break;
				case INTEGER:
					if (propertyDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propertyId,
								convertInteger(metadata.get(metadataName)));
					} else {
						List<BigInteger> list = new ArrayList<BigInteger>();
						for (String v : metadata.getValues(metadataName)) {
							list.add(convertInteger(v));
						}
						properties.put(propertyId, list);
					}
					break;
				case DECIMAL:
					if (propertyDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propertyId,
								convertDecimal(metadata.get(metadataName)));
					} else {
						List<BigDecimal> list = new ArrayList<BigDecimal>();
						for (String v : metadata.getValues(metadataName)) {
							list.add(convertDecimal(v));
						}
						properties.put(propertyId, list);
					}
					break;
				case BOOLEAN:
					if (propertyDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propertyId,
								convertBoolean(metadata.get(metadataName)));
					} else {
						List<Boolean> list = new ArrayList<Boolean>();
						for (String v : metadata.getValues(metadataName)) {
							list.add(convertBoolean(v));
						}
						properties.put(propertyId, list);
					}
					break;
				case DATETIME:
					if (propertyDef.getCardinality() == Cardinality.SINGLE) {
						properties.put(propertyId,
								convertDate(metadata.get(metadataName)));
					} else {
						List<Date> list = new ArrayList<Date>();
						for (String v : metadata.getValues(metadataName)) {
							list.add(convertDate(v));
						}
						properties.put(propertyId, list);
					}
					break;
				}
			} catch (Exception e) {
				// Tika provided a value that doesn't match the property
				// definition -> ignore
			}
		}
	}

	private BigInteger convertInteger(String s) {
		int dot = s.indexOf('.');
		if (dot > -1) {
			s = s.substring(0, dot);
		}

		return new BigInteger(s);
	}

	private BigDecimal convertDecimal(String s) {
		return new BigDecimal(s);
	}

	private Boolean convertBoolean(String s) {
		return Boolean.valueOf(s);
	}

	private Date convertDate(String s) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT,
				new DateFormatSymbols(Locale.US));
		return sdf.parse(s);
	}
}
