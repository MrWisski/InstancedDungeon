package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Class to handle writing out a classes data to CSV.
 * 
 * Please note - Only works with basic data types, all others are "In Progress".
 * For now, please rebuild data inside synch() - which MUST be called after the class
 * is rebuilt.
 * 
 * @author MrWisski
 *
 */
public abstract class CSVable {
	protected static final boolean DEBUG = false;
	protected static final String DATA_SEPERATOR = ";";
	protected static final String FIELD_SEPERATOR = "~";
	protected boolean synched = false;
	
	/**synch function - Use this to synchronize/rebuild data after being reinstated from CSV.
	 * 
	 * @return True : Success! False : Failure! :(
	 */
	public abstract boolean synch();
	
	/** toCSV - Convert class to CSV.
	 * 
	 * @return a String containing an encoded (mostly) CSV value.
	 */
	public String toCSV(){

		String out = "";
	
		for (Field field : this.getClass().getFields()) {
			Log.info("Examining field : " +Modifier.toString(field.getModifiers())+ " " + field.getName());
			try {
				int m = field.getModifiers();
				if(Modifier.isPrivate(m) || Modifier.isProtected(m) || Modifier.isStatic(m)){
					Log.debug("Ignoring - shouldn't touch this.");
				} else {
					if(field.get(this) != null){
						String val = field.get(this).toString();
						String so = field.getName() + DATA_SEPERATOR + field.getType().getName() + DATA_SEPERATOR + val + FIELD_SEPERATOR;
						Log.debug("Added : " + so);

						out += so;
					} else {
						Log.debug("Field '"+field.getName()+"' is null - skipping.");
					}
					//l.info("Think i can access this.");
				}
			} catch (IllegalArgumentException | ReflectiveOperationException e) {
				Log.severe("Failed to serialize object!");
				e.printStackTrace();
				return null;
			}
        }
		
		return out;
	}
	
	public boolean fromCSV(String s){
		String temp = "";
		int c = 0;
		String[] fstr = s.split(FIELD_SEPERATOR);
		Log.debug("Big split");
		for(String split : fstr){
			temp += "["+c+"] '"+split + "'\n";
			c++;
		}
		Log.debug(temp);
		try{
			for(String sub : fstr){
				Log.debug("Analyzing string : '" + sub + "'");
				String[] fieldstr = sub.split(DATA_SEPERATOR);
				temp = "";
				c = 0;
				for(String split : fieldstr){
					temp += "["+c+"] '"+split + "'\n";
					c++;
				}
				Log.debug(temp);
				Field field = this.getClass().getDeclaredField(fieldstr[0]);
				Class<?> rClass = field.getType();
				if(fieldstr.length != 3){
					Log.debug("Field " + field.getName() + " value saved as null - skipping.");
					continue;
					
				}
				
				if (rClass == String.class) {
					field.set(this, fieldstr[2]);
				} else if (rClass == int.class) {
					field.set(this, Integer.parseInt(fieldstr[2]));
				} else if (rClass == boolean.class) {
					field.set(this, Boolean.parseBoolean(fieldstr[2]));
				} else if (rClass == float.class) {
					field.set(this, Float.parseFloat(fieldstr[2]));
				} else if (rClass == long.class) {
					field.set(this, Long.parseLong(fieldstr[2]));
				} else if (rClass == short.class) {
					field.set(this, Short.parseShort(fieldstr[2]));
				} else if (rClass == double.class) {
					field.set(this, Double.parseDouble(fieldstr[2]));
				} else if (rClass == byte.class) {
					field.set(this, Byte.parseByte(fieldstr[2]));
				} else if (rClass == char.class) {
					field.set(this, fieldstr[2].charAt(0));
				} else {
					Log.severe("Failed to de-serialize " + rClass.getSimpleName() + " object!");
					//return false;
				}
			}
		} catch(IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			Log.severe("Failed to de-serialize object " + this.getClass().getName());
			//to = null;
			e.printStackTrace();
			return false;

		}
		//We still need to re-instance some of the data.
		this.synched = false;
		return true;

	}
}
