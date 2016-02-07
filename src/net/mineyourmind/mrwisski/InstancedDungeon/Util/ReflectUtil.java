package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.block.Block;

/** Class to handle reflection, provides access to underlying craftbukkit things, and
 * logging functions for Methods, Classes, Fields, Objects, etc.
 * 
 * @author MrWisski
 *
 */
public class ReflectUtil {
	
	public static Object getRawTileEntityAt(World w, int x, int y, int z){
		Method gtea = null;
		for(Method m : w.getClass().getMethods()){
			if(m.getName().equalsIgnoreCase("getTileEntityAt")){
				Log.debug("Found getTileEntityAt!");
				gtea = m;
			} 
		}
		
		Object v = null;
		try {
			v = gtea.invoke(w, x,y,z);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.error("Exception invoking getTileEntityAt :(");
			return null;
		}
		if(v == null){
			Log.error("No tile entity there! :(");
			return null;
		} else {
			Log.debug("Found something...hopefully its what we're looking for!");
			return v;
		}
	}

	public static Object getNMSBlock(Block b){
		for(Method m : b.getClass().getMethods()){
			if(m.getName().equalsIgnoreCase("getnmsblock")){
				Log.debug("Found GetNMSBlock!");
				
				Object x = null;
				try {
					x = m.invoke(b);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					Log.error("Failed reflection!");
					x = null;
				}
				
				return x;
				
			} 
		}
		Log.debug("Couldn't find getNMSBlock");
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static void dumpMethods(Object o){
		String s = "";
		Log.debug("[][][][][][][] Dumping Method info for object of type : " + o.getClass().getName());
		for(Method m : o.getClass().getMethods()){
			s += m.getName() + "(";
			for(Class c : m.getParameterTypes()){
				s += c.getSimpleName() + ",";
			}
			s += ") Returns : " + m.getReturnType().getSimpleName() + "\n";
		}		
		Log.debug(s);
	}
	
	@SuppressWarnings("rawtypes")
	public static void dumpMethods(Class o){
		String s = "";
		Log.debug("[][][][][][][] Dumping Method info for Class of type : " + o.getName());
		for(Method m : o.getDeclaredMethods()){
			s += m.getName() + "(";
			for(Class c : m.getParameterTypes()){
				s += c.getSimpleName() + ",";
			}
			s += ") Returns : " + m.getReturnType().getSimpleName() + "\n";
		}		
		Log.debug(s);
	}
	
	public static void dumpFields(Object o){
		String s = "";
		Log.debug("[][][][][][][] Dumping Field info for object of type : " + o.getClass().getName());
		for(Field f : o.getClass().getFields()){
			s += f.getName() + " ("+f.getType().getName()+")\n";
		}
		Log.debug(s);
	}
	
	@SuppressWarnings("rawtypes")
	public static void dumpFields(Class o){
		String s = "";
		Log.debug("[][][][][][][] Dumping Field info for Class of type : " + o.getName());
		for(Field f : o.getFields()){
			s += f.getName() + " ("+f.getType().getName()+")\n";
		}
		Log.debug(s);
	}
	
	@SuppressWarnings("rawtypes")
	public static void dumpInterfaces(Object o){
		Log.debug("[][][][][][][] Dumping Interface info for object of type : " + o.getClass().getName());
		for(Class c : o.getClass().getInterfaces()){
			logObjInfo(c);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void dumpInterfaces(Class o){
		Log.debug("[][][][][][][] Dumping Interface info for object of type : " + o.getName());
		for(Class c : o.getClass().getInterfaces()){
			logObjInfo(c);
		}
	}
	
	public static void logObjInfo(Object o){
		if(o == null){Log.debug("Nothing to report on a NULL object."); return;}
		Log.debug("[][][][] Dumping info for object of type : " + o.getClass().getName());
		dumpMethods(o);
		dumpFields(o);
		dumpInterfaces(o);

	}
	
	@SuppressWarnings("rawtypes")
	public static void logObjInfo(Class o){
		if(o == null){Log.debug("Nothing to report on a NULL object."); return;}
		Log.debug("[][][][] Dumping info for object of type : " + o.getName());
		dumpMethods(o);
		dumpFields(o);
		dumpInterfaces(o);
		
	}
}
