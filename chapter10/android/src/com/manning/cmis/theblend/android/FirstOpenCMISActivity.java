package com.manning.cmis.theblend.android;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This class illustrate how it's possible to create a single simple activity
 * which
 * <ul>
 * <li>creates a Session Factory</li>
 * <li>creates a Session object based on parameters defined in a map</li>
 * <li>uses the session to get a folder object based on this path</li>
 * <li>lists the folder and retrieve all its children</li>
 * <li>displays some properties as simple string values into a TextView</li>
 * </ul>
 * 
 * @author Jean Marie Pascal
 */
public class FirstOpenCMISActivity extends Activity {

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FirstOpenCMISAsyncTask().execute();
    }

    /**
     * This AsyncTask is reponsible to use the OpenCMIS Android client to get
     * all children (documents and/or folders) of /media folder.
     */
    private class FirstOpenCMISAsyncTask extends AsyncTask<Void, Void, String> {

        /*
         * (non-Javadoc)
         * 
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected String doInBackground(Void... arg0) {

            // Initiates a Session Factory
            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            // Initiates connection session parameters.
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put(SessionParameter.USER, "admin");
            parameter.put(SessionParameter.PASSWORD, "admin");
            parameter.put(SessionParameter.ATOMPUB_URL, "http://192.168.1.36:8081/inmemory/atom/");
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

            // Retrieves repository information and create the session object.
            Repository repository = sessionFactory.getRepositories(parameter).get(0);
            parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
            Session session = sessionFactory.createSession(parameter);

            // Retrieves media folder and list all this children.
            String listChildren = "";
            Folder mediaFolder = (Folder) session.getObjectByPath("/media");
            ItemIterable<CmisObject> children = mediaFolder.getChildren();
            for (CmisObject o : children) {
                listChildren += o.getName() + " - " + o.getType().getDisplayName() + " - " + o.getCreatedBy() + "\b\n";
            }

            return listChildren;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            // Retrieves the Textview UI Component and displays all the
            // children.
            TextView tv = (TextView) (FirstOpenCMISActivity.this).findViewById(R.id.opencmis_text);
            tv.setText(result);
        }
    }
}
