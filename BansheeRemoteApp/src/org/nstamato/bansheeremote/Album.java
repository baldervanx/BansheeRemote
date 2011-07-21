package org.nstamato.bansheeremote;

public class Album extends MusicItem {

	private int albumId;

	public Album(String title, int albumId) {
		this.name = title;
		this.albumId = albumId;
	}

	public int getAlbumId() {
		return albumId;
	}
}
