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

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.chemistry.opencmis.client.api.Document;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.manning.cmis.theblend.android.constant.CmisBookIds;

/**
 * Responsible to display track item into the listview.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class TracksAdapter extends ArrayAdapter<Document> {

    /** The track list from the server. */
    private List<Document> trackList;

    /** Android Service inflater. */
    private LayoutInflater inflater;

    /** The context associated to the listview. */
    private Context context;

    /**
     * Instantiates a new tracks adapter.
     * 
     * @param context
     *            the context associated to the listview
     * @param tracks
     *            list of tracks
     */
    public TracksAdapter(Context context, List<Document> tracks) {
        super(context, android.R.layout.simple_list_item_2, tracks);
        this.context = context;
        this.trackList = tracks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ArrayAdapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // Try to reuse previous view from the listview.
        // If the view doesn't exist, create a new one.
        if (convertView == null) {
            holder = new ViewHolder();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
            holder.topText = (TextView) convertView.findViewById(android.R.id.text1);
            holder.bottomText = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(holder);
        }

        // Displays information Name + Artist name
        holder = (ViewHolder) convertView.getTag();
        holder.topText.setText((String) trackList.get(position).getName());
        holder.bottomText.setText(getArtistValue(trackList.get(position))
                + getFormattedLength((BigInteger) trackList.get(position).getProperty(CmisBookIds.LENGTH).getValue()));

        return convertView;
    }

    /**
     * Utility method to display or not the artist property value.
     * 
     * @param track
     *            Document object representing the track
     * @return String representation of artist value.
     */
    private String getArtistValue(Document track) {
        String value = "";
        if (track.getProperty(CmisBookIds.ARTIST) != null && track.getProperty(CmisBookIds.ARTIST).getValue() != null) {
            value = track.getProperty(CmisBookIds.ARTIST).getValueAsString();
        }
        return value;
    }

    /**
     * Utility method to transform time in seconds into formatted text
     * 
     * @param length
     *            : time in seconds
     * @return formatted string representing the value of length.
     */
    private String getFormattedLength(BigInteger length) {
        if (length != null) {
            return String.format(" - %d min, %d sec", TimeUnit.SECONDS.toMinutes(length.longValue()),
                    length.longValue() - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(length.longValue())));
        } else {
            return "";
        }
    }

    /**
     * Utility Class ViewHolder.
     */
    private class ViewHolder {

        /** The top text. */
        TextView topText;

        /** The bottom text. */
        TextView bottomText;
    }
}
