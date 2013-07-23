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

import com.manning.cmis.theblend.session.IdMapping;
import com.manning.cmis.theblend.util.Mapping;

public class TikaMappingService {

	public static final String DEFAULT_DOCUMENT_TYPE = "cmis:document";
	public static final String DEFAULT_FOLDER_TYPE = "cmis:folder";

	private static final String MIMETYPES_PROPERTIES_FILE = "mimetypes.properties";
	private static final String TIKAMAPPING_PROPERTIES_FILE = "tikamapping.properties";

	private static Mapping MIME_TYPES_MAPPING;
	private static Mapping TIKA_MAPPING;

	static {
		MIME_TYPES_MAPPING = new Mapping(MIMETYPES_PROPERTIES_FILE);
		TIKA_MAPPING = new Mapping(TIKAMAPPING_PROPERTIES_FILE);
	}

	/**
	 * Returns the type id that is associated with the given MIME type.
	 */
	public static String getRepositoryTypeIdFromMIMEType(String mimetype) {
		String typeId = MIME_TYPES_MAPPING.getRight(mimetype);
		return (typeId == null ? DEFAULT_DOCUMENT_TYPE : IdMapping
				.getRepositoryTypeId(typeId));
	}

	/**
	 * Returns the property if that matches the Tika metadata name.
	 */
	public static String getPropertyIdFromTikaMetadata(
			String metadataName) {
		String propertyId = TIKA_MAPPING.getRight(metadataName);
		return IdMapping.getRepositoryPropertyId(propertyId);
	}
}
