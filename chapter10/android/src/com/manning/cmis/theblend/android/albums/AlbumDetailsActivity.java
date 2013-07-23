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

import java.io.File;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.manning.cmis.theblend.android.R;
import com.manning.cmis.theblend.android.constant.BundleConstant;
import com.manning.cmis.theblend.android.properties.PropertiesTask;
import com.manning.cmis.theblend.android.properties.PropertyActivity;
import com.manning.cmis.theblend.android.tracks.TracksAdapter;
import com.manning.cmis.theblend.android.tracks.TracksTask;
import com.manning.cmis.theblend.android.tracks.actions.AddTrackTask;
import com.manning.cmis.theblend.android.tracks.actions.DownloadTask;

/**
 * The Class AlbumDetailsActivity is responsible to display the list of tracks
 * and properties for a specific album. <br/>
 * Both tracks and properties data are displayed in the same Listview component.<br/>
 * Depending on user selection, the listview is refreshed with the appropriate
 * data (tracks or properties).<br/>
 * This activity can use 2 differents asynctask to retrieve data.
 * 
 * @author Jean Marie Pascal
 */
public class AlbumDetailsActivity extends Activity implements OnTabChangeListener {

    /** Id of Properties TAB. */
    private static final String TAB_PROPERTIES = "Properties";

    /** ID of Tracks TAB. */
    private static final String TAB_TRACKS = "Tracks";

    /** The OpenCMIS Server session. */
    private Session session;

    /** The album object. */
    private CmisObject albumObject;

    /** The m tab host. */
    private TabHost mTabHost;

    /** ProgressBar associated to the list. */
    private ProgressBar albumProgressBar;

    /** The listview of the activity. */
    private ListView albumListView;

    /** The empty view associated to the screen. */
    private View albumEmptyView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_tabs);

        // Start the asynctask to retrieve list of properties
        new PropertiesTask(this).execute();

        // Initiates UI Components
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        albumProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        albumListView = (ListView) findViewById(R.id.listView);
        albumEmptyView = findViewById(R.id.empty);
        TextView evt = (TextView) findViewById(R.id.empty_text);
        evt.setText(R.string.no_tracks);
        setListShown(false);
        setupTabs();
    }

    /**
     * Callback method for TracksTask. Creates the listAdapter based on list of
     * retrieved.
     * 
     * @param trackList
     *            the track list
     * @param object
     *            the album object
     */
    public void listTracks(List<Document> trackList, CmisObject object) {
        if (trackList != null && !trackList.isEmpty()) {
            albumListView.setAdapter(new TracksAdapter(this, trackList));
        } else {
            albumListView.setAdapter(new TracksAdapter(this, trackList));
            albumListView.setEmptyView(albumEmptyView);
        }
        albumObject = object;
        setTitle("Properties : " + albumObject.getName());
        setListShown(true);
    }

    /**
     * Callback method for PropertiesTask. Associates the listAdapter based on
     * data retrieved.
     * 
     * @param adapter
     *            the adapter
     * @param object
     *            the object
     */
    public void listProperties(SimpleAdapter adapter, CmisObject object) {
        albumListView.setVisibility(View.VISIBLE);
        albumObject = object;
        albumListView.setAdapter(adapter);
        setTitle("Properties : " + albumObject.getName());
        setListShown(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Actions available inside the activity.
        switch (item.getItemId()) {
        case R.id.menu_add_track:
            addTrack();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Setup tabs of the screen.
     */
    private void setupTabs() {
        mTabHost.setup();

        mTabHost.addTab(newTab(TAB_PROPERTIES, R.string.menu_properties, android.R.id.tabcontent));
        mTabHost.addTab(newTab(TAB_TRACKS, R.string.menu_tracks, android.R.id.tabcontent));
        mTabHost.setOnTabChangedListener(this);
    }

    /**
     * Responsible to create a New tab.
     * 
     * @param tag
     *            the tag associated to the tab.
     * @param labelId
     *            the label id of the tab
     * @param tabContentId
     *            the tab content id
     * @return the tab spec associated to the tabHost.
     */
    private TabSpec newTab(String tag, int labelId, int tabContentId) {
        TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setContent(tabContentId);
        tabSpec.setIndicator(this.getText(labelId));
        return tabSpec;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
     */
    public void onTabChanged(String tabId) {
        // Listener responsible to refresh the listview depending on user
        // choice.
        setListShown(false);
        if (TAB_PROPERTIES.equals(tabId)) {
            refreshProperties();
        } else if (TAB_TRACKS.equals(tabId)) {
            refreshTracks();
        }
    }

    /**
     * Action to refresh the listview with properties informations.
     */
    public void refreshProperties() {
        mTabHost.setCurrentTabByTag(TAB_PROPERTIES);
        new PropertiesTask(this).execute();
    }

    /**
     * Action to refresh the listview with tracks list informations.
     */
    public void refreshTracks() {
        mTabHost.setCurrentTabByTag(TAB_TRACKS);
        new TracksTask(this).execute();

        // Simple click action : Download and play the track inside the device.
        albumListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View arg1, int position, long arg3) {
                openin((Document) adapter.getItemAtPosition(position));

            }
        });

        // Long click action : Display the list of properties for this track.
        albumListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                properties((Document) parent.getItemAtPosition(position));
                return true;
            }
        });
    }

    /**
     * Actions : Displays properties for the specified track. <br>
     * 
     * @param track
     *            the track
     */
    private void properties(Document track) {
        Bundle b = new Bundle();
        b.putSerializable(BundleConstant.KEY_SESSION, session);
        b.putString(BundleConstant.KEY_CMISOBJECT_ID, (String) track.getId());
        Intent i = new Intent(this, PropertyActivity.class);
        i.putExtra(BundleConstant.KEY_EXTRAS, b);
        startActivity(i);
    }

    /**
     * Actions : Download and play the track. <br>
     * 
     * @param doc
     *            the track to play with an installed application inside the
     *            device
     */
    private void openin(Document doc) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
        new DownloadTask(this, doc, new File(getDownloadFolder(), doc.getName())).execute();
    }

    /**
     * Gets the download folder inside the device.
     * 
     * @return the download folder
     */
    public File getDownloadFolder() {
        File folder = null;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
                folder = Environment.getDownloadCacheDirectory();
            }

            folder = new File(folder, "TheBlend");
        } catch (Exception e) {
            Log.e("TrackActivity", "Error during getting download folder : " + e.getMessage());
        }

        return folder;
    }

    /**
     * Display or not the listView. If not displayed, an indeterminate
     * progressbar is displayed.
     * 
     * @param shown
     *            : true to display the listview. False otherwise
     * 
     */
    protected void setListShown(Boolean shown) {
        if (shown) {
            albumListView.setVisibility(View.VISIBLE);
            albumProgressBar.setVisibility(View.GONE);
        } else {
            albumEmptyView.setVisibility(View.GONE);
            albumListView.setVisibility(View.GONE);
            albumProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Actions : Associate a cmisbook:track id as property for a specific album
     * object. <br>
     * Displays a popup to get the information from the user.
     */
    public void addTrack() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View form = inflater.inflate(R.layout.add_track, null, false);
        new AlertDialog.Builder(this).setTitle(R.string.menu_add_track).setView(form)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String id = ((TextView) form.findViewById(R.id.add_track_id)).getText().toString();

                        new AddTrackTask(AlbumDetailsActivity.this, (Document) albumObject, id).execute();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
