
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class main extends Activity {
	
	private RemoteProtocol protocol;
	
	private ImageButton prev, playpause, next, mute, artists, albums, songs;
	private PhoneStateListener phoneListener;
	private TelephonyManager tm;
	private TextView track, artist, album, seekposition, seektotal;
	private SeekBar seekbar;
	private ImageView cover;
		
	private static final int SETTINGS_ITEM = 0;
	private static final int SHUFFLE_ITEM = 1;
	private static final int REPEAT_ITEM = 2;
	private static final int EXIT_ITEM = 3;

	private static final int SETTINGS_REQUEST_CODE = 0;
	private static final int BROWSE_REQUEST_CODE = 2;
	
	private final Handler update = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			PlayerStatus status = protocol.getPlayerStatus();
			if (msg.what == PlayerStatus.FULL_UPDATE) {
				playpause.setImageResource(status.getState() ==  PlayerState.PLAYING ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
				track.setText(status.getTrack());
				artist.setText(status.getArtist());
				album.setText(status.getAlbum());
				
				seektotal.setText(formatTime(status.getSeekTotal()));
				seekbar.setMax(status.getSeekTotal());
				cover.setImageBitmap(protocol.getCover());
			}

			seekbar.setProgress(status.getSeekPosition());
			seekposition.setText(formatTime(status.getSeekPosition()));
		}
	};

	
	private static String formatTime(int seconds) {
		String leading = (seconds % 60 < 10) ? "0" : "";
		if(seconds >= 60)
			return (seconds / 60) + ":" + leading + (seconds % 60);
		else
			return "0:" + leading + seconds;
	}
		
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item1 = menu.add(Menu.NONE,SETTINGS_ITEM,0,"Settings");
		item1.setIcon(android.R.drawable.ic_menu_edit);
		//MenuItem item2 = menu.add(0,1,1,"Sync");
		//item2.setIcon(R.drawable.ic_menu_refresh);
		MenuItem item2 = menu.add(Menu.NONE,SHUFFLE_ITEM,1,"Shuffle");
		item2.setIcon(android.R.drawable.ic_menu_directions);
		MenuItem item3 = menu.add(Menu.NONE,REPEAT_ITEM,2,"Repeat");
		item3.setIcon(android.R.drawable.ic_menu_revert);
		MenuItem item4 = menu.add(Menu.NONE,EXIT_ITEM,3,"Exit");
		item4.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item){
    	try {
	    	switch (item.getItemId()) {
	    	case SETTINGS_ITEM:
	    		displaySettings();
	    		break;
	    	case SHUFFLE_ITEM:
	    		displayText(protocol.getShuffleMode());
	    		break;
	    	case REPEAT_ITEM:
	    		displayText(protocol.getRepeatMode());
				break;
	    	case EXIT_ITEM:
	    		Intent response = new Intent();
	    		setResult(RESULT_OK,response);
	    		finish();
	    		break;
	    	}
    	} catch (Exception e) {
    		ErrorHandler.handleError(this, e, R.string.error_serverconnection);
    	}
    		
		return true;
	}
        
    private void displayText(ResourceItem mode) {
		Toast.makeText(main.this,mode.getResourceId(),Toast.LENGTH_SHORT).show();
	}

	@Override
    public void onDestroy() {
    	super.onDestroy();
    	protocol.stop();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
       	super.onActivityResult(requestCode, resultCode, data);
       	if (requestCode == SETTINGS_REQUEST_CODE) {
	    	if (resultCode==RESULT_OK) {
	    		Bundle extras = data.getExtras();
	    		protocol.initialize(extras.getString("ip"), extras.getInt("port"));
	    	}
       	}
       	else if(requestCode==1) { //FIXME: Where is this code used?
       		if (resultCode != RESULT_OK){
       			Toast.makeText(main.this,"Something went wrong, please try resynching.",Toast.LENGTH_LONG).show();
       		}
       	}
       	else if (requestCode == BROWSE_REQUEST_CODE){
       		if(resultCode==RESULT_OK){
       			Bundle extras = data.getExtras();
	    		String uri = extras.getString("Uri");
	    		protocol.play(uri);
       		}
       	}
    }
    
    public void displaySettings() {
    	Intent i = new Intent(main.this,Settings.class);
    	startActivityForResult(i, SETTINGS_REQUEST_CODE);
    }
        
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
            switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                	protocol.volumeUp();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                	protocol.volumeDown();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
            }
        }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup(R.layout.main);
        Bundle extras = getIntent().getExtras();
        //FIXME: Determine how to use previous instance values.
        
        protocol.initialize(extras.getString("ip"), extras.getInt("port"));
    }
    
    private void setup(int content){
        setContentView(content);
        protocol = new RemoteProtocol(this, update);
        
        this.prev = (ImageButton)this.findViewById(R.id.prev);
        this.playpause = (ImageButton)this.findViewById(R.id.playpause);
        this.next = (ImageButton)this.findViewById(R.id.next);
        //this.mute = (ImageButton)this.findViewById(R.id.mute);
        this.track = (TextView)this.findViewById(R.id.track);
        this.artist = (TextView)this.findViewById(R.id.artist);
        this.album = (TextView)this.findViewById(R.id.album);
        this.seekposition = (TextView)this.findViewById(R.id.seek_position);
        this.seektotal = (TextView)this.findViewById(R.id.seek_total);
        this.artists = (ImageButton)this.findViewById(R.id.artists);
        this.albums = (ImageButton)this.findViewById(R.id.albums);
        this.songs = (ImageButton)this.findViewById(R.id.songs);
        this.seekbar = (SeekBar)this.findViewById(R.id.seekbar);
        this.cover = (ImageView)this.findViewById(R.id.cover);
        this.tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        
        this.phoneListener = new PhoneStateListener(){
         public void onCallStateChanged(int state, String incomingNumber){
        	 if(state==TelephonyManager.CALL_STATE_RINGING){
        		 protocol.pause();
        	 }
         }
        };
        this.tm.listen(this.phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
        this.prev.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				protocol.prev();
			}
        });

        this.playpause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				protocol.playPause();
			}
        });
        
        this.next.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		protocol.next();
			}
        });
        
        this.artists.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(main.this,ArtistBrowse.class);
            	startActivityForResult(i, BROWSE_REQUEST_CODE);
			}
        });
        
        this.albums.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(main.this,AlbumBrowse.class);
            	startActivityForResult(i, BROWSE_REQUEST_CODE);
			}
        });
        
        this.songs.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent i = new Intent(main.this,TrackBrowse.class);
            	startActivityForResult(i, BROWSE_REQUEST_CODE);
			}
        });
        
        this.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				if (fromTouch) {
					protocol.seek(progress);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
        });
        
    }
    
	

}