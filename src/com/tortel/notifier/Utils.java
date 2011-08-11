package com.tortel.notifier;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
//TODO: Log shit.

/**
 * Some code borrowed from SMSPopUp (net.everythingandroid.smspopup), by Adam K (adam@everythingandroid.net)
 * @author Scott Warner
 */
public class Utils {
	/**
	 * Useful consts
	 */
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
	public static final String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
	
	public static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
	public static final Uri MMS_INBOX_CONTENT_URI = Uri.withAppendedPath(MMS_CONTENT_URI, "inbox");
	
	private static final String UNREAD_CONDITION = "read=0";
	
	/**
	 * Returns the SMS thread ID from a given phone number
	 * @param sender the phone number
	 * @param context context
	 * @return 0 if null, or the Thread ID
	 */
	public static int getThreadID(String sender, Context context){
		if(sender == null)
			return 0;
		Log.v("Getting threadID for "+sender);
		
		ContentResolver resolver = context.getContentResolver();
		int threadId = 0;
		Uri threadUri = Uri.parse("content://sms/inbox");
		//Cursor threadCursor = resolver.query(threadUri, new String[] {"thread_id", "address", "person", "date", "body" }, null, null, null);
		Cursor threadCursor = resolver.query(threadUri, new String[] {"thread_id", "address", "read" }, null, null, "read ASC");
		/**
		 * Search sms/threads for thread ID, and unread message count. Order by unread to make it faster, and break when get to any read
		 */
		int addressCol = threadCursor.getColumnIndex("address");
		
		while(threadCursor.moveToNext()){
			if(threadCursor.getString(addressCol).toString().equals(sender)){
				threadId = threadCursor.getInt(threadCursor.getColumnIndex("thread_id"));
				break;
			}
		};
		Log.v("Found thread ID: "+threadId);
		threadCursor.close();
		return threadId;
	}
	
	/**
	 * Returns the current unread SMS count
	 * @param context
	 * @return
	 */
	public static synchronized int getUnreadSmsCount(Context context, String body) {
	    final String[] projection = new String[] { "_id", "body" };
		final String selection = UNREAD_CONDITION;
		final String[] selectionArgs = null;
		final String sortOrder = "date DESC";
		
		int count = 0;

		Cursor cursor = context.getContentResolver().query(
		    SMS_INBOX_CONTENT_URI,
		    projection,
		    selection,
		    selectionArgs,
		    sortOrder);

		if (cursor != null) {
		  try {
		    count = cursor.getCount();
		    
		    //This stack of ifs is to check if the latest message was actually counted or not
		    if(body != null && count > 0)
		    	if(cursor.moveToFirst())
		    		if(!body.equals(cursor.getString(1)))
		    			count++;
		    
		    
		  } finally {
		    cursor.close();
		  }
		}

    	return count;
    }
	
	/**
	 * Return current unread MMS count
	 *
	 * @param context context
	 * @return unread mms message count
	 */
	public static synchronized int getUnreadMmsCount(Context context) {
	  final String selection = UNREAD_CONDITION;
	  final String[] projection = new String[] { "_id" };
	
	  int count = 0;
	  Cursor cursor = context.getContentResolver().query(
			  Uri.parse("content://mms/inbox"),
	      projection,
	      selection, null, null);
	
	  if (cursor != null) {
	    try {
	      count = cursor.getCount();
	    } finally {
	      cursor.close();
	    }
	  }
	  return count;
	}
	
	/**
	 * Returns the total number of unread messages, both SMS and MMS
	 * @param context
	 * @return number of unread messages
	 */
	public static synchronized int getUnreadCount(Context context){
		return getUnreadMmsCount(context) + getUnreadSmsCount(context,null);
	}
	
	/**
	 * Returns the total number of unread messages, while checking if
	 * the message with body is in the database yet
	 * @param context context
	 * @param body message to check
	 * @return number of unread messages
	 */
	public static synchronized int getUnreadCount(Context context,String body){
		return getUnreadMmsCount(context) + getUnreadSmsCount(context,body);
	}
	
	/**
	 * Returns an Intent to open to inbox
	 * @return
	 */
	public static Intent getSmsInboxIntent() {
	    Intent conversations = new Intent(Intent.ACTION_MAIN);
	    conversations.setType(SMS_MIME_TYPE);
	    int flags =
	      Intent.FLAG_ACTIVITY_NEW_TASK |
	      Intent.FLAG_ACTIVITY_SINGLE_TOP |
	      Intent.FLAG_ACTIVITY_CLEAR_TOP;
	    conversations.setFlags(flags);
	    return conversations;
	}
	
	/**
	 * Looks up a contact in the phones contact database
	 * @param number the phone number
	 * @param context context
	 * @return a contact object with a contact, or properly formatted phone number
	 */
	public static Contact lookupContact(String number, Context context){
		ContentResolver resolver = context.getContentResolver();
		String displayName = null;
		
		//Need to look up the number in contacts
		Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		//contact_id
		Cursor c = resolver.query(lookupUri, new String[] {PhoneLookup.DISPLAY_NAME}, null, null, null);
		try {
		    c.moveToFirst();
		    displayName = c.getString(0);
		} catch(Exception e){
			displayName = null;
		}finally {
		    c.close();
		}
		
		//Not found, format the number
		if(displayName == null)
			displayName = PhoneNumberUtils.formatNumber(number);
		Contact contact = new Contact(displayName,number,-1,-1);
		contact.threadId = getThreadID(number,context);
		contact.icon = Utils.getIcon(displayName,context);
		Log.v("Got icon "+contact.icon);
		if(contact.icon < 0){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			contact.icon = Values.icons[Integer.parseInt(prefs.getString("defColor", "0"))];
		} else
			contact.icon = Values.icons[contact.icon];
		Log.v(contact.toString());
		return contact;
	}
	
	public static int getIcon(String name, Context context){
		DB db = new DB(context);
		db.open();
		int icon = db.getContactIcon(name);
		db.close();
		return icon;
	}
	
	/**
	 * Borrowed from SMSPopup
	 * @param context
	 * @param ignoreThreadId
	 * @return
	 */
	  synchronized public static MmsMessage getMmsDetails(Context context, long ignoreThreadId) {
	
		    final String[] projection = new String[] { "_id", "thread_id", "date", "sub", "sub_cs" };
	    String selection = UNREAD_CONDITION;
	    String[] selectionArgs = null;
	    final String sortOrder = "date DESC";
	    int count = 0;
	
	    if (ignoreThreadId > 0) {
	      selection += " and thread_id != ?";
	      selectionArgs = new String[] { String.valueOf(ignoreThreadId) };
	    }
	
	    Cursor cursor = context.getContentResolver().query(
	        MMS_INBOX_CONTENT_URI,
	        projection,
	        selection,
	        selectionArgs,
	        sortOrder);
	
	    if (cursor != null) {
	      try {
	        count = cursor.getCount();
	        if (count > 0) {
	          cursor.moveToFirst();
	          //          String[] columns = cursor.getColumnNames();
	          //          for (int i=0; i<columns.length; i++) {
	          //            Log.v("columns " + i + ": " + columns[i] + ": "
	          //                + cursor.getString(i));
	          //          }
	          long messageId = cursor.getLong(0);
	          long threadId = cursor.getLong(1);
	          String subject = cursor.getString(3);
	
	          return new MmsMessage(context, messageId, threadId, subject, lookupContact(getMmsAddress(context,messageId), context));
	        }
	
	      } finally {
	        cursor.close();
	      }
	    }
	    return null;
	}
	  
	  /**
	   * Borrowed from SMSPopup
	   * @param context
	   * @param messageId
	   * @return
	   */
	  public static String getMmsAddress(Context context, long messageId) {
		    final String[] projection =  new String[] { "address", "contact_id", "charset", "type" };
		    final String selection = "type=137"; // "type="+ PduHeaders.FROM,

		    Uri.Builder builder = MMS_CONTENT_URI.buildUpon();
		    builder.appendPath(String.valueOf(messageId)).appendPath("addr");

		    Cursor cursor = context.getContentResolver().query(
		        builder.build(),
		        projection,
		        selection,
		        null, null);

		    if (cursor != null) {
		      try {
		        if (cursor.moveToFirst()) {
		          // Apparently contact_id is always empty in this table so we can't get it from here

		          // Just return the address
		          return cursor.getString(0);
		        }
		      } finally {
		        cursor.close();
		      }
		    }

		    return context.getString(android.R.string.unknownName);
		  }
}
