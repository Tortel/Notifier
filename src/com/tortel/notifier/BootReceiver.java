package com.tortel.notifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent tmp = new Intent(context,SMSListenerService.class);
		context.startService(tmp);
		Log.v("Service Started");
	}

}
