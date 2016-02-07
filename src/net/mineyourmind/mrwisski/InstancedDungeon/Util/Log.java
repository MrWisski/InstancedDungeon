package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;

public class Log {
	public static enum MsgType {
		DEBUG,
		INFO,
		WARNING,
		SEVERE
	}
	
	private static void writeLog(MsgType t, String msg){
		if(msg == null || t == null){return;}
		switch(t){
		case DEBUG:
			if(Config.debug){
				InstancedDungeon.instance.getLogger().info("[DEBUG] " + msg);
			}
			break;
		case INFO:
			InstancedDungeon.instance.getLogger().info(msg);
			break;
		case WARNING:
			InstancedDungeon.instance.getLogger().warning(msg);
			break;
		case SEVERE:
			InstancedDungeon.instance.getLogger().severe("[ERROR] " + msg);
		}
	}
	
	public static void debug(String msg){
		Log.writeLog(MsgType.DEBUG, msg);
	}
	
	public static void info(String msg){
		Log.writeLog(MsgType.INFO, msg);
	}
	
	public static void warning(String msg){
		Log.writeLog(MsgType.WARNING, msg);
	}
	
	public static void error(String msg){
		Log.writeLog(MsgType.SEVERE, msg);
	}
	
	public static void severe(String msg){
		Log.writeLog(MsgType.SEVERE, msg);
	}
}
