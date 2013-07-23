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
package com.manning.cmis.theblend.android.albums;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.utils.CmisResult;

/**
 * Provides an asynchronous mechanism to search all albums inside the CMIS
 * Server. <br/>
 * Returns a list of Document object with a baseType equals to cmisbook:album
 * 
 * @author Jean Marie Pascal
 * 
 */
public class AlbumsTask extends AsyncTask<Void, Void, CmisResult<List<Document>>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "AlbumsTask";

    /** The CMIS Session object. */
    private Session session;

    /** The activity to display the result. */
    private Activity activity;

    /** CMIS Query to retrieve all albums. */
    private static final String QUERY_ALL_ALBUMS = "SELECT * FROM cmis:document where cmis:objectTypeId = 'cmisbook:album'";

    /**
     * Instantiates a new albums task.
     * 
     * @param activity
     *            the activity to display the result
     * @param session
     *            the session associated
     */
    public AlbumsTask(Activity activity, Session session) {
        this.activity = activity;
        this.session = session;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<List<Document>> doInBackground(Void... arg0) {
        List<Document> listAlbums = null;
        Exception exception = null;

        // Try to execute a CMIS Query to retrieve all albums from the Server.
        try {
            ItemIterable<QueryResult> results = session.query(QUERY_ALL_ALBUMS, false);
            listAlbums = new ArrayList<Document>((int) results.getTotalNumItems());
            Document album = null;

            // Create a list of Albums (Document object) based on the result.
            for (QueryResult result : results) {
                album = (Document) session.getObject(session.createObjectId((String) result.getPropertyById(
                        PropertyIds.OBJECT_ID).getFirstValue()));
                listAlbums.add(album);
            }
        } catch (Exception e) {
            exception = e;
        }
        return new CmisResult<List<Document>>(exception, listAlbums);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(CmisResult<List<Document>> results) {
        // In case of exception, displays informations for debugging purpose.
        if (results.hasException()) {
            Toast.makeText(activity, results.getException().getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        } else if (activity instanceof AlbumsActivity) {
            // Display albums inside the listview.
            ((AlbumsActivity) activity).listAlbums(results.getData());
        }
    }
}
