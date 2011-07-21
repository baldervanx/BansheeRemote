
using System;
using System.Net;
using System.Net.Sockets;
using System.Collections;
using System.Text;
using System.Threading;
using System.IO;

using Mono.Unix;
using System.Runtime.InteropServices;
using Banshee.Base;
using Banshee.Collection;
using Banshee.Collection.Database;
using Banshee.Sources;
using Banshee.Metadata;
using Banshee.MediaEngine;
using Banshee.Library;
using Banshee.PlaybackController;
using Banshee.ServiceStack;
using Banshee.Preferences;
using Banshee.Configuration;
using Banshee.Query;
using Banshee.Streaming;

using Hyena;
using Hyena.Data;
using Hyena.Data.Sqlite;


namespace Banshee.RemoteListener
{


	public class RemoteListenerService : IExtensionService, IDisposable
	{
		string IService.ServiceName {
			get { return "RemoteServer"; }
		}
		
		Socket bansheeServerConn;
		//Socket client;
		ushort volume = ServiceManager.PlayerEngine.Volume;
		//TcpListener myList;
		//Thread listenThread;
		byte[] socketBuffer = new byte[5000];
		
		private PreferenceBase port_pref;
		private PreferenceBase logging_pref;
		PreferenceService bansheePrefs;
		int port;
		bool isLogging;
		
		void IExtensionService.Initialize()
		{	
			InstallPreferences();
			Console.WriteLine("BansheeRemoteListener: Preferences installed");
			
			bansheePrefs["RemoteControl"]["BansheeRemote"]["remote_control_port"].ValueChanged += delegate {
				listenPort();
			};
			bansheePrefs["RemoteControl"]["BansheeRemote"]["remote_control_logging"].ValueChanged += delegate {
				listenLogging();
			};
			
			listenPort();
			listenLogging();
		}
		
		void IDisposable.Dispose()
		{
			//base.Dispose();
			UninstallPreferences();
			bansheeServerConn.Close();
			//myList.Stop();
		}
		
		public void listenPort () {
			if (bansheeServerConn != null){
				bansheeServerConn.Disconnect(false);
			}
			
			port = (int) bansheePrefs["RemoteControl"]["BansheeRemote"]["remote_control_port"].BoxedValue;
			Console.WriteLine("BansheRemoteListener will listen on port " + port.ToString());
			IPEndPoint bansheeServer_ep = new IPEndPoint (IPAddress.Any,port);
			
			bansheeServerConn = new Socket(AddressFamily.InterNetwork,SocketType.Stream,ProtocolType.Tcp);
			bansheeServerConn.Bind(bansheeServer_ep);
			bansheeServerConn.Listen(10);
			//bansheeServerConn.SendBufferSize = 5000;
			bansheeServerConn.BeginAccept(new AsyncCallback(OnIncomingConnection), bansheeServerConn);
		}
		
		public void listenLogging() {
			isLogging = (bool) bansheePrefs["RemoteControl"]["BansheeRemote"]["remote_control_logging"].BoxedValue;
		}
		
		void OnIncomingConnection (IAsyncResult ar){
			Socket client = ((Socket)ar.AsyncState).EndAccept(ar);
			//client.SendBufferSize=5000;
			client.BeginReceive(socketBuffer,0,socketBuffer.Length,SocketFlags.None,OnSocketReceive,client);
			bansheeServerConn.BeginAccept(new AsyncCallback(OnIncomingConnection), bansheeServerConn);
		}
		
		void OnSocketReceive (IAsyncResult ar) {
			Socket client = (Socket) ar.AsyncState;
			//client.SendBufferSize=10000000;
			int bytes =  client.EndReceive(ar);
			
			client.BeginReceive(socketBuffer,0,socketBuffer.Length,SocketFlags.None,OnSocketReceive,client);
			//IPAddress ipAd = IPAddress.Parse("127.0.0.1");
		//	myList=new TcpListener(IPAddress.Any,port);
		//	listenThread = new Thread(new ThreadStart(startListening));
		//	listenThread.Start();
		//}
			//private void startListening(){
			//if(myList!=null)
			//myList.Start();
			
			
			//Console.WriteLine("BansheRemoteListener will listen on port " + port.ToString());
			//while(true){
				//if(myList!=null)
			//client=myList.AcceptSocket();
			//client.Receive(socketBuffer,socketBuffer.Length,SocketFlags.None);
			string text = Encoding.UTF8.GetString(socketBuffer,0,bytes);
			string sep = "/";
			string[] remoteMessage = text.Split('/');
			string action = remoteMessage[0];
			string variable = remoteMessage[1];
			if (action.Equals("play")){
				variable = variable.Replace('*','/');
			}
			//Console.WriteLine("BansheeRemoteListener: " + action+ " " + variable);
			Banshee.Collection.TrackInfo currTrack = ServiceManager.PlayerEngine.CurrentTrack;
			string replyText = "";
			ushort currVol;
			ushort volStep = 10;
			bool replyReq = false;
			string home = Environment.GetEnvironmentVariable("HOME");
			string coverPath="";
			//string dbPath=home+"/.config/banshee-1/banshee.db";
			if(currTrack!=null && currTrack.ArtworkId!=null){
				coverPath = home + "/.cache/media-art/" + currTrack.ArtworkId.ToString() +".jpg";
			}
			
			switch (action){
			case "coverImage":			/*request*/
				byte[] coverImage = File.ReadAllBytes(coverPath);
				client.Send(coverImage);
				replyReq = true;
				break;
			
			/*case "syncCount":
				int count = System.IO.File.ReadAllBytes(dbPath).Length;
				client.Send(System.Text.Encoding.UTF8.GetBytes(count.ToString()));
				replyReq=true;
				break;*/
				
			/*case "sync":			//request
				//Console.WriteLine("About to Read the File");
				byte[] db = File.ReadAllBytes(dbPath);
				//String s = Convert.ToString(db[8152063],16).PadLeft(2,'0');
				//Console.WriteLine("Byte Read   "+s);
				byte[] chunk;
				int remainingBytes=db.Length;
				int offset=0;
				int chunksize = 1024;
				//int rep = 0;
				//String hash="";// = System.Convert.ToBase64String
				//int numPieces = (int)Math.Ceiling((double)db.Length/2048);
				//Console.WriteLine("Number of bytes "+db.Length);
				//client.Send(System.Text.Encoding.UTF8.GetBytes(db.Length.ToString()));
				while(remainingBytes>0){
					if(remainingBytes>=chunksize){
						chunk = new byte[chunksize];
						Array.Copy(db,offset,chunk,0,chunksize);
						//if(rep==0){
						//	hash = System.Convert.ToBase64String(chunk,0,chunksize);
							//Console.WriteLine("Hash is : "+hash);
							//client.Close();
							//client=myList.AcceptSocket();
							//int bytesT = client.Receive(socketBuffer,socketBuffer.Length,SocketFlags.None);
							//string textT = Encoding.UTF8.GetString(socketBuffer,0,bytesT);
							//client=myList.AcceptSocket();
							//Console.WriteLine("Received Hash is : "+textT);
								
						//}
						offset+=chunksize;
						remainingBytes=remainingBytes-chunksize;
					}
					else{
						chunk = new byte[remainingBytes];
						Array.Copy(db,offset,chunk,0,remainingBytes);
						//hash = System.Convert.ToBase64String(chunk,0,chunksize);
						remainingBytes=0;
					}
					client.Send(chunk);
					//rep++;
				}
				//client.Send(db);
				replyReq = true;
				break;*/
				
			/*case "sync":			//request
				//Console.WriteLine("About to Read the File");
				byte[] db = File.ReadAllBytes(dbPath);
				int byteLength = db.Length;
				int offset = Convert.ToInt32(variable);
				int chunksize = 1024;
				byte[] chunk;
				//if(offset+chunksize>byteLength){
				//	chunk=new byte[byteLength-offset];
				//	Array.Copy(db,offset,chunk,0,(byteLength-offset));
				//}
				//else{
					chunk = new byte[chunksize];
					Array.Copy(db,0,chunk,0,chunksize);
				//}
				client.Send(chunk);
				Console.WriteLine("Sent from offset "+variable);
				//String s = Convert.ToString(db[8152063],16).PadLeft(2,'0');
				//Console.WriteLine("Byte Read   "+s);
				
				//int remainingBytes=db.Length;
				//int offset=0;
				//int chunksize = 1024;
				//int rep = 0;
				//String hash="";// = System.Convert.ToBase64String
				//int numPieces = (int)Math.Ceiling((double)db.Length/2048);
				//Console.WriteLine("Number of bytes "+db.Length);
				//client.Send(System.Text.Encoding.UTF8.GetBytes(db.Length.ToString()));
				//while(remainingBytes>0){
				//	if(remainingBytes>=chunksize){
				//		chunk = new byte[chunksize];
				//		Array.Copy(db,offset,chunk,0,chunksize);
						//if(rep==0){
						//	hash = System.Convert.ToBase64String(chunk,0,chunksize);
							//Console.WriteLine("Hash is : "+hash);
							//client.Close();
							//client=myList.AcceptSocket();
							//int bytesT = client.Receive(socketBuffer,socketBuffer.Length,SocketFlags.None);
							//string textT = Encoding.UTF8.GetString(socketBuffer,0,bytesT);
							//client=myList.AcceptSocket();
							//Console.WriteLine("Received Hash is : "+textT);
								
						//}
				//		offset+=chunksize;
				//		remainingBytes=remainingBytes-chunksize;
				//	}
				//	else{
				//		chunk = new byte[remainingBytes];
				//		Array.Copy(db,offset,chunk,0,remainingBytes);
						//hash = System.Convert.ToBase64String(chunk,0,chunksize);
				//		remainingBytes=0;
				//	}
					//client.Send(chunk);
					//rep++;
				//}
				//client.Send(db);
				replyReq = true;
				break;*/
				
			case "coverExists":			/*request*/
				replyText = coverExists(coverPath);
				replyReq = true;
				break;
				
			case "playPause":			/*command*/
				ServiceManager.PlayerEngine.TogglePlaying ();
				replyReq = true;
				break;
				
			case "next":				/*command*/
				ServiceManager.PlaybackController.Next ();
				replyReq = true;
				break;
				
			case "prev":				/*command*/
				ServiceManager.PlaybackController.Previous ();
				replyReq = true;
				break;
				
			case "play":
				var source = ServiceManager.SourceManager.ActiveSource as ITrackModelSource;
				if (source == null) {
					source = ServiceManager.SourceManager.DefaultSource as ITrackModelSource;
				}
				if (source != null) {
					var countSongs = source.Count;
					UnknownTrackInfo track = new UnknownTrackInfo(new SafeUri(variable));
					TrackInfo trackTemp=null;
					for(int i=0;i<countSongs;i++){
						trackTemp = source.TrackModel [i];
						if(trackTemp.TrackEqual(track)){
						   break;
						}
					}
					if (trackTemp!=null) {
						ServiceManager.PlayerEngine.OpenPlay (trackTemp);
					} else {
						log("Track not found: " + variable);
					}
				} else {
					log("No source to play from.");
				}
				replyReq = true;
				break;
				
			case "volumeDown":			/*command*/
				currVol = ServiceManager.PlayerEngine.Volume;
				//ServiceManager.PlayerEngine.Open(new SafeUri("hello"));
				if (currVol < 10) {
					ServiceManager.PlayerEngine.Volume = 0;	
				}
				else {
					ServiceManager.PlayerEngine.Volume = (ushort) (currVol - volStep);	
				}
				replyReq = true;
				break;
				
			case "volumeUp":			/*command*/
				currVol = ServiceManager.PlayerEngine.Volume;
				if (currVol > 90) {
					ServiceManager.PlayerEngine.Volume = 100;
				}
				else {
					ServiceManager.PlayerEngine.Volume = (ushort) (currVol + volStep);
				}
				replyReq = true;
				break;
				
			case "mute":				/*command*/
					currVol = ServiceManager.PlayerEngine.Volume;
					if (currVol > 0) {
						volume = currVol;
						ServiceManager.PlayerEngine.Volume = 0;
				}
				else {
					ServiceManager.PlayerEngine.Volume = volume;	
				}
				replyReq = true;
				break;
			case "status":				/*request*/		
				replyText = ServiceManager.PlayerEngine.CurrentState.ToString().ToLower();
				Console.WriteLine("Status " + replyText);
				replyReq = true;
				break;
				
			case "album":				/*request*/
				replyText = currTrack.DisplayAlbumTitle;
				replyReq = true;
				break;
				
			case "artist":				/*request*/
				replyText = currTrack.DisplayArtistName;
				replyReq = true;
				break;
				
			case "title":				/*request*/
				replyText = currTrack.DisplayTrackTitle;
				replyReq = true;
				break;
			case "trackCurrentTime":	/*request*/
				replyText = (ServiceManager.PlayerEngine.Position/1000).ToString();
				replyReq = true;
				break;
				
			case "trackTotalTime":		/*request*/
				replyText = currTrack.Duration.ToString();
				replyReq = true;
				break;
				
			case "seek":				/*command*/
				ServiceManager.PlayerEngine.Position = UInt32.Parse(variable)*1000;
				replyReq = true;
				break;
				
			case "shuffle":				/*command*/ /*request*/
				if (ServiceManager.PlaybackController.ShuffleMode.ToString() == "off") {
					ServiceManager.PlaybackController.ShuffleMode = "song";
					replyText="song";
				}else if(ServiceManager.PlaybackController.ShuffleMode.ToString()=="song") {
					//ServiceManager.PlaybackController.ShuffleMode = PlaybackShuffleMode.Linear ;
					ServiceManager.PlaybackController.ShuffleMode = "artist" ;
					replyText="Artist";
				}
				else if(ServiceManager.PlaybackController.ShuffleMode.ToString()=="artist") {
					//ServiceManager.PlaybackController.ShuffleMode = PlaybackShuffleMode.Linear ;
					ServiceManager.PlaybackController.ShuffleMode = "album" ;
					replyText="Album";
				}
				else if(ServiceManager.PlaybackController.ShuffleMode.ToString()=="album") {
					//ServiceManager.PlaybackController.ShuffleMode = PlaybackShuffleMode.Linear ;
					ServiceManager.PlaybackController.ShuffleMode = "rating" ;
					replyText="Rating";
				}
				else if(ServiceManager.PlaybackController.ShuffleMode.ToString()=="rating"){
					//ServiceManager.PlaybackController.ShuffleMode = PlaybackShuffleMode.Linear ;
					ServiceManager.PlaybackController.ShuffleMode = "score" ;
					replyText="Score";
				}
				else{
					ServiceManager.PlaybackController.ShuffleMode = "off" ;
					replyText="off";
				}
				//replyText="off";
				//replyText = ServiceManager.PlaybackController.ShuffleMode.ToString();
				//Console.WriteLine(ServiceManager.PlaybackController.ShuffleMode.ToString());
				replyReq = true;
				break;
				
			case "repeat":				/*command*/ /*request*/
				if (ServiceManager.PlaybackController.RepeatMode == PlaybackRepeatMode.None){
					ServiceManager.PlaybackController.RepeatMode = PlaybackRepeatMode.RepeatAll;
					replyText="all";
				}else if (ServiceManager.PlaybackController.RepeatMode == PlaybackRepeatMode.RepeatAll){
					ServiceManager.PlaybackController.RepeatMode = PlaybackRepeatMode.RepeatSingle;
					replyText="single";
				}else {
					ServiceManager.PlaybackController.RepeatMode = PlaybackRepeatMode.None;
					replyText="off";
				}
				//replyText = ServiceManager.PlaybackController.RepeatMode.ToString();
				//Console.WriteLine(replyText);
				replyReq = true;
				break;
				
			case "all":					/*request*/
				replyText = ServiceManager.PlayerEngine.CurrentState.ToString().ToLower() + sep;
				replyText += currTrack.DisplayAlbumTitle.Replace('/','\\') + sep;
				replyText += currTrack.DisplayArtistName.Replace('/','\\') + sep;
				replyText += currTrack.DisplayTrackTitle.Replace('/','\\') + sep;
				replyText += ((uint) (ServiceManager.PlayerEngine.Position/1000)).ToString() + sep;
				replyText += ((uint) (currTrack.Duration.TotalSeconds)).ToString() + sep;
				replyText += coverExists(coverPath);
				replyReq = true;
				break;
			
			case "test":
				replyText = "";
				replyReq = true;
				break;
			
			default:
				replyText = "";
				replyReq = false;
				break;
			}

			byte[] messageByte = System.Text.Encoding.UTF8.GetBytes(replyText);
			
			if (replyReq){
				reply(client, messageByte);
				//Console.WriteLine(System.Text.Encoding.UTF8.GetString(messageByte));
			//}

			client.Close();	
		}
	}
		
		void reply (Socket remoteClient, byte[] reply) {
			//Console.WriteLine("BansheeRemoteListener: Sending reply.");
			//int name = ServiceManager.SourceManager.MusicLibrary.; //.Query<int>("SELECT PlaylistID FROM Playlists WHERE Name = ? LIMIT 1", "kvell");
		   // HyenaSqliteConnection db = ServiceManager.DbConnection;
			//HyenaSqliteCommand command = new HyenaSqliteCommand("SELECT ArtistID FROM CoreArtists WHERE NameLowered='offspring'");
			//int n=db.Query<int>(command);
			//int n = ServiceManager.DbConnection.Query<int>("SELECT PlaylistID FROM Playlists WHERE Name = ? LIMIT 1", "kvell");			
			//Console.WriteLine(n);
			remoteClient.Send(reply);
		}
		
		string coverExists(string coverPath){
			string retVal = "false";
			  if (System.IO.File.Exists(coverPath)){
				if (System.IO.File.ReadAllBytes(coverPath).Length < 110000){
					retVal = "true";
				}
			}
			//Console.WriteLine("coverPath "+coverPath);
			return retVal;
		}
	
	private void log(string message) {
			if (isLogging) {
				Console.WriteLine(message);
			}
		}

	private void InstallPreferences(){
			bansheePrefs = ServiceManager.Get<PreferenceService>();
			
			if (bansheePrefs == null){
				return;
			}
			Page remoteControlPage = new Page("RemoteControl","Remote Control",3);
			bansheePrefs.FindOrAdd(remoteControlPage);
			
			Section BansheeRemotePrefs = remoteControlPage.FindOrAdd(new Section("BansheeRemote","Banshee Remote",0));
			
			port_pref = BansheeRemotePrefs.Add (new SchemaPreference<int>(
			          RemotePortSchema,
			          Catalog.GetString("Banshee Remote port"),
			          Catalog.GetString("Banshee will listen for the Android Banshee Remote app on this port")));
			
			logging_pref = BansheeRemotePrefs.Add (new SchemaPreference<bool>(
			          LoggingSchema, 
			          Catalog.GetString("Banshee Remote Logging"),
			          Catalog.GetString("Enables or disables logging")));
		}
		
	private void UninstallPreferences(){
			Console.WriteLine("BansheeRemoteListener: UninstallPreferences() called");
			bansheePrefs["RemoteControl"]["BansheeRemote"].Remove(port_pref);
			bansheePrefs["RemoteControl"]["BansheeRemote"].Remove(logging_pref);
			bansheePrefs["RemoteControl"].Remove(bansheePrefs["RemoteControl"].FindById("BansheeRemote"));
			bansheePrefs.Remove(bansheePrefs.FindById("RemoteControl"));
		}
	
	public static readonly SchemaEntry<int> RemotePortSchema = new SchemaEntry<int> (
            "remote_control", "remote_control_port",
            8484,1024,49151,
            "BansheeRemote Port",
            "BansheeRemoteListener will listen for the BansheeRemote Android app on this port"
        );
	
	public static readonly SchemaEntry<bool> LoggingSchema = new SchemaEntry<bool> (
            "remote_control", "remote_control_logging",
            false,
            "BansheeRemote Logging",
            "Enables or disables logging"
        );		
	}
}
