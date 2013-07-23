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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import com.manning.cmis.theblend.install.TikaProperties;

public abstract class AbstractAddServlet extends AbstractTheBlendServlet {
	private static final long serialVersionUID = 1L;

	protected ContentStream prepareContentStream(Session session,
			File contentFile, String typeId, Map<String, Object> properties)
			throws Exception {

		ObjectType docType = session.getTypeDefinition(typeId);

		TikaProperties tikaProperties = new TikaProperties(contentFile);
		tikaProperties.setDocumentType(docType);
		tikaProperties.enrichProperties(session, properties);

		String name = (String) properties.get(PropertyIds.NAME);
		long size = contentFile.length();
		String mimetype = tikaProperties.getMIMEType();
		InputStream stream = new BufferedInputStream(new FileInputStream(
				contentFile));

		return session.getObjectFactory().createContentStream(name, size,
				mimetype, stream);
	}

}
