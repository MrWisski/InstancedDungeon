package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.World;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.CSVable;

public class DungeonData extends CSVable{
	public static class dungeonState{
		public static final int INVALID = -1;
		public static final int NEW = 0;
		public static final int PREPPED = 1;
		public static final int EDITING = 2;
		public static final int READY = 3;
		public static final int IN_USE = 4;
		
		public static String toString(int v){
			String t = "INVALID";
			switch(v){
				case -1: t = "INVALID"; break;
				case 0: t = "NEW"; break;
				case 1: t = "PREPPED"; break;
				case 2: t = "EDITING"; break;
				case 3: t = "READY"; break;
				case 4: t = "IN USE"; break;
				default: t = "INVALID"; break;
			}		
			return t;
		}
	}
	
	public DungeonData(String name){
		this.name = name;
		this.state = dungeonState.NEW;

	}
	
	public DungeonData(){
		this.state = dungeonState.INVALID;
	}
	
	public String name = "";
	//The original worldedit schematic
	public String templateLoc = "";
	//The finished Dungeon Schematic!
	public String schematicLoc = "";
	//The state of this current dungeon
	public int state = dungeonState.INVALID;
	
	//spawnPoint is calculated relative to the CENTERPOINT of the cuboid!
	public int spawnX = 0;
	public int spawnY = 0;
	public int spawnZ = 0;
	public int spawnPitch = 0;
	public int spawnYaw = 0;
	
	private CuboidRegion bounds = new CuboidRegion(new Vector(0,0,0),new Vector(1,1,1));
	private Vector center = new Vector(0,0,0);
	
	private CuboidClipboard template = null;
	private CuboidClipboard schematic = null;
	
	private InstanceData editInst = null;
	
	public void setTemplate(CuboidClipboard schem){
		template = schem;
		if(this.state >= dungeonState.PREPPED){
			log.info("Will not set up template, data is in PREPPED or higher state!");
			return;
		}
		
		bounds = new CuboidRegion(new Vector(0,0,0),schem.getSize());
		center = schem.getSize().divide(2);
				
		log.info("Schematic Stats : ");
		log.info("H : " + schem.getHeight() + " W : " + schem.getWidth() + " L : " + schem.getLength());
		log.info("Origin X : " + schem.getOrigin().getX() + " Y : " + schem.getOrigin().getY() + " Z : " + schem.getOrigin().getZ());
		log.info("Offset X : " + schem.getOffset().getX() + " Y : " + schem.getOffset().getY() + " Y : " + schem.getOffset().getZ());	
	}
	public CuboidClipboard getTemplate(){ return template;}
	
	public void setSchematic(CuboidClipboard schem){
		schematic = schem;
		if(this.state < dungeonState.PREPPED){
			log.info("Will not set up schematic, data is not past PREPPED state.");
			return;
		}
		
		bounds = new CuboidRegion(new Vector(0,0,0),schem.getSize());
		center = schem.getSize().divide(2);
				
		
	}
	
	public CuboidClipboard getSchematic(){return schematic;}
	

	public void setSpawn(int x, int y, int z, int yaw, int pitch){
		this.spawnX = x;
		this.spawnY = y;
		this.spawnZ = z;
		this.spawnYaw = yaw;
		this.spawnPitch = pitch;
	}

	@Override
	public boolean synch() {
		DungeonManager.setTemplate(this);
		DungeonManager.setSchematic(this);
		
		this.synched = true;
		return true;
	}
	
	public String getStatusDisplay(){
		String t = "OFFLINE";
		t = Config.tcol + this.name + "::" + Config.bcol + dungeonState.toString(state) + " :: " +Config.tcol+ " Template : " + (template != null ? "Yes" : "No") + " :: " + Config.bcol + " Schematic : " + (schematic != null ? "Yes" : "No");
		return t;
		
	}
}
