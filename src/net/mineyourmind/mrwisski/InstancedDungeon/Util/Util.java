package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;

//Please only put server-agnostic functions here.
public class Util {
		/** Convert MS to human readable time display.
	 * 
	 * @param	timein	(long) A duration of time in Milliseconds.  
	 * @return	A string in the format of "Years:Months:Days:Hours:Minutes:Seconds"
	 * 
	 */
	public static String toHMSFormat(long timein){
		String in = DurationFormatUtils.formatDuration(timein,"y:M:d:H:m:s");
	
		String out = "";
		String[] sp = in.split(":");
		String[] t = {"Y","M","D","h","m","s"};
		
		for(int i = 0;i < sp.length;i++)
			if(Long.parseLong(sp[i]) != 0)
				out += sp[i] + t[i];
		
		if(out == "")
			out = "0s";
		return out;
	}
	
	/**Helper function to return a string from an InputStream.
	 * 
	 *  @param InputStream in - Input stream to convert to String
	 *  @return String - the contents of the input stream, as a String
	 */
	public static String getStringFromStream(InputStream in){
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        try {
			while ((line = reader.readLine()) != null) {
				
			    out.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
        return out.toString();
		
	}
	
	public static String vToStr(Vector V){
		return V.getBlockX() + "," + V.getBlockY() + "," + V.getBlockZ();
	}
	
	public static Vector regionToBlock(Vector coords){
		int blockX = (int)Math.floor((coords.getBlockX() * 16) * 32.0);
		int blockZ = (int)Math.floor((coords.getBlockZ() * 16) * 32.0);
		return new Vector(blockX,0,blockZ);
	}
	
	public static Vector blockToRegion(Vector coords){
		int regionX = (int)Math.floor((coords.getBlockX() / 16) / 32.0);
		int regionZ = (int)Math.floor((coords.getBlockZ() / 16) / 32.0);
		return new Vector(regionX,0,regionZ);
	}
	
	public static ArrayList<String> argsToList(String[] arguments){
		ArrayList<String> arg = new ArrayList<String>();
		for(String s : arguments){
			arg.add(s);
		}
		
		return arg;
	}

}
