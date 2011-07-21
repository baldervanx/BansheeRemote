package org.nstamato.bansheeremote;

public enum ShuffleMode implements ResourceItem {
	OFF(R.string.shuffle_off), 
	TRACK(R.string.shuffle_track), 
	ARTIST(R.string.shuffle_artist), 
	ALBUM(R.string.shuffle_album), 
	SCORE(R.string.shuffle_score), 
	RATING(R.string.shuffle_rating);

	private int resourceId;

	private ShuffleMode(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public int getResourceId() {
		return resourceId;
	}

}
