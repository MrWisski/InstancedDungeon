package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData.instanceState;

public class InstanceManager {
	public static final int REGION_SIZE = 512;
	public static final int HALF_REGION = 256;
	public static final Vector HALF_REGION_V = new Vector(256,0,256);
	public static final Vector REGION_V = new Vector(512,0,512);
	
	private static HashMap<String,InstanceData> instances = new HashMap<String, InstanceData>();
	//private static ArrayList<String> regions = new ArrayList<String>();
	public static Logger log = null;
	public static FunctionsBridge bridge = null;
	private static InstanceManager instance = null;
	public static String message = "No Message.";
	private static Vector lastInstance = new Vector(0,0,0);
	
	protected InstanceManager(){
		log = InstancedDungeon.getInstance().getLogger();
		bridge = (FunctionsBridge)InstancedDungeon.getInstance();
	}
	
	public static InstanceManager getInstance(){
		if(instance == null) {
			instance = new InstanceManager();
		}
		return instance;		
	}
	
	public static InstanceData createInstance(String owner, String dungeon, boolean edit){
		log.info("createInstance");
		if(owner == ""){
			message = Config.ecol + "Instance owner cannot be blank!";
			return null;
		}
		UUID uuid = bridge.getUUID(owner);
		if(uuid == null){
			log.info("uuid == null");
			message = Config.ecol + "Player UUID is null! Must be a real player! Must be online!";
			return null;
		} else {
			log.info("uuid == " + uuid.toString());
		}
		if(dungeon == ""){
			message = Config.ecol + "Dungeon cannot be blank!";
			return null;
		}		
		DungeonData d = DungeonManager.getDungeon(dungeon);
		
		//First, we need to do a few things.
		Vector atLoc = getNextFree();
		log.info("atLoc (Region) = " +vToStr(atLoc));
		atLoc = InstanceManager.regionToBlock(atLoc);
		log.info("atLoc = " + vToStr(atLoc) );
		String name = getRegionID(atLoc); //atLoc.getBlockX() + "_" + atLoc.getY();
		
		atLoc = HALF_REGION_V.add(atLoc);
		log.info("atLoc + halfregion = " + vToStr(atLoc));
		atLoc = atLoc.setY(64);
		log.info("atLoc Final = " + vToStr(atLoc));
		
		log.info("new InstanceData(" + name + ", " + owner + ", " + uuid.toString() + ", " + dungeon + ", " + vToStr(atLoc) + ", false");
		
		InstanceData i = new InstanceData(name, owner, uuid, dungeon, atLoc, edit);
		if(i != null){
			instances.put(name, i);
			log.info("Leaving Creation, status : " + i.getStatusDisplay());
		}
		
		return i;
			
	}
	
	public static void setInstance(InstanceData i){
		if(i == null){
			message = Config.ecol + "setInstance cannot accept null!";
			return;
		}
		instances.put(i.name, i);
	}
	
	public static InstanceData getInstance(String name){
		if(!instances.containsKey(name)){
			message = Config.ecol + "Instance '"+name+"' does not exist!";
			return null;
		} else {
			return instances.get(name);
		}
	}
	
	public static boolean mountRegion(String name){
		log.info("mountRegion");
		InstanceData i = getInstance(name);
		if(i == null){log.info("InstanceData == null! :("); return false;} // allow getInstance error message through
		
		if(i.state != instanceState.WAITING){
			message = Config.ecol + "Cannot mount an instance that is not in the WAITING state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state);
			log.severe(message);
			return false;
		} else {
			log.info("Instance Status : " + i.getStatusDisplay());
		}
		
		if(i.getDungeon().state != dungeonState.READY){
			message = Config.ecol + "Cannot mount an instance whose underlying Dungeon is not in the READY state - Dungeon '" + i.getDungeon().name + "' is in state : "+ dungeonState.toString(i.getDungeon().state);
			log.severe(message);
			return false;
			
		} else {
			log.info("Dungeon State : " + i.getDungeon().getStatusDisplay());
		}
		
		if(!DungeonManager.pasteSchematic(i.dungeonName, i.getOrigin())){
			log.severe("DungeonManager error while attempting to mount Instance '"+i.name+"'!");
			message = DungeonManager.message;
			return false;
		} else {
			setProtection(i);
			log.info("Successfully mounted Instance '"+i.name+"'!");
			i.state = instanceState.READY;
		}
				
		return true;
	}
	
	
	
	private static boolean setProtection(InstanceData i){
		//TODO : Integrate worldguard protection!
		log.severe(Config.ecol + "I WAS ASKED TO PROTECT A REGION!");
		log.severe(Config.ecol + "I HAVEN'T BEEN CODED TO DO THAT!");
		log.severe(Config.ecol + "INSTANCE IS UNPROTECTED!");
		
		return false;
	}
	
	public static boolean sendPlayerToInstance(String name, InstanceData i){
		log.severe(Config.ecol + "I WAS ASKED TO TELEPORT A PLAYER!");
		log.severe(Config.ecol + "I HAVEN'T BEEN CODED TO DO THAT!");
		log.severe(Config.ecol + "PLAYER IS UNTELEPORTED!");
		return true;
	}
	
	private static Vector getNextFree(){
		Vector inst = lastInstance;
		search:{
		for(int z = inst.getBlockZ(); z < 99; z++)
			for(int x = inst.getBlockX(); x < 99; x++){
				inst = inst.setX(x);
				inst = inst.setZ(z);
				log.info("Checking next at " + toRegionID(inst));
				if(instances.get(toRegionID(inst)) == null){
					break search;
				}
			}
		}
		lastInstance = inst;
		
		return inst;
	}
	
	public static Set<String> getInstanceList(){
		return instances.keySet();
	}
	
	public static boolean saveInstances(){
		log.info("saveinstances");
		if(instances.isEmpty()){
			log.info("No data to save - Nothing to do!");
			return true;
		}
		
		File f = new File(bridge.getDataDir(),Config.pathToInstances + "instances.csv");
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		


		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
		    Collection<InstanceData> d = instances.values();
		    
		    for(InstanceData dat : d){
		    	String s = dat.toCSV();
		    	bw.write(s);
		    	bw.newLine();
		    	
		    }
		    return true;
		} catch (FileNotFoundException e) {
			message = "No instances found!";
			
		} catch (IOException e) {
			message = Config.ecol + " Error : IOException writing data!";
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean loadInstances(){
		String fp = InstancedDungeon.getInstance().getDataFolder().toString() + Config.pathToInstances + "instances.csv";
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
				InstanceData d = new InstanceData();
				if(d.fromCSV(line)){
					d.synch();
					log.info("Loaded Instance '" + d.name + "'");
					instances.put(d.name, d);
				} else {
					log.severe("Failed to load instance : " + line);
				}
				return true;
			}
		} catch (FileNotFoundException e) {
			message = "No instances found!";
			
		} catch (IOException e) {
			message = Config.ecol + " Error : IOException reading data!";
			e.printStackTrace();
		}
		return false;
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
	
	public static String getRegionID(Vector coords){
		int regionX = (int)Math.floor((coords.getBlockX() / 16) / 32.0);
		int regionZ = (int)Math.floor((coords.getBlockZ() / 16) / 32.0);
		String t = regionX + "_" + regionZ;
		
		return t;
	}
	
	public static String toRegionID(Vector coords){
		return coords.getBlockX() + "_" + coords.getBlockY();
	}
	
	public static boolean isInstanceAt(Vector loc){
		Vector rc = blockToRegion(loc);

		return instances.containsKey(rc.getBlockX() + "_" + rc.getBlockZ());
	}
	
	public static InstanceData getInstanceAt(Vector loc){
		Vector rc = blockToRegion(loc);
		return instances.get(rc.getBlockX() + "_" + rc.getBlockZ());
	}
	
	public static String vToStr(Vector V){
		return V.getBlockX() + "," + V.getBlockY() + "," + V.getBlockZ();
	}
	
}
