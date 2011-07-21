package org.nstamato.bansheeremote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Extremely simple cache for now, could possibly be improved later. 
 * Mainly used to check whether a new cover needs to be fetched or not.
 */
public class CoverCache {

	private Bitmap noCover;
	private Bitmap currentCover;
	private String currentArtist;
	private String currentAlbum;
	
	public CoverCache(Context context) {
		noCover = BitmapFactory.decodeResource(context.getResources(),R.drawable.no_cover_art);
	}

	public Bitmap getCover(PlayerStatus status) {
		if (!status.getHasCover()) {
			return noCover;
		}
		if (currentCover != null && 
				status.getAlbum().equals(currentAlbum) &&
				status.getArtist().equals(currentArtist)) {
			return currentCover;
		}
		return null;
	}

	public void putCover(PlayerStatus status, Bitmap cover) {
		currentArtist = status.getArtist();
		currentAlbum = status.getAlbum();
		currentCover = cover;
	}
}
