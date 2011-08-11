package com.tortel.notifier;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * Database helper class
 * @author scott
 *
 */
public class DBHelper extends SQLiteOpenHelper {
	/**
	 * Long string for create table query
	 */
	private static final String CREATE_TABLE_NAMES="create table "+DBConstants.TABLE_NAME_NAMES+" ("+
	DBConstants.KEY_ID+" integer primary key autoincrement, "+
	DBConstants.CONTACT_NAME+" text not null, "+
	DBConstants.CONTACT_ICON+" int);";

	/**
	 * Constructor
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * Create the table
	 */
	public void onCreate(SQLiteDatabase db) {
		Log.v("open onCreate","Creating all the tables");
		try{
			db.execSQL(CREATE_TABLE_NAMES);
		}catch(SQLiteException ex)
		{
			Log.v("open exception caught",ex.getMessage());
			
		}
	}

	/**
	 * Database upgrade. Drop it all, idk what else
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("TaskDBAdapter", "Upgrading from version "+oldVersion +" to "+newVersion+", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS "+DBConstants.TABLE_NAME_NAMES);
		onCreate(db);
	}
}
