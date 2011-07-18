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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NewServer extends Activity {
    String serverData = null; 
    String savedServers[];
    boolean serversExist=false;
	final String filename = "bansheeServers.dat";
	
    public void writeSettings(Context context, String filename, String data){
    	FileOutputStream fOut = null;
        OutputStreamWriter osw = null;
       
        try{
         fOut = context.openFileOutput(filename,Context.MODE_APPEND);      
            osw = new OutputStreamWriter(fOut);
            osw.write(data+';');
            osw.flush();
            }
            catch (Exception e) {  
            e.printStackTrace();
            }
            finally {
               try {
                      osw.close();
                      fOut.close();
                      } catch (Exception e) {
                      e.printStackTrace();
                      }
            }
       }
    public String readSettings(Context context, String filename){
        FileInputStream fIn = null;
        InputStreamReader isr = null;
       
        char[] inputBuffer = new char[255];
        String data = null;
       
        try{
         fIn = openFileInput(filename);      
            isr = new InputStreamReader(fIn);
            isr.read(inputBuffer);
            data = new String(inputBuffer);
            }
            catch (Exception e) {      
            e.printStackTrace();
            }
            finally {
               try {
                      isr.close();
                      fIn.close();
                      } catch (Exception e) {
                      e.printStackTrace();
                      }
            }
            return data;
       } 
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup();
    }
    public void setup(){
    	setContentView(R.layout.init);
        Button data = (Button) findViewById(R.id.submit);
        serverData = readSettings(NewServer.this,filename);
    	if(serverData!=null){
    		serversExist=true;
    		savedServers = serverData.split(";");
    	}
        data.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				EditText iptext = (EditText)findViewById(R.id.ip);
		        EditText porttext = (EditText)findViewById(R.id.port);
				String ip = iptext.getText().toString();
				Socket s;
				OutputStream os;
				String command = "test/";
				boolean canConnect = false;
				boolean isReachable = false;
				int port=Integer.parseInt(porttext.getText().toString());
				
				try{
	    			InetAddress address = InetAddress.getByName(ip);
	            	isReachable = address.isReachable(3000);
	    			if(isReachable){
	    				try {
	    					s = new Socket(ip,port); 
	    					os = s.getOutputStream();
	    					os.write(command.getBytes(), 0, command.length());
	    					s.close();
	    					os.close();
	    					canConnect = true;
	    				} catch(Exception e) {
	    					canConnect = false;
	    					Toast.makeText(NewServer.this,"Can't connect to Server. Check your settings.",Toast.LENGTH_SHORT).show();
	    				}
	    			}
	    			else{
						Toast.makeText(NewServer.this,"Unreachable host.",Toast.LENGTH_SHORT).show();

	    			}
	    		}catch(Exception e){
					Toast.makeText(NewServer.this,"Can't connect to Server. Check your settings.",Toast.LENGTH_SHORT).show();

	    		}
				
				if(canConnect){
					Intent response = new Intent(NewServer.this,main.class);
					response.putExtra("ip",ip);
					response.putExtra("port", port);
					String newServer = ip+':'+porttext.getText().toString();
					boolean exists = false;
					if(serversExist){
			        	for(int i=0;i<savedServers.length-1;i++){
			        		if(newServer.equals(savedServers[i])){
			        			exists = true;

			        		}
			        	}
					
			        	if(!exists){
			        		int numberServersSaved = savedServers.length-1;
			        		if(numberServersSaved<5)
			        			writeSettings(NewServer.this,filename,newServer);
			        		else{
			        			deleteFile(filename);
			        			for(int i=1;i<numberServersSaved;i++){
			        				writeSettings(NewServer.this,filename,savedServers[i]);
			        			}
			        			writeSettings(NewServer.this,filename,newServer);
			        		}
			        	}
					}
					else{
						writeSettings(NewServer.this,filename,newServer);
					}
					startActivityForResult(response,1);
					//setResult(RESULT_OK,response);
					//finish();
				}
			}
        	
        });
    }

}

