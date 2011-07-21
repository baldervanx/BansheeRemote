package org.nstamato.bansheeremote;

public class PlayerStatus implements Cloneable {
	
	public final static int PARTIAL_UPDATE = 1;
	public final static int FULL_UPDATE = 2;
	
	private PlayerState state = PlayerState.NOTREADY;
	private String track;
	private String artist;
	private String album;
	private int seekPosition;
	private int seekTotal;
	private boolean hasCover = false;
	
	public boolean getHasCover() {
		return hasCover;
	}

	public void setHasCover(boolean hasCover) {
		this.hasCover = hasCover;
	}

	public PlayerState getState() {
		return state;
	}

	public void setState(PlayerState state) {
		this.state = state;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public int getSeekTotal() {
		return seekTotal;
	}

	public void setSeekTotal(int seekTotal) {
		this.seekTotal = seekTotal;
	}

	public int getSeekPosition() {
		return seekPosition;
	}
	
	public void setSeekPosition(int seekPosition) {
		this.seekPosition = seekPosition;
	}
	
	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new AssertionError("Shall never ever get here."); 
		}
	}

	public int getRequiredUpdate(PlayerStatus prevStatus) {
		//Rather unlikely with two artists that has both the same track
		//name and same album name. But it is best to be thorough.
		if (!prevStatus.track.equals(track) ||
				!prevStatus.album.equals(album) ||
				!prevStatus.artist.equals(artist)) {
			return FULL_UPDATE;
		}
		return PARTIAL_UPDATE;
	}
}
