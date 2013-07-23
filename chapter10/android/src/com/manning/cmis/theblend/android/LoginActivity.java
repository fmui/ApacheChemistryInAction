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
package com.manning.cmis.theblend.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.manning.cmis.theblend.android.constant.BundleConstant;

/**
 * LoginActivity is responsible to display a login screen for the Blend
 * Application.
 * 
 * @author Jean Marie Pascal
 */
public class LoginActivity extends Activity {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                requestSession();
            }
        });

        setTitle(R.string.title_activity_main);
    }

    /**
     * Retrieve values from the form and request an Asynctask to create an
     * OpenCMIS Session object.
     */
    private void requestSession() {
        String hostname, username, password;

        // Finds in the layout all textViews
        TextView tv = (TextView) findViewById(R.id.repository_hostname);
        hostname = tv.getText().toString();

        tv = (TextView) findViewById(R.id.repository_username);
        username = tv.getText().toString();

        tv = (TextView) findViewById(R.id.repository_password);
        password = tv.getText().toString();

        // Creates a data package to pass to the asynctask
        Bundle b = new Bundle();
        b.putString(BundleConstant.KEY_HOSTNAME, hostname);
        b.putString(BundleConstant.KEY_USERNAME, username);
        b.putString(BundleConstant.KEY_PASSWORD, password);

        // Initiates and starts the SessionTask
        new SessionTask(this).execute(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_manning:
            openIn(R.string.menu_manning_url);
            return true;
        case R.id.menu_chemistry:
            openIn(R.string.menu_chemistry_url);
            return true;
        case R.id.menu_cmis_specification:
            openIn(R.string.menu_cmis_specification_url);
            return true;
        case R.id.menu_cmis_chemistry:
            openIn(R.string.menu_cmis_chemistry_url);
            return true;
        case R.id.menu_javadoc:
            openIn(R.string.menu_javadoc_url);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Utility class reponsible to launch a Web Browser to access the specific
     * URL defined by its id.
     * 
     * @param id
     *            : The ID that represents an url.
     */
    public void openIn(int id) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(id).toString()));
        startActivity(intent);
    }

}
