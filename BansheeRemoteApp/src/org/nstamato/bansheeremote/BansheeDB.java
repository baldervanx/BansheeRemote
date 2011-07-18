package org.nstamato.bansheeremote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class BansheeDB {

    final String filenameDB = "banshee.db";
    private SQLiteDatabase bansheeDB;
	
	public List<Album> getAlbums(String forArtist) {
		SQLiteDatabase db = getDB();
		//Build the query
		String query = "SELECT Title,AlbumID FROM CoreAlbums";
		String[] params = null;
		if (!forArtist.equals("Albums")) {
			query+=" WHERE ArtistName=?";
			params = new String[]{forArtist};
		}
		query+=" ORDER BY Title";

		Cursor c = db.rawQuery(query,params);
		int name = c.getColumnIndex("Title");
		int albumIDColumn = c.getColumnIndex("AlbumID");
		
		List<Album> albumList = new ArrayList<Album>();
		c.moveToFirst();
		while (!c.isAfterLast()){
			String title = c.getString(name);
			String albumId = c.getString(albumIDColumn);
			if (title != null)
				albumList.add(new Album(title, Integer.parseInt(albumId)));
			c.moveToNext();
		}
		c.close();
		return albumList;
	}
	
	private SQLiteDatabase getDB() {
		if (bansheeDB == null) {
			File db = Environment.getExternalStorageDirectory();
			String path = db.getAbsolutePath()+'/'+filenameDB;
			bansheeDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
		return bansheeDB;
	}
	
}
