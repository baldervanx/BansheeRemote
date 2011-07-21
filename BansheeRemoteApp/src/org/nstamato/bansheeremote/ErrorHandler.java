package org.nstamato.bansheeremote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorHandler {

	private static final String TAG = "BansheeRemote";

	public static void handleError(Context context, Throwable th, int message) {
		Log.e(TAG, "Error", th);
		Toast.makeText(context, message,Toast.LENGTH_LONG).show();		
	}

	public static void handleErrorQuiet(Exception e) {
		Log.w(TAG, "Warning", e);
	}
}
