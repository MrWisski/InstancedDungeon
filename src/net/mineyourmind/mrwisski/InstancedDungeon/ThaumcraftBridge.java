package net.mineyourmind.mrwisski.InstancedDungeon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.ReflectUtil;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

public class ThaumcraftBridge {
	static Random r = new Random();
	
	private static ArrayList<Method> getBossSpawns(Object o){
		ArrayList<Method> ret = new ArrayList<Method>();
		int c = Config.thaumbosses.length;
		if(c == 0){
			Log.error("Config error - No boss spawn rooms configured!");
			return null;
		}
		if(!o.getClass().getName().equalsIgnoreCase(Config.thaumlockclass)){
			Log.error("We are expecting class of type '"+Config.thaumlockclass+"' but were handed a class of type '"+o.getClass().getName()+"'!");
			return null;
		}
		Class<?>[] params = new Class<?>[3];
		params[0] = params[1] = params[2] = int.class;

		for(int i = 0; i < c; i++){
			try {
				if(!ret.add(o.getClass().getDeclaredMethod(Config.thaumbosses[i], params))){
					Log.error("Failed to find a method named " + Config.thaumbosses[i] + " in " + Config.thaumlockclass);
				}
				
			} catch (NoSuchMethodException | SecurityException e) {
				Log.error("Failed to find a method named " + Config.thaumbosses[i] + " in " + Config.thaumlockclass);
			}
			
		}

		return ret;

	}

	@SuppressWarnings("deprecation")
	public static boolean handleEldritchLock(Block b, Player player){

		//We need to get the raw tile entity object from the underlying craftbukkit implementation.
		Object o = ReflectUtil.getRawTileEntityAt(b.getWorld(), b.getX(), b.getY(), b.getZ());
		if(o == null){
			Log.error("Couldn't find tileentity object for block " + Material.getMaterial(b.getTypeId()).toString());
			return false;
		}
		Method getFacing = null;
		try {
			getFacing = o.getClass().getMethod("getFacing");
		} catch (NoSuchMethodException | SecurityException e1) {
			Log.error("Couldn't find method getFacing()!");
			e1.printStackTrace();
			return false;
		}
		byte facing = 0;
		try {
			facing = (byte) getFacing.invoke(o);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			Log.error("Error invoking getFacing!");
			e1.printStackTrace();
			return false;
		}
		
		ArrayList<Method> bosses = ThaumcraftBridge.getBossSpawns(o);
		if(bosses == null){
			Log.error("We don't have any thaumcraft boss spawnrooms configured, or there was an error integrating with Thaumcraft.");
			return false;
		}
		int v = r.nextInt(bosses.size());
		Log.debug("Random value = " + v);
		Method m = bosses.get(v);
		int cx = b.getLocation().getBlockX();
		int cz = b.getLocation().getBlockZ();
		Log.debug("x = " + cx);
		Log.debug("z = " + cz);
		cx = ((int)Math.floor(cx / 16))-1;
		cz = ((int)Math.floor(cz / 16))-1;
		Log.debug("cx = " + cx);
		Log.debug("cz = " + cz);

	
		switch(facing){
		case 2: // +Z
//			cz++;
			break;
		case 3: // -Z
//			cz--;
			break;
		case 4: // +X
//			cx++;
			break;
		case 5: // -X
			//if(cx < 0){cx -= 2;}
			//if(cz < 0){cz -= 2;}
			break;
		}
		
		if(m != null){ //If the thaumcraft classes change, one, or all, of our target methods might be null.
			Log.debug("Attempting to invoke " + m.getName() + "(" + cx + ", " + cz + ", " + 2 + ")");

			try {
				m.setAccessible(true);
				try {
					m.invoke(o, cx,cz,2 );
					return true;
				} catch (IllegalAccessException e) {
					Log.error("Exception trying to invoke " + m.getName() + " - Can't access!");
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					Log.error("Exception trying to invoke " + m.getName() + " - Did thaumcraft update?");
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					Log.error("Exception trying to invoke " + m.getName());
					e.printStackTrace();
				}

			} catch (SecurityException e) {
				Log.error("Exception trying to set access for " + m.getName());
				e.printStackTrace();
			}
		} else {
			Log.error("Spawn method at index "+v+" doesn't exist! I'm not entirely sure how this happened - but it just did!");
			return false;
		}
		//Log.debug("Dumping info for tileentity!");
		//ReflectUtil.logObjInfo(o);
		return false;
	}

}