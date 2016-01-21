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
import java.util.UUID;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData.instanceState;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

public class InstanceManager {
	public static final int REGION_SIZE = 512;
	public static final int HALF_REGION = 256;
	public static final Vector HALF_REGION_V = new Vector(256,0,256);
	public static final Vector REGION_V = new Vector(512,0,512);
	
	private static HashMap<String, InstanceData> instances = new HashMap<String, InstanceData>();
	private static HashMap<UUID, InstanceData> ownerInstances = new HashMap<UUID, InstanceData>();
	//private static ArrayList<String> regions = new ArrayList<String>();
	public static FunctionsBridge bridge = null;
	private static InstanceManager instance = null;
	private static Vector lastInstance = new Vector(0,0,0);
	
	protected InstanceManager(){
		bridge = (FunctionsBridge)InstancedDungeon.getInstance();
	}
	
	public static InstanceManager getInstance(){
		if(instance == null) {
			instance = new InstanceManager();
		}
		return instance;		
	}
	
	public static RetVal addOwner(String owner, InstanceData inst){
		RetVal r = new RetVal();
		
		InstanceData t = ownerInstances.get(bridge.getUUID(owner));
		if(t != null){
			r.Err("Player " + owner + " already owns instance '"+t.name+"', an instance of '"+t.dungeonName+"'! Cannot assign owner!");
			return r;
		}
		
		ownerInstances.put(bridge.getUUID(owner), inst);
		r.tru();
		
		return r ;
	}
	
	public static RetVal removeOwner(String owner){
		Log.debug("InstanceManager.removeOwner");
		RetVal r = new RetVal();
		
		InstanceData t = ownerInstances.get(bridge.getUUID(owner));
		if(t == null){
			r.Err("Player " + owner + " doesn't own an instance!");
			return r;
		}
		
		ownerInstances.remove(bridge.getUUID(owner));
		r.status = ownerInstances.get(bridge.getUUID(owner)) == null;
		return r ;
	}
	
	public static RetVal createInstance(String owner, String dungeon, boolean edit){
		Log.debug("InstanceManager.createInstance");
		RetVal r = new RetVal();
		
		if(owner == ""){
			r.Err("Instance owner cannot be blank!");
			
			return r;
		}
		UUID uuid = bridge.getUUID(owner);
		if(uuid == null){
			Log.severe("Owner uuid for "+owner+" == null");
			r.Err("Player UUID is null! Must be a real player! Must be online!");
			return r;
		} else {
			Log.debug("uuid == " + uuid.toString());
		}
		
		InstanceData i = ownerInstances.get(uuid); 
		if(i != null){
			r.Err("You already have an instance for Dungeon '"+i.dungeonName+"'! You can only have one Instance at a time!");
			return r;
		}
		
		if(dungeon == ""){
			r.Err("Dungeon cannot be blank!");
			
			return r;
		}		
		DungeonData d = DungeonManager.getDungeon(dungeon);
		
		if(d == null){
			Log.error("Dungeon '"+dungeon+"' not found!");
			r.Err("Dungeon '"+dungeon+"' not found!");
			return r;
		}
		
		//First, we need to do a few things.
		Vector atLoc = getNextFree();
		Log.debug("atLoc (Region) = " +Util.vToStr(atLoc));
		atLoc = Util.regionToBlock(atLoc);
		Log.debug("atLoc = " + Util.vToStr(atLoc) );
		String name = getRegionID(atLoc); //atLoc.getBlockX() + "_" + atLoc.getY();
		
		atLoc = HALF_REGION_V.add(atLoc);
		Log.debug("atLoc + halfregion = " + Util.vToStr(atLoc));
		atLoc = atLoc.setY(64);
		Log.debug("atLoc Final = " + Util.vToStr(atLoc));
		
		Log.debug("new InstanceData(" + name + ", " + owner + ", " + uuid.toString() + ", " + dungeon + ", " + Util.vToStr(atLoc) + ", false");
		
		i = new InstanceData(name, owner, uuid, dungeon, atLoc, edit);
		addOwner(owner, i);
		addInstance(i);
		r.retObj = i;
		
		Log.debug("Leaving Creation, status : " + i.getStatusDisplay());
		r.status = true;
		
		return r;
	}
	
	public static void addInstance(InstanceData i){
		Log.debug("InstanceManager.setInstance");

		if(i == null){
			Log.error("setInstance cannot accept null!");
			return;
		}
		instances.put(i.name, i);
	}
	
	public static InstanceData getInstance(String name){
		Log.debug("InstanceManager.getInstance");

		if(!instances.containsKey(name)){
			Log.error("Instance '"+name+"' does not exist!");
			return null;
		} else {
			return instances.get(name);
		}
	}
	
	public static void delInstance(String name){
		Log.debug("InstanceManager.delInstance");
		if(!instances.containsKey(name)){
			Log.error("Instance '"+name+"' does not exist!");
			return;
		} 
		
		instances.remove(name);
		
		
		
	}
	
	public static RetVal unmountRegion(String name){
		Log.debug("InstanceManager.unmountRegion");
		RetVal r = new RetVal();

		if(name == null || name == ""){
			r.Err("Invalid parameters! Instance Name cannot be empty!");
			return r;
		}
		InstanceData i = getInstance(name);
		if(i == null){
			Log.warning("unmountRegion was passed a non-existant instance to unmount!");
			r.Err("Instance '"+name+"' not found!");
			return r;
		} // allow getInstance error message through
		
		if(i.state != instanceState.READY){
			r.Err("Cannot unmount an instance that is not in the READY state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
			Log.severe("Cannot unmount an instance that is not in the WAITING state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
			return r;
		} else {
			Log.debug("Instance Status : " + i.getStatusDisplay());
		}
		
		
		//Make sure there are no players in the area!
		HashMap<Location, String> l = bridge.getAllPlayerLocs(Config.dimension);
		BukkitWorld W = new BukkitWorld(bridge.getIDim());
		
		Log.debug("Checking "+l.keySet().size() +" locations.");
		Log.debug("Location (Min/Max) is : " + Util.vToStr(i.getBounds().getMinimumPoint()) + " to " + Util.vToStr(i.getBounds().getMaximumPoint()));
		
		for(Location loc : l.keySet()){
			Log.debug("Examining :" + loc.getWorld().getName() + " : " + loc.getPosition().getX() + ", "+ loc.getPosition().getY() + ", "+ loc.getPosition().getZ());
			if(loc.getWorld().getName() == W.getWorld().getName() && i.getBounds().contains(loc.getPosition())){	
				Log.debug("Moving player '" + l.get(loc) + "' to spawn!");
				bridge.tpPlayerToSpawn(l.get(loc), "Instance is being destroyed! For your safety, you are being moved to spawn!");
			}
		}
		
		Log.debug("Area is free of players!");
		
		RetVal rf = DungeonManager.deleteArea(i.getBounds().getMinimumPoint(), i.getBounds().getMaximumPoint());
		if(!rf.status){
			Log.severe("We ran into an issue clearing the area for instance " + i.name);
			r.addAll(rf.message);
			r.Err("Error occured unmounting instance!");
			return r;
		} else {
			i.state = instanceState.RELEASED;
			removeOwner(i.owner);
		}
		
		
		return r;
	}
	
	public static RetVal mountRegion(String name){
		Log.debug("InstanceManager.mountRegion");
		RetVal r = new RetVal();

		if(name == null || name == ""){
			r.Err("Invalid parameters! Instance Name cannot be empty!");
			return r;
		}
		InstanceData i = getInstance(name);
		if(i == null){
			Log.warning("mountRegion was passed a non-existant instance to mount!");
			r.Err("Instance '"+name+"' not found!");
			return r;
		} // allow getInstance error message through
		
		if(i.state != instanceState.WAITING){
			r.Err("Cannot mount an instance that is not in the WAITING state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
			Log.severe("Cannot mount an instance that is not in the WAITING state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
			return r;
		} else {
			Log.debug("Instance Status : " + i.getStatusDisplay());
		}
		
		if(i.getDungeon().state != dungeonState.READY){
			r.Err("Cannot mount an instance whose underlying Dungeon is not in the READY state - Dungeon '" + i.getDungeon().name + "' is in state : "+ dungeonState.toString(i.getDungeon().state));
			Log.severe("Cannot mount an instance whose underlying Dungeon is not in the READY state - Dungeon '" + i.getDungeon().name + "' is in state : "+ dungeonState.toString(i.getDungeon().state));
			return r;
			
		} else {
			Log.debug("Dungeon State : " + i.getDungeon().getStatusDisplay());
		}
		RetVal rf = DungeonManager.pasteSchematic(i.dungeonName, i.getOrigin()); 
		
		if(!rf.status){
			Log.severe("DungeonManager error while attempting to mount Instance '"+i.name+"'!");
			r.message.addAll(rf.message);
			r.Err("Failed to paste schematic! Errors from Dungeon Manager included above!");
			return r;
		} else {
			rf = setProtection(i);
			if(rf.status){
				Log.error("Failed to add protection to newly mounted Instance '"+i.name+"'");
				r.addAll(rf.message);
				//r.Err("Errors encountered mounting instance! For your protection, this instance will be flagged as unusable!");
				//TODO : get WG protection working.
				//i.state = instanceState.INVALID;
			}
			
			Log.debug("Successfully mounted Instance '"+i.name+"'!");
			i.state = instanceState.READY;
			r.tru();
			
		}
				
		return r;
	}
	
	
	
	private static RetVal setProtection(InstanceData i){
		Log.debug("InstanceManager.");
		RetVal r = new RetVal();

		//TODO : Integrate worldguard protection!
		Log.severe(Config.ecol + "I WAS ASKED TO PROTECT A REGION!");
		Log.severe(Config.ecol + "I HAVEN'T BEEN CODED TO DO THAT!");
		Log.severe(Config.ecol + "INSTANCE IS UNPROTECTED!");
		r.Err("Can't set protections - I dunno how!");
		return r;
	}
	
	public static RetVal sendPlayerToInstance(String name, InstanceData i){
		Log.debug("InstanceManager.");
		RetVal r = new RetVal();

		Log.severe(Config.ecol + "I WAS ASKED TO TELEPORT A PLAYER!");
		Log.severe(Config.ecol + "I HAVEN'T BEEN CODED TO DO THAT!");
		Log.severe(Config.ecol + "PLAYER IS UNTELEPORTED!");
		r.Err("Can't TP - I dunno how!");
		return r;
	}
	
	private static Vector getNextFree(){
		Log.debug("InstanceManager.getNextFree");

		Vector inst = lastInstance;
		search:{
		for(int z = inst.getBlockZ(); z < 99; z++)
			for(int x = inst.getBlockX(); x < 99; x++){
				inst = inst.setX(x);
				inst = inst.setZ(z);
				Log.debug("Checking next at " + toRegionID(inst));
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
	
	public static RetVal saveInstances(){
		Log.debug("InstanceManager.saveInstances");
		RetVal r = new RetVal();

		if(instances.isEmpty()){
			Log.info("No data to save - Nothing to do!");
			r.add("No instances to save!");
			r.status = true;
			return r;
		}
		
		File f = new File(bridge.getDataDir(),Config.pathToInstances + "instances.csv");
		if(!f.getParentFile().exists()){
			Log.debug("Creating path : " + f.getParentFile().getPath());
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
		    r.add("Saved Instances!");
		    r.status = true;
		    
		} catch (FileNotFoundException e) {
			r.IntErr("No instances found!");
		} catch (IOException e) {
			r.IntErr("IOException writing data!");
			e.printStackTrace();
		}
		return r;
	}
	
	public static RetVal loadInstances(){
		Log.debug("InstanceManager.loadInstances");
		RetVal r = new RetVal();

		String fp = InstancedDungeon.getInstance().getDataFolder().toString() + Config.pathToInstances + "instances.csv";
		File f = new File(fp);
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			if(f.createNewFile()){
				//If the file doesn't exist, and we just created it, then there's really no error.
				r.add("No instances to load!");
				return r;
			}
		} catch (IOException e) {
			r.IntErr("Error establishing Instances save file!");
			e.printStackTrace();
			return r;
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				InstanceData d = new InstanceData();
				if(line == ""){
					continue;
				}
				if(d.fromCSV(line)){
					d.synch();
					Log.debug("Loaded Instance '" + d.name + "'");
					instances.put(d.name, d);
					ownerInstances.put(d.getUUID(), d);
				} else {
					Log.severe("Failed to load instance : '" + line + "'!");
					r.IntErr("Failed to read an instance!");
				}
			}
		} catch (FileNotFoundException e) {
			r.IntErr("No instances found!");
			Log.severe("Error : File not found loading instances!");
			return r;
			
		} catch (IOException e) {
			Log.severe("IO Exception reading data!");
			r.IntErr("Error reading data!");
			e.printStackTrace();
			return r;
		}
		r.add("Read in Instances!");
		r.status = true;
		return r;
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
		Vector rc = Util.blockToRegion(loc);

		return instances.containsKey(rc.getBlockX() + "_" + rc.getBlockZ());
	}
	
	public static InstanceData getInstanceAt(Vector loc){
		Vector rc = Util.blockToRegion(loc);
		return instances.get(rc.getBlockX() + "_" + rc.getBlockZ());
	}
	
	public static InstanceData getInstanceOwnedBy(String name){
		if(name == null || name == ""){
			Log.error("Cannot get data for a null player name!");
			return null;
		}
		
		return ownerInstances.get(name);
	}

	public static RetVal createAndMountInstance(String dungeon, String owner, boolean editmode) {
		Log.debug("InstanceManager.createAndMountInstance");
		RetVal r = new RetVal();
		
		InstanceData i = InstanceManager.getInstanceOwnedBy(owner);
		if(i != null){
			r.Err("You already have an instance for Dungeon '"+i.dungeonName+"'! You can only have one Instance at a time!");
			return r;
		}
		
		RetVal rf = InstanceManager.createInstance(owner, dungeon, editmode);
		
		if(rf.retObj == null || !(rf.retObj instanceof InstanceData) || !rf.status){
			Log.severe("Failed to create instance!");
			r.addAll(rf.message);
			return r;
		} else {
			Log.debug("Created Instance Data.");
		}
		
		i = (InstanceData)rf.retObj;
		
		//TODO - This will have to be asynch somehow. :(
		rf = InstanceManager.mountRegion(i.name);
		if(i.state == instanceState.READY && rf.status){
			Log.debug("Instance State : " + i.getStatusDisplay());
			r.add("New Instance successfully created and mounted! Instance '" + i.name + "' is up and running!");
			rf = InstanceManager.sendPlayerToInstance(owner, i);
			
			r.addAll(rf.message);
			r.tru();
			return r;
		} else {
			Log.severe("Something went wrong mounting instance!");
			Log.severe("Instance State : " + i.getStatusDisplay());
			r.addAll(rf.message);
			
			InstanceManager.removeOwner(owner);
			InstanceManager.delInstance(i.name);
			i = null;
			
			return r;
		}
		
	}
		
}
