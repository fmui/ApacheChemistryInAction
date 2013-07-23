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
package com.manning.cmis.theblend.android.properties;

import org.apache.chemistry.opencmis.client.api.CmisObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.manning.cmis.theblend.android.R;

/**
 * PropertyActivity is responsible to display the list of properties for a
 * specific object.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class PropertyActivity extends Activity {

    /** The cmis object. */
    private CmisObject cmisObject;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracks);

        // Start the asynctask to retrieve list of properties
        new PropertiesTask(this).execute();
    }

    /**
     * Callback method for PropertiesTask. Creates the listAdapter based on data
     * retrieved.
     * 
     * @param adapter
     *            the listview adapter to displays properties.
     * @param object
     *            the cmisObject associated to the id.
     */
    public void listProperties(SimpleAdapter adapter, CmisObject object) {
        cmisObject = object;
        ((ListView) findViewById(android.R.id.list)).setAdapter(adapter);
        setTitle("Properties : " + cmisObject.getName());
    }
}
