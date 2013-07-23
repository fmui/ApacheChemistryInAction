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
package com.manning.cmis.theblend.android.albums.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.albums.AlbumsActivity;
import com.manning.cmis.theblend.android.constant.CmisBookIds;
import com.manning.cmis.theblend.android.utils.CmisResult;

/**
 * Provides an asynchronous mechanism to create a cmisbook:album.
 * 
 * @author Jean Marie Pascal
 */
public class CreateAlbumTask extends AsyncTask<Void, Void, CmisResult<Document>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "CreateAlbumTask";

    /** The CMIS Session object. */
    private Session session;

    /** The activity to display the result. */
    private Activity activity;

    /** Album title. */
    private String albumTitle;

    /** The parent folder path. */
    private String albumParentfolderPath;

    /**
     * Instantiates a new creates the album task.
     * 
     * @param activity
     *            the activity to display the result
     * @param session
     *            the CMIS Session object
     * @param folderPath
     *            the parent folder path
     * @param title
     *            the album title
     */
    public CreateAlbumTask(Activity activity, Session session, String folderPath, String title) {
        this.activity = activity;
        this.session = session;
        this.albumTitle = title;
        this.albumParentfolderPath = folderPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<Document> doInBackground(Void... arg0) {
        Document doc = null;
        Exception exception = null;

        // Try to retrieve the parent folder object and then create an album.
        try {
            Folder folder = (Folder) session.getObjectByPath(albumParentfolderPath);

            // Create the map of properties associated to the future album.
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, CmisBookIds.BOOK_ALBUM);
            properties.put(PropertyIds.BASE_TYPE_ID, ObjectType.DOCUMENT_BASETYPE_ID);
            properties.put(PropertyIds.NAME, albumTitle);

            doc = folder.createDocument(properties, null, null);
        } catch (Exception e) {
            exception = e;
        }
        return new CmisResult<Document>(exception, doc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(CmisResult<Document> results) {
        // In case of exception, displays informations for debugging purpose.
        if (results.hasException()) {
            Toast.makeText(activity, results.getException().getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        } else if (activity instanceof AlbumsActivity) {
            // Display albums inside the listview.
            ((AlbumsActivity) activity).listAlbums();
        }
    }
}
