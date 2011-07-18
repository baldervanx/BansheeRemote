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

import android.graphics.Bitmap;

public class BansheeInstance {
	public String status;
	public Bitmap cover;
	public String track, artist, album;
	public int iseekposition, iseektotal;
	public boolean isCover;
	public String ip;
	public int port;
	
	public BansheeInstance(String status, Bitmap cover, String track, String artist, String album, int position, int total, boolean isCover, String ip, int port){
		this.status=status;
		this.cover = cover;
		this.track=track;
		this.artist=artist;
		this.album=album;
		this.iseekposition=position;
		this.iseektotal=total;
		this.ip=ip;
		this.port=port;
	}
}
