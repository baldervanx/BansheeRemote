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

public class TrackBrowse extends MusicListActivity<Track> {
    	
	public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Track selected = getItem(position);
    	Intent i = new Intent();
    	i.putExtra("Uri", selected.getUri());
    	setResult(RESULT_OK, i);
    	finish();
	}

	@Override
	protected int getIconResource() {
		return R.drawable.songs;
	}

	@Override
	protected List<Track> getMusicItems(Bundle extras) {
		int albumId = extras != null ? extras.getInt("AlbumID") : 0;
		return bansheeDB.getTracks(albumId);
	}

	@Override
	protected String getTitleText(Bundle extras) {
		String album = extras != null ? extras.getString("Album") : null;
		if (album == null) {
			return getResources().getString(R.string.title_tracks);
		}
		return album;
	}

}
