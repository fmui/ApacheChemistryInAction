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

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.albums.AlbumsActivity;
import com.manning.cmis.theblend.android.constant.BundleConstant;
import com.manning.cmis.theblend.android.utils.CmisResult;
import com.manning.cmis.theblend.android.utils.UIUtils;

/**
 * The Class SessionTask provides an asynchronous mechanism to create an
 * OpenCMIS Session Object.
 * 
 * @author Jean Marie Pascal
 */
public class SessionTask extends AsyncTask<Bundle, Void, CmisResult<Session>> {

    /** The Constant TAG for logging purpose. */
    private static final String TAG = "SessionTask";

    /** The username. */
    private String username;

    /** The password. */
    private String password;

    /** The url. */
    private String url;

    /** The activity. */
    private Activity activity;

    /** The progress dialog. */
    private ProgressDialog progressDialog;

    /**
     * Instantiates a new session task.
     * 
     * @param activity
     *            the activity
     */
    public SessionTask(Activity activity) {
        this.activity = activity;
        if (activity.getIntent().getExtras() != null) {
            Bundle b = activity.getIntent().getExtras().getBundle(BundleConstant.KEY_ACCOUNT);
            retrieveBundleValues(b);
        }
    }

    /**
     * Retrieve bundle values.
     * 
     * @param b
     *            the b
     */
    private void retrieveBundleValues(Bundle b) {
        username = b.getString(BundleConstant.KEY_HOSTNAME);
        password = b.getString(BundleConstant.KEY_HOSTNAME);
        url = b.getString(BundleConstant.KEY_HOSTNAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    public void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(this.activity.getText(R.string.create_session_title));
        progressDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.setTitle(this.activity.getText(R.string.create_session_title));
        progressDialog.setMessage(this.activity.getText(R.string.create_session_description));
        progressDialog.show();
        UIUtils.blockScreenOrientation(activity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<Session> doInBackground(Bundle... b) {
        Session session = null;
        Exception exception = null;
        try {
            if (b != null && b[0] != null) {
                retrieveBundleValues(b[0]);
            }

            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put(SessionParameter.USER, username);
            parameter.put(SessionParameter.PASSWORD, password);
            parameter.put(SessionParameter.ATOMPUB_URL, url);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            Repository repository = sessionFactory.getRepositories(parameter).get(0);
            parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
            session = sessionFactory.createSession(parameter);
        } catch (Exception e) {
            exception = e;
        }

        return new CmisResult<Session>(exception, session);

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(CmisResult<Session> results) {
        progressDialog.dismiss();

        // In case of exception, displays informations for debugging purpose.
        if (results.hasException()) {
            Toast.makeText(activity, results.getException().getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        } else if (activity instanceof LoginActivity) {
            // Start the album activity and pass the session object.
            Bundle b = new Bundle();
            b.putSerializable(BundleConstant.KEY_SESSION, results.getData());
            Intent i = new Intent(activity, AlbumsActivity.class);
            i.putExtra(BundleConstant.KEY_EXTRAS, b);
            activity.startActivity(i);
        }
    }
}
