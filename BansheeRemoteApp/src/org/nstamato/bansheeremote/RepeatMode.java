package org.nstamato.bansheeremote;

public enum RepeatMode implements ResourceItem {
	OFF(R.string.repeat_off), 
	SINGLE(R.string.repeat_single), 
	ALL(R.string.repeat_all); 

	private int resourceId;

	private RepeatMode(int resourceId) {
		this.resourceId = resourceId;
	}
	
	public int getResourceId() {
		return resourceId;
	}

}
