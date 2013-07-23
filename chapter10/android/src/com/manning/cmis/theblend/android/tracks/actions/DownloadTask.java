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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.manning.cmis.theblend.android.R;
import com.manning.cmis.theblend.android.utils.CmisResult;
import com.manning.cmis.theblend.android.utils.UIUtils;

/**
 * Provides an asynchronous task to download the content of a document
 * object.</br> Displays a ProgressDialog windows to display the download
 * progress in percentage.
 * 
 * @author Jean Marie Pascal
 */
public class DownloadTask extends AsyncTask<Void, Integer, CmisResult<File>> {
    /** The Constant TAG for logging purpose. */
    private static final String TAG = "DownloadTask";

    /** The Constant MAX_BUFFER_SIZE. */
    private static final int MAX_BUFFER_SIZE = 1024;

    /** Number of bytes downloaded. */
    private int downloaded;

    /** The document to download. */
    private Document doc;

    /** The destination file inside the device. */
    private File destFile;

    /** The activity to display the result. */
    private Activity activity;

    /** The undeterminate progress dialog. */
    private ProgressDialog progressDialog;

    /** Number of bytes to download. */
    private long totalSize;

    /**
     * Instantiates a new download task.
     * 
     * @param document
     *            the document
     * @param destFile
     *            the dest file
     */
    public DownloadTask(Activity activity, Document document, File destFile) {
        this.activity = activity;
        this.destFile = destFile;
        this.doc = document;
        totalSize = doc.getContentStreamLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Initiate the progress dialog.
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(this.activity.getText(R.string.download));
        progressDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        progressDialog.setCancelable(true);
        progressDialog.setTitle(this.activity.getText(R.string.download) + " : " + doc.getName());
        progressDialog.setMessage(this.activity.getText(R.string.download_progress));
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        UIUtils.blockScreenOrientation(activity);

        downloaded = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected CmisResult<File> doInBackground(Void... params) {
        // Copy stream from Server to a device file.
        ContentStream contentStream = doc.getContentStream();
        return createFileFromStream(contentStream.getStream(), doc.getContentStreamLength(), destFile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(CmisResult<File> results) {
        super.onPostExecute(results);
        UIUtils.unBlockScreenOrientation(activity);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // If everything worked, try to play the track.
        if (!results.hasException()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri data = Uri.fromFile(results.getData());
            intent.setDataAndType(data, doc.getContentStreamMimeType());

            // Find if there's a player able to play the track
            PackageManager pm = activity.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (activities.size() > 0) {
                activity.startActivity(intent);
            } else {
                // if not, display information.
                Toast.makeText(activity, R.string.no_player, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, Log.getStackTraceString(results.getException()));
            Toast.makeText(activity, R.string.error_download, Toast.LENGTH_LONG).show();
            Toast.makeText(activity, results.getException().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int percent = Math.round(((float) values[0] / totalSize) * 100);
        progressDialog.setProgress(percent);
    }

    /**
     * Create File From Stream.
     * 
     * @param sourceStream
     *            the stream from the server
     * @param size
     *            the size of the track to download
     * @param deviceFile
     *            the device file where the binary are stored.
     * @return true, if successfully downloaded.
     */
    public CmisResult<File> createFileFromStream(InputStream sourceStream, long size, File deviceFile) {
        deviceFile.getParentFile().mkdirs();
        createUniqueName(deviceFile);
        OutputStream os = null;
        Exception exception = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(deviceFile));

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            while (size > 0) {
                if (size - downloaded < MAX_BUFFER_SIZE) {
                    buffer = new byte[(int) (size - downloaded)];
                }

                int read = sourceStream.read(buffer);
                if (read == -1) {
                    break;
                }

                os.write(buffer, 0, read);
                downloaded += read;
                publishProgress(downloaded);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            exception = e;
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            exception = e;
        } finally {
            closeStream(sourceStream);
            closeStream(os);
        }
        return new CmisResult<File>(exception, deviceFile);
    }

    /**
     * Utility method to close a stream.
     * 
     * @param stream
     *            the stream
     */
    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Utility method to create file unique name.
     * 
     * @param file
     *            the file to test.
     * @return the file with a unique name.
     */
    private static File createUniqueName(File file) {
        if (!file.exists()) {
            return file;
        }

        int index = 1;

        File tmpFile = file;
        while (index < 500) {
            tmpFile = new File(tmpFile.getParentFile(), tmpFile.getName() + "-" + index);
            if (!tmpFile.exists()) {
                return tmpFile;
            }
            index++;
        }
        return null;
    }
}
