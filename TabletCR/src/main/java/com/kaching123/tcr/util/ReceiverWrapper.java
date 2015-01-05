package com.kaching123.tcr.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public abstract class ReceiverWrapper extends BroadcastReceiver {
	
	private IntentFilter intentFilter;
	
	public ReceiverWrapper(IntentFilter intentFilter){
		this.intentFilter = intentFilter;
	}

	boolean registered;

	public void register(Context context) {
		if (registered)
			return;
		registered = true;
		LocalBroadcastManager.getInstance(context).registerReceiver(this, intentFilter);
	}

	public void unregister(Context context) {
		if (!registered)
			return;
		registered = false;
		LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
	}
}
