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

import java.io.File;
import java.util.ArrayList;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ArtistBrowse extends ListActivity{
	ArrayList<String> artistList;
    ArrayAdapter<String> artistListAdapter; 
    final String filenameDB = "banshee.db";
    SQLiteDatabase bansheeDB;
    public ImageView icon;
    public TextView title;
    
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
        this.icon = (ImageView)this.findViewById(R.id.icon);
        this.title = (TextView)this.findViewById(R.id.title);
        this.title.setText("Artists");
        this.icon.setImageResource(R.drawable.artist);
        this.artistList = new ArrayList<String>();
        this.artistListAdapter = new ArrayAdapter<String>(this,R.layout.list_item,artistList);
        //this.header = new ArrayList<String>();
        //this.header.add("title");
        //this.headerAdapter = new ArrayAdapter<String>(this,R.layout.list_item,header);
        //setListAdapter(headerAdapter);
        setListAdapter(artistListAdapter);
        File db = null;
		try{
			//db = getFileStreamPath(filenameDB);
			db = Environment.getExternalStorageDirectory();
			String path = db.getAbsolutePath()+'/'+filenameDB;
			bansheeDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			String query = "SELECT Name FROM CoreArtists ORDER BY Name";
			Cursor c = bansheeDB.rawQuery(query,null);
			//int columnCount = c.getColumnCount();
			int name = c.getColumnIndex("Name");
			//int count = c.getCount();
			c.moveToFirst();
			while(c.isAfterLast()==false){
				String artist = c.getString(name);
				if(artist!=null)
					artistList.add(c.getString(name));
				c.moveToNext();
				//artistList.add("hello");
			}
			c.close();
			//artistList.add("hello");
			//Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
			//String result = c.getString(name);
		}catch(SQLiteException e){
			//Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
			Toast.makeText(this,"Something went wrong. Make sure the banshee database file is on your sd-card.",Toast.LENGTH_LONG).show();
		}
		catch(Exception e){
			//Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
			Toast.makeText(this,"Something went wrong. Make sure the banshee database file is on your sd-card.",Toast.LENGTH_LONG).show();
		}
		ListView l = getListView();
		l.setTextFilterEnabled(true);
		l.setFastScrollEnabled(true);
    }
	
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	String selected = this.artistListAdapter.getItem(position);
    	//Toast.makeText(this,selected,Toast.LENGTH_SHORT).show();
    	Intent i = new Intent(this,AlbumBrowse.class);
    	i.putExtra("Artist",selected);
    	startActivityForResult(i,2);
	}

}
