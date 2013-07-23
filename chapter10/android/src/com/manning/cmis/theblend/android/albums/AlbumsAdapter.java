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
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.manning.cmis.theblend.android.constant.CmisBookIds;

/**
 * Responsible to display album item into the listview.
 * 
 * @author Jean Marie Pascal
 * 
 */
public class AlbumsAdapter extends ArrayAdapter<Document> {

    /** The list of albums from the server. */
    private List<Document> albumsLibrary;

    /** Android Service inflater. */
    private LayoutInflater inflater;

    /** The context associated to the listview. */
    private Context context;

    /**
     * Instantiates a new albums adapter.
     * 
     * @param context
     *            : context associated to the listview
     * @param albums
     *            : list of albums.
     */
    public AlbumsAdapter(Context context, List<Document> albums) {
        super(context, android.R.layout.simple_list_item_2, albums);
        this.context = context;
        this.albumsLibrary = albums;
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

        // Displays information Name + Number of tracks available
        holder = (ViewHolder) convertView.getTag();
        holder.topText.setText((String) albumsLibrary.get(position).getName());
        PropertyData<Object> trackList = albumsLibrary.get(position).getProperty(CmisBookIds.TRACKS);
        holder.bottomText.setText(((trackList != null) ? trackList.getValues().size() : 0) + " tracks");

        return convertView;
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
