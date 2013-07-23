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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.manning.cmis.theblend.android.R;
import com.manning.cmis.theblend.android.albums.AlbumDetailsActivity;
import com.manning.cmis.theblend.android.constant.BundleConstant;
import com.manning.cmis.theblend.android.utils.CmisResult;
import com.manning.cmis.theblend.android.utils.ListUtils;

/**
 * Provides an asynchronous mechanism to retrieves all properties for a specific
 * CMIS Object. <br/>
 * 
 * @author Jean Marie Pascal
 * 
 */
public class PropertiesTask extends AsyncTask<Void, Void, CmisResult<List<Map<String, ?>>>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "PropertiesTask";

    /** The CMIS Session object. */
    private Session session;

    /** The activity to display the result. */
    private Activity activity;

    /** The object id for the cmisObject. */
    private String objectId;

    /** The cmis object. */
    private CmisObject cmisObject;

    /**
     * Instantiates a new properties task.
     * 
     * @param activity
     *            the activity to display the result
     */
    public PropertiesTask(Activity activity) {
        this.activity = activity;
        if (activity.getIntent().getExtras() != null) {
            Bundle b = activity.getIntent().getExtras().getBundle(BundleConstant.KEY_EXTRAS);
            session = (Session) b.getSerializable(BundleConstant.KEY_SESSION);
            if (activity instanceof PropertyActivity) {
                objectId = b.getString(BundleConstant.KEY_CMISOBJECT_ID);
            } else if (activity instanceof AlbumDetailsActivity) {
                objectId = b.getString(BundleConstant.KEY_ALBUM_ID);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<List<Map<String, ?>>> doInBackground(Void... arg0) {
        List<Map<String, ?>> list = null;
        Exception exception = null;

        // Find the cmisobject associated to the id and returns the list of
        // properties.
        try {
            cmisObject = session.getObject(objectId);

            list = new ArrayList<Map<String, ?>>();
            List<Property<?>> props = cmisObject.getProperties();
            for (Property<?> prop : props) {
                list.add(ListUtils.createPair(prop.getDisplayName(), (prop.isMultiValued()) ? prop.getValuesAsString()
                        .replace("[", "").replace("]", "") : prop.getValueAsString()));
            }

        } catch (Exception e) {
            exception = e;
        }

        return new CmisResult<List<Map<String, ?>>>(exception, list);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(CmisResult<List<Map<String, ?>>> results) {
        // Illustrates how it's possible to reuse the same Task across multiple
        // Activities

        // In case of exception, displays informations for debugging purpose.
        if (results.hasException()) {
            Toast.makeText(activity, results.getException().getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        } else if (activity instanceof PropertyActivity) {
            SimpleAdapter adapter = new SimpleAdapter(activity, results.getData(), R.layout.properties_row,
                    new String[] { "name", "value" }, new int[] { R.id.propertyName, R.id.propertyValue });
            ((PropertyActivity) activity).listProperties(adapter, cmisObject);
        } else if (activity instanceof AlbumDetailsActivity) {
            SimpleAdapter adapter = new SimpleAdapter(activity, results.getData(), R.layout.properties_row,
                    new String[] { "name", "value" }, new int[] { R.id.propertyName, R.id.propertyValue });
            ((AlbumDetailsActivity) activity).listProperties(adapter, cmisObject);
        }
    }
}
