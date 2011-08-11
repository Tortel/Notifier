package com.tortel.notifier;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
//import android.database.Cursor;
//import android.net.Uri;
import android.os.Bundle;
//import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
//import android.provider.ContactsContract.PhoneLookup;
//import android.provider.ContactsContract.CommonDataKinds.Phone;
//import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
//import android.widget.TableRow;
//import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Tutorial here:
 * http://mobile.tutsplus.com/tutorials/android/android-essentials-using-the-contact-picker/
 * @author Scott Warner
 *
 */
public class EditContact extends Activity {
	private static final int PICK_RESULT = 1001;
	private String name;
	private int icon;
	
	private Button contactInfo;
	private Spinner spinner;
	
	/**
	 * OnCreate
	 */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editcontact);      
        
        //Set up stuff
		contactInfo = (Button)findViewById(R.id.chooseContact);
		ButtonListener tmp = new ButtonListener();
		contactInfo.setOnClickListener(tmp);
		Button save = (Button)findViewById(R.id.saveButton);
		save.setOnClickListener(tmp);
		Button delete = (Button)findViewById(R.id.deleteButton);
		delete.setOnClickListener(tmp);
		
		spinner = (Spinner) findViewById(R.id.chooseIcon);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.colors, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new SpinnerListener());
	    spinner.setPrompt("Choose a color");
	    
	    //Get data (If its there)
	    Intent intent = getIntent();
	    if(intent != null){
		    icon = intent.getIntExtra("icon", 0);
		    name = intent.getStringExtra("name");
		    if(name == null)
		    	name = "";
		    //If theres data, enable everything
		    Log.v("EditContact onCreate with extra data: "+name+" "+icon);
		    if(name != null && !name.equals("")){
		    	contactInfo.setText(name);
		    	spinner.setSelection(icon);
		    }
	    }
	    
    }
    
    
    /**
     * Restore the data
     */
    public void onRestoreInstanceState(Bundle savedInstance){
    	name = savedInstance.getString("name");
    	icon = savedInstance.getInt("icon");
    	
    	if(name == null)
    		return;
    	if(name.equals(""))
    		return;
    	contactInfo.setText(name);
    	spinner.setSelection(icon);
    }
    
    /**
     * When the application resumes from pause
     */
    public void onResume(){
    	super.onResume();
    }
    
    /**
     * Save the data
     */
    protected void onSaveInstanceState(Bundle outState){
    	outState.putString("name", name);
    	outState.putInt("icon", icon);
    }
    
    public void onDestroy(){
    	super.onDestroy();
    }
    
    private void saveContact(){
    	Log.v("Attempting to save. name: "+name+" icon: "+icon);
    	if(name == null)
    		return;
    	if(!name.equals("") ){
	    	DB db = new DB(this);
	    	db.open();
	    	if(icon < 0)
	    		icon = 0;
	    	db.deleteContact(name);
	    	db.insertContact(name, icon);
	    	db.close();
    	}
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (resultCode == RESULT_OK) {  
            switch (requestCode) {  
            case PICK_RESULT: 
            	Bundle extras = data.getExtras();  
            	Set<String> keys = extras.keySet();  
            	Iterator<String> iterate = keys.iterator();  
            	while (iterate.hasNext()) {  
            	    String key = iterate.next();  
            	    Log.v(key + "[" + extras.get(key) + "]");
            	    name = extras.getString(key);
            	}  
            	/*
            	Uri result = data.getData();  
            	Log.v("Got a result: "  
            	    + result.toString());
            	String id = result.getLastPathSegment();  
            	

                Cursor cursor = getBaseContext().getContentResolver().query(Phone.CONTENT_URI, null, Phone.LOOKUP_KEY + " = ?", new String[] { id }, null);
                try {
                    while (cursor.moveToNext()) {
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                        Log.v("Number: "+phoneNumber);
                        numbers.add(phoneNumber);
                        //int type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
                        //CharSequence phoneLabel = Phone.getTypeLabel(getResources(), type, "Undefined");
                        // boolean isPrimary = (cursor.getInt(cursor.getColumnIndex(Phone.IS_PRIMARY)) == 1);
                    }
                } finally {
                    cursor.close();
                }
                
            	Toast.makeText(this, "Id: "+id, 80000).show();*/
                break;  
            }  
      
        } else {  
            // gracefully handle failure  
            Log.v("Contact Pick not ok");  
        }
        contactInfo.setText(name);
    }  
    
    /**
     * Button Listener class
     */
    class ButtonListener implements View.OnClickListener{
        /**
         * Button event method
         */
    	public void onClick(View but) {
    			if(but.getId() == R.id.chooseContact){
    			    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
    			            Contacts.CONTENT_URI);
    			    startActivityForResult(contactPickerIntent, PICK_RESULT);  
    			} else if(but.getId() == R.id.saveButton) {
    				saveContact();
    				Toast.makeText(EditContact.this, "Saved", 3000).show();
    				Intent temp = new Intent(EditContact.this, ContactList.class);
    				startActivity(temp);
    			} else if(but.getId() == R.id.deleteButton){
    				DB db = new DB(EditContact.this);
    				db.open();
    				db.deleteContact(name);
    				db.close();
    				name = null;
    				Intent temp = new Intent(EditContact.this, ContactList.class);
    				startActivity(temp);
    			}
    	}
    }
    
    /**
     * Listens for changes to the spinner
     */
    class SpinnerListener implements OnItemSelectedListener{

		/**
		 * Save the icon
		 */
		public void onItemSelected(AdapterView<?> adapter, View view, int position,
				long id) {
			icon = position;
		}

		/**
		 * Do nothing
		 */
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
    	
    }
	
}
