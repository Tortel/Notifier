package com.tortel.notifier;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.database.Cursor;

/**
 * Activity to view, create, and edit contacts in the database.
 * @author Scott Warner
 *
 */
public class ContactList extends ListActivity {
	TextView selection;
	Button createNewButton;
	DB db;
	ListFiller filler;
	
	/** 
	 * Called when the activity is first created.
	 */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("enabled", true))
			startService(new Intent(this, SMSListenerService.class));
		else
			stopService(new Intent(this, SMSListenerService.class));
        db = new DB(ContactList.this);
        db.open();
        filler = new ListFiller(this);
        setListAdapter(filler);
        createNewButton = (Button) findViewById(R.id.addNew);
        createNewButton.setOnClickListener(new ButtonListener());
        registerForContextMenu(getListView());
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	db.close();
    }
    
    public void onResume(){
    	super.onResume();
	    filler = new ListFiller(this);
	    setListAdapter(filler);
    }
	
	/**
	 * Handles return from creating a new contact
	 */
    protected void onActivityResult(int reqCode, int resCode, Intent data){
    	//Regenerate list
    	setListAdapter(new ListFiller(this));
    }
    /**
     * Edit a contact
     */
	public void onListItemClick(ListView parent, View v,
			int position, long id) {
		Intent editContact = new Intent(ContactList.this, EditContact.class);
		Contact temp = (Contact) getListAdapter().getItem(position);
		editContact.putExtra("name", temp.name);
		editContact.putExtra("icon", temp.icon);
		startActivityForResult(editContact,1);
	}
	
	/**
	 * Creates the context menu
	 */
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  inflater.inflate(R.menu.context_menu, menu);
	}
	
	/**
	 * Context menu selection
	 */
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  Log.v("ID "+info.id+" selected");
	  Contact temp=null;
	  switch (item.getItemId()) {
	  case R.id.edit_contact:
		temp = filler.getItem((int) info.id);
		Intent editContact = new Intent(ContactList.this, EditContact.class);
		editContact.putExtra("name", temp.name);
		editContact.putExtra("icon", temp.icon);
		startActivityForResult(editContact,1);
	    return true;
	  case R.id.delete_contact:
		temp = filler.getItem((int) info.id);
	    DB db = new DB(this);
	    db.open();
	    db.deleteContact(temp.name);
	    db.close();
	    filler = new ListFiller(this);
	    setListAdapter(filler);
	    return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
	
	/**
	 * Creates the options menu from list_menu.xml
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.list_menu, menu);
	    return true;
	}
	
	/**
	 * Handles option menu selections
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent intent = null;
	    switch (item.getItemId()) {
		    case R.id.start_service:
		    	intent = new Intent(this, SMSListenerService.class);
			    startService(intent);
			    Toast.makeText(this, "Service Started", 4000).show();
		        return true;
		    case R.id.stop_service:
		    	intent = new Intent(this, SMSListenerService.class);
			    stopService(intent);
			    Toast.makeText(this, "Service Stopped", 4000).show();
		        return true;
		    case R.id.prefrences:
				intent = new Intent(ContactList.this,Settings.class);
				startActivity(intent);
		    	return true;
		    case R.id.new_contact:
				intent = new Intent(ContactList.this,EditContact.class);
				startActivity(intent);
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

    /**
     * Button Listener class
     */
    class ButtonListener implements View.OnClickListener{
        /**
         * Button event method
         */
    	public void onClick(View something) {
    			Intent makeNew = new Intent(ContactList.this, EditContact.class);
    			startActivity(makeNew);
    	}
    }
    
    
    
    /**
     * Fills the list of memos
     */
	class ListFiller extends BaseAdapter {
		private ArrayList<Contact> contacts;
		private LayoutInflater inflater;
		
		public ListFiller(Context context) {
			contacts = new ArrayList<Contact>();
			inflater=getLayoutInflater();
			getdata();
		}
		
		public void getdata(){
			Cursor c = db.getContacts();
            startManagingCursor(c);
            if(c.moveToFirst()){
            	do{
            		//Get the info, fill the wrapper
            		String name = c.getString(c.getColumnIndex(DBConstants.CONTACT_NAME));
            		//String number = c.getString(c.getColumnIndex(DBConstants.CONTACT_NUMBER));
            		int icon = c.getInt(c.getColumnIndex(DBConstants.CONTACT_ICON));
            		Contact temp = new Contact(name,null,icon,-1);
            		contacts.add(temp);
            	}while(c.moveToNext());
            	
            }
            contacts.trimToSize();
		}
		
		/**
		 * Actual method that fills the list
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			View row=inflater.inflate(R.layout.contactlist, parent, false);
			TextView contactName=(TextView)row.findViewById(R.id.contactName);
			ImageView contactIcon = (ImageView)row.findViewById(R.id.contactIcon);
			contactName.setText(getItem(position).name);
			contactIcon.setImageResource(Values.icons[getItem(position).icon]);
			return(row);
		}
		
		
		public int getCount() {
			return contacts.size();
		}
		
		public Contact getItem(int i) {
			return contacts.get(i % getCount());
		}
		public long getItemId(int i) {
			return i;
		}
	}
}
