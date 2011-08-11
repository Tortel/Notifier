package com.tortel.notifier;

public class Contact {
	public String name;
	public String number;
	public int icon;
	public int threadId;
	
	public Contact(String name, String number, int icon, int threadId){
		this.name = name;
		this.number = number;
		this.icon = icon;
		this.threadId = threadId;
	}
	
	public String toString(){
		String toRet = "Contact: ";
		toRet += name+" "+number+" "+threadId+" "+icon;
		return toRet;
	}
}
