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

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class AlbumBrowse extends MusicListActivity<Album> {
    
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Album selected = getItem(position);
    	Intent i = new Intent(this, TrackBrowse.class);
    	i.putExtra("Album",selected.getName());
    	i.putExtra("AlbumID",selected.getAlbumId());
    	startActivityForResult(i,2);
	}
	
	@Override
	protected List<Album> getMusicItems(Bundle extras) {
		String artist = extras != null ? extras.getString("Artist") : null;
		return bansheeDB.getAlbums(artist);
	}

	@Override
	protected String getTitleText(Bundle extras) {
		String artist = extras != null ? extras.getString("Artist") : null;
		if (artist == null) {
			return getResources().getString(R.string.title_albums);
		}
		return artist;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.album;
	}
}
