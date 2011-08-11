package com.tortel.notifier;


public class Log {
	public static void v(String message){
		android.util.Log.v("SMSListenService", message);
	}
}
