package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;

public class RetVal {
	public boolean status = false;
	public ArrayList<String> message = new ArrayList<String>();
	public Object retObj = null;
	
	public RetVal(){	
	}
	
	public RetVal(boolean s, String msg){
		status = s;
		add(msg);
	}
	
	public void add(String msg){
		message.add(msg);
	}
	
	public void addAll(ArrayList<String> msgs){
		message.addAll(msgs);
	}
	
	public void Err(String msg){
		message.add(Config.ecol + msg);
	}

	public void IntErr(){
		message.add(Config.ecol + "Internal server error! Please contact an administrator!");
	}
	
	public void IntErr(String s){
		if(s != null && s != ""){
			message.add(Config.ecol + "Internal server error : '" + s + "' - Please contact an administrator!");
		} else {
			IntErr();
		}
		
	}
	
	public void fail(){
		status = false;
	}
	
	public void tru(){
		status = true;
	}
}
