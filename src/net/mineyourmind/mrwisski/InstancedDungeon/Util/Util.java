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
	
	public static boolean canAccess(Field F){
		Logger l = InstancedDungeon.instance.getLogger();
		int m = F.getModifiers();
		l.info("Checking Modifiers : '" + F.getName() + "'");
		
			return false;

	}
	
	public static String toCSV(Object o){
		Logger l = InstancedDungeon.instance.getLogger();
		String out = "";
		for (Field field : o.getClass().getDeclaredFields()) {
			l.info("Examining field " + field.getName());
			try {
				int m = field.getModifiers();
				if(Modifier.isPrivate(m) || Modifier.isProtected(m) || Modifier.isStatic(m)){
					//String so = field.getName() + ";" + field.getType().getName() + ";" + field.get(o).toString() + ";*@*;";
					//l.info("Added : " + so);
					//out += so;
					l.info("Think i can access this.");
				} else {
					l.info("Ignoring - shouldn't touch this.");
				}
			} catch (IllegalArgumentException e) {
				l.severe("Failed to serialize object!");
				e.printStackTrace();
				return null;
			}
        }
		
		return out;
	}
	
	public static void fromCSV(String s, Object to){
		String[] fstr = s.split(";*@*;");
		try{
		for(String sub : fstr){
			String[] fieldstr = sub.split(";");
			Field field = to.getClass().getDeclaredField(fieldstr[0]);
			Class<?> rClass = field.getType();
			//Don't try to de-serialize inaccessible fields.
			if(to.getClass().getDeclaredField(fieldstr[0]).isAccessible() == false){
				continue;
			}
			if (rClass == String.class) {
				field.set(to, fieldstr[3]);
			} else if (rClass == int.class) {
				field.set(to, Integer.parseInt(fieldstr[3]));
			} else if (rClass == boolean.class) {
				field.set(to, Boolean.parseBoolean(fieldstr[3]));
			} else if (rClass == float.class) {
				field.set(to, Float.parseFloat(fieldstr[3]));
			} else if (rClass == long.class) {
				field.set(to, Long.parseLong(fieldstr[3]));
			} else if (rClass == short.class) {
				field.set(to, Short.parseShort(fieldstr[3]));
			} else if (rClass == double.class) {
				field.set(to, Double.parseDouble(fieldstr[3]));
			} else if (rClass == byte.class) {
				field.set(to, Byte.parseByte(fieldstr[3]));
			} else if (rClass == char.class) {
				field.set(to, fieldstr[3].charAt(0));
			} else {
				InstancedDungeon.instance.getLogger().severe("Failed to de-serialize " + rClass.getSimpleName() + " object!");
			}
		}
		} catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			InstancedDungeon.instance.getLogger().severe("Failed to de-serialize object " + to.getClass().getName());
			to = null;
			e.printStackTrace();
			
		}
		
	}
}
