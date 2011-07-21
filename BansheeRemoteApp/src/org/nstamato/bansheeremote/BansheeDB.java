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
	
    public List<Artist> getArtists() {
		SQLiteDatabase db = getDB();
		String query = "SELECT Name FROM CoreArtists ORDER BY Name";
		List<Artist> artistList = new ArrayList<Artist>();
		Cursor c = db.rawQuery(query,null);
		c.moveToFirst();
		int name = c.getColumnIndex("Name");
		while(c.isAfterLast()==false){
			String artist = c.getString(name);
			if (artist != null) {
				artistList.add(new Artist(artist));
			}
			c.moveToNext();
		}
		c.close();
		return artistList;
    }
    
    public List<Track> getTracks(int forAlbumId) {
		SQLiteDatabase db = getDB();
		String query = "SELECT Title,Uri FROM CoreTracks";
		String[] params = null;
		
		if (forAlbumId >= 0) {
			query+=" WHERE AlbumID=?";
			params = new String[]{String.valueOf(forAlbumId)};
			query+=" ORDER BY TrackNumber";
		} else {
			query+=" ORDER BY Title";
		}
		List<Track> trackList = new ArrayList<Track>();
		Cursor c = db.rawQuery(query, params);
		int titleColumn = c.getColumnIndex("Title");
		int uriColumn = c.getColumnIndex("Uri");
		c.moveToFirst();
		while(c.isAfterLast()==false){
			String title = c.getString(titleColumn);
			String uri = c.getString(uriColumn);
			if (title != null) {
				trackList.add(new Track(title, uri));
			}
			c.moveToNext();
		}
		return trackList;
    }
    
	public List<Album> getAlbums(String forArtist) {
		SQLiteDatabase db = getDB();
		//Build the query
		String query = "SELECT Title,AlbumID FROM CoreAlbums";
		String[] params = null;
		if (forArtist != null) {
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
