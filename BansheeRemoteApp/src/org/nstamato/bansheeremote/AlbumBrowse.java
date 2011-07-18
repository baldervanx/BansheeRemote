package org.nstamato.bansheeremote;

/*
BansheeRemote

Copyright (C) 2010 Nikitas Stamatopoulos

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlbumBrowse extends ListActivity {
    private AlbumListAdapter albumListAdapter; 
    private BansheeDB bansheeDB = new BansheeDB();
    public ImageView icon;
    public TextView title;
    public String titleText="";
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		setResult(resultCode,data);
		if(resultCode==RESULT_OK)
			finish();
	}
    
	public void onCreate(Bundle savedInstanceState) {
		//this.setFastScrollEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse);
        Bundle extras = getIntent().getExtras();
        this.titleText = extras.getString("Artist");
        this.icon = (ImageView)this.findViewById(R.id.icon);
        this.title = (TextView)this.findViewById(R.id.title);
        this.title.setText(titleText);
        this.icon.setImageResource(R.drawable.album);
		try{
			List<Album> albums = bansheeDB.getAlbums(titleText);
			this.albumListAdapter = new AlbumListAdapter(this, R.layout.list_item, albums);
			setListAdapter(albumListAdapter);
		} catch(Exception e){
			Toast.makeText(this,"Something went wrong. Make sure the banshee database file is on your sd-card.",Toast.LENGTH_LONG).show();
		}
		ListView l = getListView();
		l.setTextFilterEnabled(true);
		l.setFastScrollEnabled(true);
    }
	
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Album selected = this.albumListAdapter.getItem(position);
    	Intent i = new Intent(this,SongBrowse.class);
    	i.putExtra("Album",selected.getTitle());
    	i.putExtra("AlbumID",selected.getAlbumId());
    	startActivityForResult(i,2);
	}
	
	private static class AlbumListAdapter extends ArrayAdapter<Album> {
		private static LayoutInflater inflater = null;
		
	    public AlbumListAdapter(Activity context, int textViewResourceId,
				List<Album> objects) {
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
			tv.setText(getItem(position).getTitle());
	 
			return row;
		}
	}

}
