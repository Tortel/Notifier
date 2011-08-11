package com.tortel.notifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SMSNotifier extends Activity {
	Intent svc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);      
        
		try {
		    // start Service
		      svc = new Intent(this, SMSListenerService.class);
		      startService(svc);
		      //Toast.makeText(this, "Service Started", 4000).show();
		}	
		catch (Exception e) {
			Toast.makeText(this, "Exception: "+e.toString(), 4000).show();
		}
		
		Button stop = (Button)findViewById(R.id.stopButton);
		ButtonListener tmp = new ButtonListener();
		stop.setOnClickListener(tmp);
		Button start = (Button)findViewById(R.id.startButton);
		start.setOnClickListener(tmp);
		Button prefs = (Button)findViewById(R.id.prefsButton);
		prefs.setOnClickListener(tmp);
		Button entries = (Button)findViewById(R.id.viewEntriesButton);
		entries.setOnClickListener(tmp);
		
    }
    
    /**
     * Button Listener class
     */
    class ButtonListener implements View.OnClickListener{
        /**
         * Button event method
         */
    	public void onClick(View but) {
    			if(but.getId() == R.id.stopButton){
	    			stopService(svc);
	    			Toast.makeText(SMSNotifier.this, "Service Stopped", 4000).show();
    			} else if(but.getId() == R.id.startButton) {
        			startService(svc);
        			Toast.makeText(SMSNotifier.this, "Service Started", 4000).show();
    			} else if(but.getId() == R.id.viewEntriesButton){
    				Intent intent = new Intent(SMSNotifier.this,ContactList.class);
    				startActivity(intent);
    			} else {
    				Intent intent = new Intent(SMSNotifier.this,Settings.class);
    				startActivity(intent);
    			}
    	}
    }
    
}