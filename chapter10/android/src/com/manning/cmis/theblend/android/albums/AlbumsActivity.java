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

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manning.cmis.theblend.android.R;
import com.manning.cmis.theblend.android.albums.actions.CreateAlbumTask;
import com.manning.cmis.theblend.android.constant.BundleConstant;
import com.manning.cmis.theblend.android.properties.PropertyActivity;

/**
 * AlbumsActivity is responsible to display the list of cmisbook:album present
 * in the CMIS Server for the Blend Application.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class AlbumsActivity extends Activity {

    /** The OpenCMIS Server session. */
    private Session session;

    /** ProgressBar associated to the list. */
    private ProgressBar albumsProgressBar;

    /** The listview of the activity. */
    private ListView albumsListView;

    /** The empty view associated to the albums listview. */
    private View albumsEmptyView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        // Retrieves informations from Intent
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras().getBundle(BundleConstant.KEY_EXTRAS);
            session = (Session) b.getSerializable(BundleConstant.KEY_SESSION);
        }

        // Initiates UI Components
        albumsProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        albumsListView = (ListView) findViewById(R.id.listView);
        albumsEmptyView = findViewById(R.id.empty);
        TextView evt = (TextView) findViewById(R.id.empty_text);
        evt.setText(R.string.no_albums);

        // Simple click on item : goes to Album Details activity
        albumsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                displayAlbumProperties((Document) parent.getItemAtPosition(position));
                return true;
            }
        });

        // Simple click on item : goes to Album Details activity
        albumsListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                displayAlbumsDetails((Document) l.getItemAtPosition(position));
            }
        });

        // Display the progressbar until having the list of Albums from server.
        setListShown(false);

        // Start the asynctask to retrieve list of albums from the server
        listAlbums();
    }

    /**
     * Display albums details screen via Intent.
     * 
     * @param album
     *            : the album (Document object)
     */
    public void displayAlbumsDetails(Document album) {
        Bundle b = new Bundle();
        b.putSerializable(BundleConstant.KEY_SESSION, session);
        b.putString(BundleConstant.KEY_ALBUM_ID, (String) album.getId());
        Intent i = new Intent(this, AlbumDetailsActivity.class);
        i.putExtra(BundleConstant.KEY_EXTRAS, b);
        startActivity(i);
    }

    /**
     * Display Properties details screen via Intent.
     * 
     * @param album
     *            : the album (Document object)
     */
    public void displayAlbumProperties(Document album) {
        Bundle b = new Bundle();
        b.putSerializable(BundleConstant.KEY_SESSION, session);
        b.putString(BundleConstant.KEY_CMISOBJECT_ID, (String) album.getId());
        Intent i = new Intent(this, PropertyActivity.class);
        i.putExtra(BundleConstant.KEY_EXTRAS, b);
        startActivity(i);
    }

    /**
     * Start AsyncTask to retrieve albums list.
     */
    public void listAlbums() {
        new AlbumsTask(this, session).execute();
    }

    /**
     * Callback method for AlbumTask. Creates the listAdapter based on data
     * retrieved.
     * 
     * @param albums
     *            the albums : List of albums from server.
     */
    public void listAlbums(List<Document> albums) {
        if (albums != null && !albums.isEmpty()) {
            albumsListView.setAdapter(new AlbumsAdapter(AlbumsActivity.this, albums));
        } else {
            albumsListView.setEmptyView(albumsEmptyView);
        }
        setListShown(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.albums, menu);
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
        case R.id.menu_add:
            addAlbum();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
            albumsListView.setVisibility(View.VISIBLE);
            albumsProgressBar.setVisibility(View.GONE);
        } else {
            albumsEmptyView.setVisibility(View.GONE);
            albumsListView.setVisibility(View.GONE);
            albumsProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Actions : Create a cmisbook:album inside the repository. <br>
     * Displays a popup to get the information from the user.
     */
    public void addAlbum() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View form = inflater.inflate(R.layout.create_album, null, false);
        new AlertDialog.Builder(this).setTitle(R.string.add_album_title).setView(form)
                .setPositiveButton(R.string.create_add_album, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String title = ((TextView) form.findViewById(R.id.album_title)).getText().toString();
                        String path = ((TextView) form.findViewById(R.id.parent_folder_path)).getText().toString();

                        new CreateAlbumTask(AlbumsActivity.this, session, path, title).execute();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
