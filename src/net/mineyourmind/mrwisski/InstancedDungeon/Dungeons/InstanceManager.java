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

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
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
	private static HashMap<String, InstanceData> editInstances = new HashMap<String, InstanceData>();
	
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
	
	public static boolean isEditing(String name){
		return editInstances.containsKey(name);
	}
	
	public static InstanceData getEditInstance(String name){
		return editInstances.get(name);
	}
	
	public static InstanceData getEditInstanceForDungeon(String dungeon){
		Log.debug("getEditInstanceForDungeon");
		for(InstanceData i : editInstances.values()){
			Log.debug("Checking instance dungeon name " + i.dungeonName + " against " + dungeon);
			if(i.dungeonName.equals(dungeon)){
				return i;
			}
		}
		
		return null;
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
		
		if(d.state == dungeonState.INVALID){
			r.Err("Dungeon in the wrong state! Requires a state OTHER than INVALID, but is in an INVALID state!");
			return r;
		}
		
		if(edit){
			if(d.state != dungeonState.EDITING){
				r.Err("Dungeon in the wrong state! Edit flag is set - Dungeon is required to be in the EDITING state, but is in state '"+dungeonState.toString(d.state)+"' instead!");
				return r;
			}
		} else {
			if(d.state == dungeonState.EDITING){
				r.Err("Dungeon in the wrong state! Edit flag is not set, but Dungeon is in the EDITING state! Please close out any Edit Instances of this dungeon before proceeding!");
				return r;
			}
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
		if(edit){
			editInstances.put(i.name, i);
		}
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
		
		if(i.state != instanceState.READY && i.state != instanceState.EDIT){
			r.Err("Cannot unmount an instance that is not in the READY or EDIT state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
			Log.severe("Cannot unmount an instance that is not in the READY or EDIT state - Instance '"+name+"' is in state : "+ instanceState.toString(i.state));
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
		
		//Do the actual removal - or rather, let the DungeonManager do that ^_^
		RetVal rf = DungeonManager.deleteArea(i.getBounds().getMinimumPoint(), i.getBounds().getMaximumPoint());
		removeOwner(i.owner); // We should remove the owner regardless.
		if(!rf.status){
			Log.severe("We ran into an issue clearing the area for instance " + i.name);
			r.addAll(rf.message);
			r.Err("Error occured unmounting instance!");
			return r;
		} else {
			if(i.state == instanceState.EDIT){
				//clean up edit instance stuff.
				editInstances.remove(i.name);
				i.getDungeon().state = dungeonState.PREPPED; // Take dungeon out of the EDITING state.
			}
			i.state = instanceState.RELEASED;
			r.tru();
		
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
		
		if(i.getDungeon().state != dungeonState.READY && i.getDungeon().state != dungeonState.EDITING){
			r.Err("Cannot mount an instance whose underlying Dungeon is not in the READY or EDITING state - Dungeon '" + i.getDungeon().name + "' is in state : "+ dungeonState.toString(i.getDungeon().state));
			Log.severe("Cannot mount an instance whose underlying Dungeon is not in the READY or EDITING state - Dungeon '" + i.getDungeon().name + "' is in state : "+ dungeonState.toString(i.getDungeon().state));
			return r;
			
		} else {
			Log.debug("Dungeon State : " + i.getDungeon().getStatusDisplay());
		}
		
		RetVal rf = DungeonManager.pasteSchematic(i.dungeonName, i); 
		
		if(!rf.status){
			Log.severe("DungeonManager error while attempting to mount Instance '"+i.name+"'!");
			r.message.addAll(rf.message);
			r.Err("Failed to paste schematic! Errors from Dungeon Manager included above!");
			return r;
		} else {
			i.state = instanceState.WAITING; //We're now waiting for the build to be completed!
			r.add("Your Instance is being prepared! You will be teleported when it is ready!");
			r.tru();
			return r;
			
		}
	}
	
	public static void notifyInstanceReady(InstanceData i){
		RetVal rf = new RetVal();
		//InstanceData i = InstanceManager.pendingInstances.get(d);
		
		
		rf = setProtection(i);
		if(rf.status){
			Log.error("Failed to add protection to newly mounted Instance '"+i.name+"'");
			//r.Err("Errors encountered mounting instance! For your protection, this instance will be flagged as unusable!");
			//TODO : get WG protection working.
			//i.state = instanceState.INVALID;
		}
		
		Log.debug("Successfully mounted Instance '"+i.name+"'!");
		if(i.getDungeon().state == dungeonState.EDITING){
			i.state = instanceState.EDIT;
		} else {
			i.state = instanceState.READY;
		}
		
		sendPlayerToInstance(i.owner, i);

		
	}
	
	private static RetVal setProtection(InstanceData i){
		Log.debug("InstanceManager.setProtection");
		RetVal r = new RetVal();
		r.tru();

		//TODO : Integrate worldguard protection!
		Log.severe(Config.ecol + "I WAS ASKED TO PROTECT A REGION!");
		Log.severe(Config.ecol + "I HAVEN'T BEEN CODED TO DO THAT!");
		Log.severe(Config.ecol + "INSTANCE IS UNPROTECTED!");
		r.Err("Can't set protections - I dunno how!");
		return r;
	}
	
	public static RetVal sendPlayerToInstance(String name, InstanceData i){
		Log.debug("InstanceManager.sendPlayerToInstance");
		RetVal r = new RetVal();

		if(name == null || name == ""){
			r.Err("Cannot tp a null player!");
			return r;
		}
		
		if(i == null){
		}
		
		if(i.state != instanceState.READY && i.state != instanceState.EDIT){
			r.Err("Cannot TP a player to an instance that is not in a READY or EDIT state - Instance is in the '"+instanceState.toString(i.state)+"' state!");
			return r;
		}
		
		
		Vector v = i.getBounds().getMinimumPoint();
		Vector offv = v.add(new Vector(i.getDungeon().spawnX, i.getDungeon().spawnY, i.getDungeon().spawnZ));
		bridge.tpPlayer(name, Config.dimension,offv.getBlockX(), offv.getBlockY(), offv.getBlockZ(), i.getDungeon().spawnYaw, i.getDungeon().spawnPitch);
		
		r.tru();
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
		    	if(dat.state == instanceState.INVALID){Log.debug("Skipping INVALID instance."); continue;}
		    	if(dat.state == instanceState.RELEASED){Log.debug("Skipping RELEASED instance."); continue;}
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
					if(d.state == instanceState.EDIT){
						editInstances.put(d.name, d);
					}
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
		return coords.getBlockX() + "_" + coords.getBlockZ();
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
		
		//This is all handled properly through AsyncWorldEdit now :D
		
		rf = InstanceManager.mountRegion(i.name);

		if(i.state == instanceState.WAITING && rf.status){
			Log.debug("InstanceManager reports mountRegion succeeded.");
			Log.debug("Instance State : " + i.getStatusDisplay());
			
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
	
	public static RetVal createAndMountEditInstance(String dungeon, String owner){
		Log.debug("InstanceManager.createAndMountEditInstance");
		RetVal r = new RetVal();
		
		//Lets do some checking to make sure we get valid data.
		DungeonData d = DungeonManager.getDungeon(dungeon);
		if(d == null){
			r.Err("Could not find a dungeon by the name of '"+dungeon+"'!");
			return r;
		}
		
		InstanceData i = InstanceManager.getInstanceOwnedBy(owner);
		if(i != null){
			r.Err("You already have an instance for Dungeon '"+i.dungeonName+"'! You can only have one Instance at a time!");
			return r;
		}
		
		if(d.state == dungeonState.INVALID) {
			r.Err("This dungeon appears to have errors - Please check console log!");
			return r;
		}
		
		d.state = dungeonState.EDITING;
		//create an instance, with the edit flag set to TRUE!
		RetVal rf = InstanceManager.createInstance(owner, dungeon, true);
		
		if(rf.retObj == null || !(rf.retObj instanceof InstanceData) || !rf.status){
			Log.severe("Failed to create instance!");
			r.addAll(rf.message);
			return r;
		} else {
			Log.debug("Created Edit Instance Data.");
		}
		
		i = (InstanceData)rf.retObj;
		
		//Mount it!
		rf = InstanceManager.mountRegion(i.name);
		r.retObj = i;
		
		//Check it!
		if(i.state == instanceState.EDIT && rf.status){
			Log.debug("Instance State : " + i.getStatusDisplay());
			//r.add("New Edit Instance successfully created and mounted! Instance '" + i.name + "' is up and running!");
			
			//Here we need to copy the current dungeon schematic data over to the edit schematic
			i.getDungeon().editSchematicLoc = "edit_" + i.getDungeon().schematicLoc;
			i.getDungeon().setEditSchematic(i.getDungeon().getSchematic());
			
			r.addAll(rf.message);
			r.tru();
			return r;
		} else {
			Log.severe("Something went wrong mounting instance!");
			Log.severe("Instance State : " + i.getStatusDisplay());
			r.addAll(rf.message);
			
			//We failed, so lets roll back the changes we made :(
			InstanceManager.removeOwner(owner);
			InstanceManager.delInstance(i.name);
			i = null;
			
			return r;
		}
	}
	
	public static boolean handleBlockBreak(int x, int y, int z, String player){
		Log.debug("InstanceManager.handleBlockBreak");
		Vector v = new Vector(x,y,z);
		String name = getRegionID(v);
		Log.debug("Checking : " + name);
		if(isEditing(name)){
			Log.debug("Removing block at " + v.toString() + " in the schematic!");
			InstanceData i = getEditInstance(name);
			if(i != null){
				//Subtract from our block position, the min point vector - this will give us our
				//offset into the schematic.
				Vector offV = v.subtract(i.getBounds().getMinimumPoint());
				i.getDungeon().getEditSchematic().setBlock(offV, new BaseBlock(0));
				
			}
		} else {
			return false;
		}
		
		//TODO : block break flag in instances, check, return true to cancel the event.
		
		
		return false;
	}
		
	public static boolean handleBlockPlace(int x, int y, int z, String player, int ID, int meta){
		Log.debug("InstanceManager.handleBlockPlace");
		Vector v = new Vector(x,y,z);
		String name = getRegionID(v);
		Log.debug("Checking : " + name);
		if(isEditing(name)){
			Log.debug("Placing block ("+ID + ":" +meta+ ") at " + v.toString() + " in the schematic!");
			InstanceData i = getEditInstance(name);
			if(i != null){
				//Subtract from our block position, the min point vector - this will give us our
				//offset into the schematic.
				Vector offV = v.subtract(i.getBounds().getMinimumPoint());
				i.getDungeon().getEditSchematic().setBlock(offV, new BaseBlock(ID, meta));
				
			}
			
			
		} else {
			return false;
		}
		return false;
	}
}
