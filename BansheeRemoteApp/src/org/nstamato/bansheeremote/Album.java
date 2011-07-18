package org.nstamato.bansheeremote;

public class Album {

	private String title;

	private int albumId;

	public Album(String title, int albumId) {
		this.title = title;
		this.albumId = albumId;
	}

	public String getTitle() {
		return title;
	}
	
	public int getAlbumId() {
		return albumId;
	}
}
