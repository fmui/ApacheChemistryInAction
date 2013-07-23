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
package com.manning.cmis.theblend.android.tracks.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.albums.AlbumDetailsActivity;
import com.manning.cmis.theblend.android.constant.CmisBookIds;
import com.manning.cmis.theblend.android.utils.CmisResult;

/**
 * Provides an asynchronous mechanism to add a track to an album.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class AddTrackTask extends AsyncTask<Void, Void, CmisResult<Document>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "CreateAlbumTask";

    /** The activity to display the result. */
    private Activity activity;

    /** The track id. */
    private String trackId;

    /** The album to add a new track. */
    private Document album;

    /**
     * Instantiates a new adds the track task.
     * 
     * @param activity
     *            the activity to display the result
     * @param album
     *            the album to add a new track
     * @param id
     *            the id to add
     */
    public AddTrackTask(Activity activity, Document album, String id) {
        this.activity = activity;
        this.trackId = id;
        this.album = album;
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

        // Try to add the trackId inside the multi-valued property track of an
        // album.
        try {

            List<Object> tracks = album.getProperty(CmisBookIds.TRACKS).getValues();
            tracks.add(trackId);

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(CmisBookIds.TRACKS, tracks);

            album.updateProperties(properties);
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
        } else if (activity instanceof AlbumDetailsActivity) {
            // Request a refresh of tracks inside the listview.
            ((AlbumDetailsActivity) activity).refreshTracks();
        }
    }
}
