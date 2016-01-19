package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;

public class DungeonManager {
	//A Place to store all our instances of this dungeon.
	private static HashMap<String, DungeonData> dungeons = new HashMap<String,DungeonData>();
	public static DungeonManager instance = null;
	public static Logger log = null;
	public static FunctionsBridge bridge = null;

	
	public static String message = "";
	
	public static String test(String name){
		//DungeonData d = dungeons.get(name);
		/*String s = d.toCSV();
		log.severe("RESULT : " + s);
		
		log.info("Attempting to re-instance");
		DungeonData d2 = new DungeonData();
		d2.fromCSV(s);
		*/
		
		DungeonManager.prepDungeon(name);
		
		return "done.";
		
		
	}
	
	protected DungeonManager(){
		log = InstancedDungeon.getInstance().getLogger();
		bridge = (FunctionsBridge)InstancedDungeon.getInstance();
	}
	
	public static DungeonManager getInstance(){
		if(instance == null) {
			instance = new DungeonManager();
		}
		return instance;		
	}
	
	public static boolean pasteTemplate(String name, Vector where){
		DungeonData d = DungeonManager.getDungeon(name);
		if(d == null) return false;
		
		EditSession es = new EditSession(new BukkitWorld(bridge.getIDim()), 999999999);
		File f = bridge.getWEditSchematic(d.templateLoc);
		CuboidClipboard cc;
		try {
			cc = SchematicFormat.MCEDIT.load(f);
		} catch (IOException | DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = "Internal Server Error. :(";
			return false;
		}
		
		try {
			cc.paste(es, where, true);
		} catch (MaxChangedBlocksException e) {
			// TODO Auto-generated catch block
			message = "Too many blocks! try using a smaller schematic!";
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean pasteSchematic(String name, Vector where){
		DungeonData d = DungeonManager.getDungeon(name);
		if(d == null) return false;
		
		EditSession es = new EditSession(new BukkitWorld(bridge.getIDim()), 999999999);
		
		
		File f = new File(bridge.getDataDir(),Config.pathToDungeons + d.templateLoc );
		
		CuboidClipboard cc;
		try {
			cc = SchematicFormat.MCEDIT.load(f);
		} catch (IOException | DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = "Internal Server Error. :(";
			return false;
		}
		
		try {
			cc.paste(es, where, true);
		} catch (MaxChangedBlocksException e) {
			// TODO Auto-generated catch block
			message = "Too many blocks! try using a smaller schematic!";
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static DungeonData getDungeon(String name){
		if(name == null || name == ""){
			message = Config.ecol + " Cannot create dungeon with a null name!";
			return null;			
		}
		if(!dungeons.containsKey(name)){
			message = Config.ecol + " Cannot find dungeon '" + name + "' - Did you create it?";
			return null;
		}

		return dungeons.get(name);
	}
	
	public static boolean setDungeon(DungeonData dungeon){
		if(dungeon == null){
			message = Config.ecol + " Cannot save a null dungeon!";
			return false;						
		}
		dungeons.put(dungeon.name, dungeon);
		return true;
	}
	
	public static DungeonData createDungeon(String name){
		if(name == null || name == ""){
			message = Config.ecol + " Cannot create dungeon with a null name!";
			return null;			
		}
		if(dungeons.containsKey(name)){
			message = Config.ecol + " Cannot create dungeon '" + name + "' - Dungeon already exists!";
			return null;
		}
		DungeonData dungeon = new DungeonData(name);
		setDungeon(dungeon);
		return dungeon;
	}
	
	//Reloads the clipboard selection - only use this during initial load!
	public static boolean setTemplate(DungeonData d){
		if(d == null){
			log.info("can't set template on a null dungeon!");
			return false;
		}
		//If we're not already prepared
		if(d.templateLoc != ""){
			try {
				d.setTemplate(SchematicFormat.MCEDIT.load(bridge.getWEditSchematic(d.templateLoc)));
			} catch (DataException | IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			log.warning("Tried to setTemplate on a dungeon without a template loc - This dungeon is probably in an Invalid state!");
		}
		return true;
	}
	
	public static boolean setSchematic(DungeonData d){
		if(d == null){
			log.info("can't set template on a null dungeon!");
			return false;
		}
		//If we're not already prepared
		if(d.schematicLoc != ""){
			try {
				d.setSchematic(SchematicFormat.MCEDIT.load(new File(bridge.getDataDir(),Config.pathToDungeons + d.schematicLoc )));
			} catch (DataException | IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	
	public static void printNBT(Vector Location, CompoundTag tag){
		
		String indentSpace = StringUtils.repeat(" ", 4);
		if(tag != null){
			log.info(Location.toString() + " : " + tag.getName());
		
			for(String sss : tag.getValue().keySet()){
				log.info(indentSpace + sss + " = " + tag.getValue().get(sss).getValue());
				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static boolean prepDungeon(String name){
		log.info("Preparing dungeon " + name);
		DungeonData d = getDungeon(name);
		if(d == null){
			log.info("No dungeon!");
			// let the getDungeon error message stand.
			return false;
		}
		
		if(d.state >= dungeonState.READY){
			message = Config.ecol + " This dungeon is already ready already! Please RESET to a non-READY state!";
			log.info("Dungeon is ready! can't prep!");
			return false;
		}
		//Grab our clipboard region - we'll need it shortly.
		CuboidClipboard t = d.getTemplate();
		if(t == null){
			log.info("Trying to prep a dungeon without a template set - attempting to set template!");
			DungeonManager.setTemplate(d);
			t = d.getTemplate();
			if(t == null){
				log.info("Couldn't get the template! This dungeon is likely in an INVALID state!");
				message = Config.ecol + " Error getting template schematic - Dungeon is likely invalid!";
				return false;
			}
		}
		
		log.info("BEGINNING PREP : "+t.getWidth()+","+t.getHeight()+","+t.getLength()+"!");
		t.setOffset(new Vector(0,0,0));
		t.setOrigin(new Vector(0,0,0));
		
		Material borderMat = Material.getMaterial(Config.border);
		BaseBlock border = new BaseBlock(borderMat.getId());
	
		for(float x = 0; x < t.getWidth(); x++)
			for(float y = 0; y < t.getHeight(); y++)
				for(float z = 0; z < t.getLength(); z++){
					Vector v = new Vector(x,y,z);
					BaseBlock b = t.getPoint(v);
					
					Material m = Material.getMaterial(b.getId());
					//CompoundTag tag = b.getNbtData();
					/*
					if(tag != null){
						log.info("NBT for block " + m.name() + " : ");
						printNBT(v,tag);
					}
					LocalEntity[] l = t.pasteEntities(new Vector(x,y,z));
					if(l.length != 0){
						log.info("Got entities : ");
						for(LocalEntity e : l){
							log.info(e.toString());
						}
					}
					*/
					if(x == 0 || x == t.getWidth()-1 || y == 0 || y == t.getHeight()-1 || z == 0 || z == t.getLength()-1){
						if(b.isAir()){
							t.setBlock(v, border);
						}

					}
					
				}
		
		//Save our modified template as our official schematic!
		File f = new File(bridge.getDataDir(),Config.pathToDungeons + d.templateLoc );
		try {
			SchematicFormat.MCEDIT.save(t, f);
		} catch (IOException | DataException e) {
			e.printStackTrace();
			message = Config.ecol + "Error : Failed to write Schematic! See console for more details!";
			return false;
		}
		d.schematicLoc = d.templateLoc;
		d.state = dungeonState.PREPPED;
		DungeonManager.setSchematic(d);
			
		return true;
		
	}
	
	public static Set<String> getDungeonList(){
		return dungeons.keySet();
	}
	
	public static boolean saveDungeons(){
		log.info("saveDungeons");
		if(dungeons.isEmpty()){
			log.info("No data to save - Nothing to do!");
			return true;
		}
		
		File f = new File(bridge.getDataDir(),Config.pathToDungeons + "dungeons.csv");
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		


		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
		    Collection<DungeonData> d = dungeons.values();
		    
		    for(DungeonData dat : d){
		    	String s = dat.toCSV();
		    	bw.write(s);
		    	bw.newLine();
		    	
		    }
		    return true;
		} catch (FileNotFoundException e) {
			message = "No Dungeons found!";
			
		} catch (IOException e) {
			message = Config.ecol + " Error : IOException writing data!";
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean loadDungeons(){
		String fp = InstancedDungeon.getInstance().getDataFolder().toString() + Config.pathToDungeons + "dungeons.csv";
		File f = new File(fp);
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			if(f.createNewFile()){
				//If the file doesn't exist, and we just created it, then there's really no error.
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				DungeonData d = new DungeonData();
				if(d.fromCSV(line)){
					d.synch();
					log.info("Loaded Dungeon '" + d.name + "'");
					dungeons.put(d.name, d);
				} else {
					log.severe("Failed to load dungeon : " + line);
				}
				return true;
			}
		} catch (FileNotFoundException e) {
			message = "No Dungeons found!";
			
		} catch (IOException e) {
			message = Config.ecol + " Error : IOException reading data!";
			e.printStackTrace();
		}
		return false;
	}
}
