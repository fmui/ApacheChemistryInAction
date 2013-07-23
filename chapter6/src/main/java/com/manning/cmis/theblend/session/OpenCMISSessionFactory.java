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
package com.manning.cmis.theblend.session;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class OpenCMISSessionFactory {

	/**
	 * Creates a new OpenCMIS session with the provided username and password.
	 */
	//<start id="ch06_createOpenCMISSession" />
	public static Session createOpenCMISSession(
			String username, String password) {
		Map<String, String> parameter = new HashMap<String, String>();

		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);

		parameter.put(SessionParameter.ATOMPUB_URL,
				"http://localhost:8081/inmemory/atom"); //<co id="ch06_service_url" />
		parameter.put(SessionParameter.BINDING_TYPE,
				BindingType.ATOMPUB.value()); //<co id="ch06_binding" />
		parameter.put(SessionParameter.REPOSITORY_ID, "A1"); //<co id="ch06_repo_id" />

		SessionFactory factory = SessionFactoryImpl.newInstance();

		return factory.createSession(parameter);
	}
	//<end id="ch06_createOpenCMISSession" />
}
