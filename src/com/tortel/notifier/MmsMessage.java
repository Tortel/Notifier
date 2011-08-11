package com.tortel.notifier;

import android.content.Context;

public class MmsMessage {
	public Context context;
	public long messageId;
	public long threadId;
	public String subject;
	public Contact sender;
	
	public MmsMessage(Context context, long messageId, long threadId, String subject, Contact sender){
		this.context = context;
		this.messageId = messageId;
		this.threadId = threadId;
		this.subject = subject;
		this.sender = sender;
	}
}
