package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.io.Files;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.data.DataException;

import de.schlichtherle.io.util.Paths;
import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;

public class DungeonManager {
	//A Place to store all our instances of this dungeon.
	private static HashMap<String, DungeonData> dungeons = new HashMap<String,DungeonData>();
	public static DungeonManager instance = null;
	public static String message = "";
	public static Logger log = null;
	public static FunctionsBridge bridge = null;
	
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
	
	public static DungeonData getDungeon(String name){
		return dungeons.get(name);
	}
	
	public static void setDungeon(DungeonData dungeon){
		dungeons.put(dungeon.getName(), dungeon);
	}
	
	public static DungeonData createDungeon(String name){
		if(dungeons.containsKey(name)){
			message = "Cannot create dungeon '" + name + "' - Dungeon already exists!";
			return null;
		}
		DungeonData dungeon = new DungeonData(name);
		dungeons.put(name, dungeon);
		message = "Created new dungeon '" + name + "'!";
		return dungeon;
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
		String fp = InstancedDungeon.getInstance().getDataFolder().toString() + Config.pathToDungeons + "dungeons.csv";
		File f = new File(fp);
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
		    Collection<DungeonData> d = dungeons.values();
		    
		    for(DungeonData dat : d){
		    	String s = dat.toString();
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
	
	@SuppressWarnings("deprecation")
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
		       if(d.fromString(line)){
		    	   d.setClipboard(CuboidClipboard.loadSchematic(bridge.getWEditSchematic(d.getTemplateLoc())));
		    	   log.info("Loaded Dungeon '" + d.getName() + "'");
		    	   dungeons.put(d.getName(), d);
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
		} catch (DataException e) {
			message = Config.ecol + " Error : Couldn't load up Template Schematic!";
			e.printStackTrace();
		}
		return false;
	}
}
