/**
 * 
 */
package org.nstamato.bansheeremote;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class MusicListAdapter<T extends MusicItem> extends ArrayAdapter<T> {
	private static LayoutInflater inflater = null;
	
    public MusicListAdapter(Activity context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		 
		if (null == convertView) {
			row = inflater.inflate(R.layout.list_item, null);
		} else {
			row = convertView;
		}

		TextView tv = (TextView)row;
		tv.setText(getItem(position).getName());

		return row;
	}
}