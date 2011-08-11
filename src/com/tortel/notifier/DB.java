package com.tortel.notifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DB {
	private SQLiteDatabase db;
	private final Context context;
	private final DBHelper dbhelper;
	
	/**
	 * Constructor
	 * @param c Context
	 */
	public DB(Context c){
		context = c;
		dbhelper = new DBHelper(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
	}
	
	/**
	 * Close the database
	 */
	public void close(){
		db.close();
	}
	
	/**
	 * Opens the database connection
	 * @throws SQLiteException oh shit!
	 */
	public void open() throws SQLiteException {
		try{
			db = dbhelper.getWritableDatabase();
		}catch(SQLiteException ex)
		{
			Log.v("Open exception caught"+ex.getMessage());
			db = dbhelper.getReadableDatabase();
		}
	}
	
	/**
	 * Deletes a contact from the database with the given name.
	 * If nto found, no action is taken
	 * @param name the contact name
	 */
	public void deleteContact(String name){
		if(name == null || name.equals(""))
			return;
		//int id = getContactID(name);
		//db.delete(DBConstants.TABLE_NAME_NUMBERS, DBConstants.CONTACT_ID+"=\""+id+"\"", null);
		db.delete(DBConstants.TABLE_NAME_NAMES, DBConstants.CONTACT_NAME+"=\""+name+"\"", null);
	}
	
	/**
	 * Inserts a contact into the database
	 * @param name contact name
	 * @param number contact number
	 * @param icon contact icon
	 * @return -1 if bad things happen
	 */
	public long insertContact(String name, int icon){
		if(name == null || name.equals(""))
			return -1;
		//TODO: Dont allow duplicates!
		try{
			ContentValues newContact = new ContentValues();
			newContact.put(DBConstants.CONTACT_NAME, name);
			newContact.put(DBConstants.CONTACT_ICON, icon);
			return db.insert(DBConstants.TABLE_NAME_NAMES, null, newContact);
		}catch(SQLiteException ex)
		{
			Log.v("open exception caught"+ex.getMessage());
			return -1;
		}	
	}
	
	/**
	 * Gets a contact from the database. If the contact is found, the threadId
	 * returned is -1
	 * @param number the number to lookup
	 * @return Contact object if found, null if not
	 */
	public int getContactIcon(String name){
		try {
			Cursor c = db.query(DBConstants.TABLE_NAME_NAMES,
					new String[] { DBConstants.CONTACT_ICON },
					DBConstants.CONTACT_NAME + " = '" + name + "'", null, null,
					null, null);
			if (c.moveToNext()) {
				int temp = c.getInt(0);
				Log.v("Looked up icon for " + name + ", got " + temp);
				c.close();
				return temp;
			}
			c.close();
		} catch (Exception e) {
			Log.v("Exception cought while gettting icon: "+e);
		}
		return -1;
	}
	
	/**
	 * Returns a cursor for all contacts
	 * @return memos
	 */
	public Cursor getContacts(){
		Cursor c =  db.query(DBConstants.TABLE_NAME_NAMES, null, null, null, null, null, null);
		return c;
	}
}
