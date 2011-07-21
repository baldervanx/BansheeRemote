package org.nstamato.bansheeremote;

public class Track extends MusicItem {

	private String uri;

	public Track(String title, String uri) {
		this.name = title;
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}
}
