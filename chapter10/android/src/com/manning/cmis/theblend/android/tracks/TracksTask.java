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
package com.manning.cmis.theblend.android.tracks;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.albums.AlbumDetailsActivity;
import com.manning.cmis.theblend.android.constant.CmisBookIds;
import com.manning.cmis.theblend.android.constant.BundleConstant;
import com.manning.cmis.theblend.android.utils.CmisResult;

// TODO: Auto-generated Javadoc
/**
 * Provides an asynchronous mechanism to have all tracks for a specified album. <br/>
 * 
 * @author Jean Marie Pascal
 * 
 */
public class TracksTask extends AsyncTask<Void, Void, CmisResult<List<Document>>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "TracksTask";

    /** The activity to display the result. */
    private Activity activity;

    /** The CMIS Session object. */
    private Session session;

    /** The album id. */
    private String albumId;

    /** The album object. */
    private Document album;

    /**
     * Instantiates a new tracks task.
     * 
     * @param activity
     *            the activity
     */
    public TracksTask(Activity activity) {
        this.activity = activity;

        // Retrieves information inside the Intent
        if (activity.getIntent().getExtras() != null) {
            Bundle b = activity.getIntent().getExtras().getBundle(BundleConstant.KEY_EXTRAS);
            session = (Session) b.getSerializable(BundleConstant.KEY_SESSION);
            albumId = b.getString(BundleConstant.KEY_ALBUM_ID);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<List<Document>> doInBackground(Void... arg0) {
        List<Document> tracksList = null;
        Exception exception = null;

        // Try to retrieve the album object, get the property track and retrieve
        // track object from track id
        try {
            session.removeObjectFromCache(albumId);
            album = (Document) session.getObject(albumId);

            List<Object> tracksId = album.getProperty(CmisBookIds.TRACKS).getValues();
            tracksList = new ArrayList<Document>(tracksId.size());

            for (Object track : tracksId) {
                tracksList.add((Document) session.getObject((String) track));
            }
        } catch (Exception e) {
            exception = e;
        }
        return new CmisResult<List<Document>>(exception, tracksList);
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
        } else if (activity instanceof AlbumDetailsActivity) {
            // Display tracks inside the listview.
            ((AlbumDetailsActivity) activity).listTracks(results.getData(), album);
        }
    }
}
