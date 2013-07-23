/*******************************************************************************
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
 ******************************************************************************/
package com.manning.cmis.theblend.android.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Property;

/**
 * List utility methods for managing list object.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class ListUtils {

    /**
     * Creates a list of maps. It allows the creation of properties listAdapter.
     */
    public static List<Map<String, ?>> buildListOfNameValueMaps(CmisObject object) {
        List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
        for (Property<?> cmisProperty : object.getProperties()) {
            list.add(createPair(cmisProperty.getDisplayName(), cmisProperty.getValueAsString()));
        }
        return list;
    }

    /**
     * Reponsible to contains values for a specific row (name/value)
     * 
     * @param name
     *            : key
     * @param value
     *            : value
     * @return a Map of String value.
     */
    public static Map<String, ?> createPair(String name, String value) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("name", name);
        hashMap.put("value", value);
        return hashMap;
    }

}
