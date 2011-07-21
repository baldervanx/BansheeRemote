package org.nstamato.bansheeremote;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

/**
 * The longterm goal would be to let this be compatible with http://xmms2.org/wiki/MPRIS.
 * For now it should just encapsulate all communication with the server.
 * TODO: Make this protocol fully asynchronous so that it NEVER waits for answers, just registers handlers
 * that takes care of the various responses.
 */
public class RemoteProtocol {

	private static final int POLLING_INTERVAL = 1;
		
	private final Handler update;
	private final Context context;
	private final CoverCache coverCache;
	
	public boolean continueserver = true;
	private Thread serverpoke;
	private PlayerStatus status = new PlayerStatus();
	private SocketManager socket = new SocketManager();

	public RemoteProtocol(Context context, Handler updateHandler) {
		this.context = context;
		coverCache = new CoverCache(context);
		update = updateHandler;
	}
	
	public PlayerStatus getPlayerStatus() {
		return status;
	}
	
	public void initialize(String server, int port) {
		//FIXME: Must synchronize here
		stop();
		try {
			socket.connect(server, port);
			start();
		} catch (Exception e) {
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public void play(String uri) {
		try {
			String safeUri = uri.replace('/','*');
			sendCommand("play",safeUri);
		} catch (Exception e) {
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
			//Toast.makeText(main.this,"Something went wrong enqueuing.",Toast.LENGTH_LONG).show();
		}
	}
	
	public void pause() {
		 if (status.getState() == PlayerState.PLAYING) {
			try {
				 sendCommand("playPause",null);
			} catch(Exception e){
				ErrorHandler.handleError(context, e, R.string.error_serverconnection);
				//Toast.makeText(main.this,"Could not pause for incoming call",Toast.LENGTH_SHORT).show();
			}
		 }
	}
	
	public void playPause() {
		try{
			sendCommand("playPause",null);
			update.sendEmptyMessage(PlayerStatus.FULL_UPDATE);
		} catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}	
	}
	
	public void prev() {
		try {
			sendCommand("prev",null);
		} catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public void next() {
		try {
			sendCommand("next",null);
		} catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public void volumeUp() {
    	try{
			sendCommand("volumeUp",null);
		}
		catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public void volumeDown() {
    	try{
			sendCommand("volumeDown",null);
		}
		catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public void seek(int progress) {
		try {
			sendCommand("seek",Integer.toString(progress));
			status.setSeekPosition(progress);
			update.sendEmptyMessage(PlayerStatus.PARTIAL_UPDATE);
		} catch(Exception e){
			ErrorHandler.handleError(context, e, R.string.error_serverconnection);
		}
	}
	
	public ShuffleMode getShuffleMode() throws Exception {
		String shuffleText = getInfo("shuffle",null);
		if (shuffleText.equals("song")) { //FIXME: Temporary workaround
			shuffleText = "track";
		}
		return ShuffleMode.valueOf(shuffleText.toUpperCase());
	}
	
	public RepeatMode getRepeatMode() throws Exception {
		String repeatText = getInfo("repeat",null);
		return RepeatMode.valueOf(repeatText.toUpperCase());
	}
	
    private void sendCommand(String action, String params) throws IOException {
    	getInfo(action, params);
    }
    
    private String getInfo(String action, String params) throws IOException {
    	String data=null;
    	String formattedAction = action+'/'+params;
		try {
			socket.write(formattedAction);
			data = socket.read();
		} finally {
			socket.done();
		}
		return data;
    }
        
    public Bitmap getCover() {
    	Bitmap cover = coverCache.getCover(status);
    	if (cover == null) {
	    	String command="coverImage/";
	    	try {
	    		socket.write(command);
				cover = BitmapFactory.decodeStream(socket.getInputStream());
				coverCache.putCover(status, cover);
			} catch (IOException e) {
				ErrorHandler.handleErrorQuiet(e);
			} finally {
				socket.done();
			}
    	}
    	return cover;
    }
	
    private PlayerState getRemotePlayerState() throws IOException {
    	String stateStr = getInfo("status",null);
    	return toPlayerState(stateStr);
    }
        
    private void updatePlayerStatus() throws IOException {
    	String everything = getInfo("all",null);
    	if (everything!=null) {
    		String resultArray[] = new String[7];
    		resultArray = everything.split("/");
    		try {
    			status.setState(toPlayerState(resultArray[0]));
    			status.setAlbum(resultArray[1]);
    			status.setArtist(resultArray[2]);
    			status.setTrack(resultArray[3]);
    			status.setSeekPosition(Integer.parseInt(resultArray[4]));
    			status.setSeekTotal(Integer.parseInt(resultArray[5]));
    			status.setHasCover(Boolean.parseBoolean(resultArray[6]));
    		} catch(NumberFormatException e) {
    			ErrorHandler.handleErrorQuiet(e);
    		}
    	}
    }
    
	private PlayerState toPlayerState(String state) {
		if (state != null && state.length() > 0) {
			return PlayerState.valueOf(state.toUpperCase());
		} else {
			return PlayerState.NOTREADY;
		}
	}

	public void stop() {
    	continueserver = false;
    	if (serverpoke != null) {
    		serverpoke.interrupt();
    		serverpoke = null;
    	}
	}
	
	public void start() {
		continueserver = true;
		if (serverpoke == null || !serverpoke.isAlive()) {
			serverpoke = new ServerPollingThread();
			serverpoke.start();
		}
	}
		
	private class ServerPollingThread extends Thread {
		public void run() {
			while (continueserver) {
				try {
					if (status.getState() == PlayerState.PLAYING) {
						PlayerStatus prevStatus = (PlayerStatus)status.clone();
						updatePlayerStatus();
						int neededUpdate = status.getRequiredUpdate(prevStatus);
						update.sendEmptyMessage(neededUpdate);
					} else {
						PlayerState prevState = status.getState();
						PlayerState newState = getRemotePlayerState();
						if (prevState != newState) {
							updatePlayerStatus();
							update.sendEmptyMessage(PlayerStatus.FULL_UPDATE);
						}
					}
				} catch(UnknownHostException e) {
					socket.disconnect();
					RemoteProtocol.this.stop();
					ErrorHandler.handleError(context, e, R.string.error_serverconnection);
				} catch(SocketException e) {
					socket.disconnect();
					RemoteProtocol.this.stop();
					ErrorHandler.handleError(context, e, R.string.error_serverconnection);
				} catch (Exception e) {
					ErrorHandler.handleErrorQuiet(e);
				}
				
				// then sleep until next round
				try {			
					Thread.sleep(1000 * POLLING_INTERVAL);
				} catch (InterruptedException e) {}
			}
		}
	}
}
