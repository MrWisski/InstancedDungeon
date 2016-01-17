package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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

public class DungeonData {
	private boolean initialized = false;
	public Logger log = InstancedDungeon.getInstance().getLogger();
	
	public DungeonData(String name){
		this.name = name;
		initialized = true;
	}
	
	public DungeonData(){
		
	}
	
	private String name = "";
	public String getName() { return name;}
	//The original worldedit schematic
	private String templateLoc = "";
	public String getTemplateLoc(){ return templateLoc; }
	public void setTemplateLoc(String tLoc){ templateLoc = tLoc;}
	
	//The finished Dungeon Schematic!
	private String schematicLoc = "";
	
	//spawnPoint is calculated relative to the CENTERPOINT of the cuboid!
	private Vector spawnPoint = new Vector(0,0,0);
	private int spawnPitch = 0;
	private int spawnYaw = 0;
	private CuboidRegion bounds = new CuboidRegion(new Vector(0,0,0),new Vector(1,1,1));
	private Vector center = new Vector(0,0,0);
	
	private boolean isPrepped = false;
	private boolean beingEdited = false;
	private boolean isReady = false;
	
	private CuboidClipboard template = null;
	
	private DungeonInstance editInst = null;
	
	public void setClipboard(CuboidClipboard schem){
		template = schem;
		bounds = new CuboidRegion(new Vector(0,0,0),schem.getSize());
		center = schem.getSize().divide(2);
				
		log.info("Schematic Stats : ");
		log.info("H : " + schem.getHeight() + " W : " + schem.getWidth() + " L : " + schem.getLength());
		log.info("Origin X : " + schem.getOrigin().getX() + " Y : " + schem.getOrigin().getY() + " Z : " + schem.getOrigin().getZ());
		log.info("Offset X : " + schem.getOffset().getX() + " Y : " + schem.getOffset().getY() + " Y : " + schem.getOffset().getZ());	
	}
	public CuboidClipboard getClipboard(){ return template;}
	
	/**
	 * 
	 * @param 	x		X coordinates	
	 * @param 	y
	 * @param 	z
	 * @param 	yaw
	 * @param 	pitch
	 */
	public void setSpawn(float x, float y, float z, int yaw, int pitch){
		
	}
	private String vToString(Vector v){
		return v.getBlockX() + ";" + v.getBlockY() + ";" + v.getBlockZ();
	}
	
	public String toString(){
		return 	name + ";" + 
				templateLoc + ";" + schematicLoc + ";" +
				vToString(spawnPoint) + ";" +
				vToString(bounds.getMaximumPoint()) + ";" +
				vToString(bounds.getMinimumPoint()) + ";" +
				(isPrepped == true ? "true;" : "false;") + 
				(beingEdited == true ? "true;" : "false;") +
				(isReady == true ? "true;" : "false;") + 
				spawnPitch + ";" + spawnYaw + ";" +
				vToString(center);
		
		
	}
	
	public boolean fromString(String s){
		if(initialized){
			InstancedDungeon.getInstance().Log.severe("Dungeon " + name + " is already initialized, but trying to re-initialize with :" + s);
			return false;
		}
		String[] a = s.split(";");
		if(a.length != 20){
			InstancedDungeon.getInstance().Log.severe("Expected length of 20, got length of " + a.length + " for string : " + s);
			return false;
		}
		name = a[0];
		templateLoc = a[1];
		schematicLoc = a[2];
		spawnPoint = new Vector(Integer.parseInt(a[3]),Integer.parseInt(a[4]),Integer.parseInt(a[5]));
		Vector pa = new Vector(Integer.parseInt(a[6]),Integer.parseInt(a[7]),Integer.parseInt(a[8]));
		Vector pb = new Vector(Integer.parseInt(a[9]),Integer.parseInt(a[10]),Integer.parseInt(a[11]));
		bounds = new CuboidRegion(pa,pb);
		isPrepped = (a[12] == "true" ? true : false);
		beingEdited = (a[13] == "true" ? true : false);
		isReady = (a[14] == "true" ? true : false);
		spawnPitch = Integer.parseInt(a[15]);
		spawnYaw = Integer.parseInt(a[16]);
		center = new Vector(Integer.parseInt(a[17]),Integer.parseInt(a[18]),Integer.parseInt(a[19]));
		
		
		return true;
		
	}
//    private void loadArea(World world, File file,Vector origin) throws DataException, IOException, MaxChangedBlocksException{
//        EditSession es = new EditSession(new BukkitWorld(world), 999999999);
//        CuboidClipboard cc = CuboidClipboard.loadSchematic(file);
//        cc.paste(es, origin, false);
//    }
}
