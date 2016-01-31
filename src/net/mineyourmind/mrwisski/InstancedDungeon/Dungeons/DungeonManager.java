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
import com.sk89q.worldedit.regions.CuboidRegion;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.MCEditExtendedSchematicFormat;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.AsyncManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

public class DungeonManager {
	//A Place to store all our instances of this dungeon.
	private static HashMap<String, DungeonData> dungeons = new HashMap<String,DungeonData>();
	//A place to store our pending async edits
	private static HashMap<DungeonData, InstanceData> pastes = new HashMap<DungeonData, InstanceData>();
	
	public static DungeonManager instance = null;
	public static FunctionsBridge bridge = null;
	public static AsyncManager asm = new AsyncManager();
	static MCEditExtendedSchematicFormat mcee = new MCEditExtendedSchematicFormat();
	
	public static String test(String name){
		//DungeonData d = dungeons.get(name);
		/*String s = d.toCSV();
		Log.severe("RESULT : " + s);
		
		Log.info("Attempting to re-instance");
		DungeonData d2 = new DungeonData();
		d2.fromCSV(s);
		*/
		
		DungeonManager.prepDungeon(name);
		
		return "done.";
		
		
	}
	
	protected DungeonManager(){
		bridge = (FunctionsBridge)InstancedDungeon.getInstance();
	}
	
	public static DungeonManager getInstance(){
		if(instance == null) {
			instance = new DungeonManager();
		}
		return instance;		
	}
	
	public static RetVal pasteTemplate(String name, Vector where){
		Log.debug("DungeonManager.pasteTemplate - " + name + ", " + Util.vToStr(where));
		RetVal r = new RetVal();
		
		DungeonData d = DungeonManager.getDungeon(name);
		if(d == null){
			r.message.add("No Dungeon found : " + name);
			return r;
		}
		
		EditSession es = bridge.getAsyncEditSession();
		es.setFastMode(true);
		
		File f = bridge.getWEditSchematic(d.templateLoc);
		CuboidClipboard cc;
		try {
			cc = mcee.load(f);
		} catch (IOException | DataException e) {
			Log.error("pasteTemplate() : IOException while reading template!");
			r.IntErr();
			e.printStackTrace();
			return r;
		}
		
		try {
			cc.paste(es, where, true);
		} catch (MaxChangedBlocksException e) {
			r.message.add("Too many blocks! Please using a smaller schematic!");
			e.printStackTrace();
			return r;
		}
		//Force the chunk to load up so worldedit will do the paste.
		bridge.loadChunk(Config.dimension, where.getBlockX(), where.getBlockY(), where.getBlockZ());
		r.status = true;
		return r;
	}
	
	public static RetVal pasteSchematic(String name, InstanceData i){
		Log.debug("DungeonManager.pasteSchematic - " + name + ", " + Util.vToStr(i.getOrigin()));
		RetVal r = new RetVal();

		DungeonData d = DungeonManager.getDungeon(name);
		if(d == null){
			r.Err("Couldn't find Dungeon '" +name+ "'!");
			return r;
		}
		
		//So that we can track when a dungeon is done pasting.
		pastes.put(d, i);
		asm.pasteClipboard(i, i.getOrigin());
		
		r.add("Dungeon Instance is being prepared - You will be teleported when it is ready!");
		r.tru();
		return r;
	}
	
	public static RetVal deleteArea(Vector MinLocation, Vector MaxLocation){
		Log.debug("DungeonManager.deleteArea");
		RetVal r = new RetVal();
		
		EditSession es = bridge.getAsyncEditSession();
		es.setFastMode(true);
		
		CuboidRegion cr = new CuboidRegion(new BukkitWorld(bridge.getIDim()), MinLocation, MaxLocation);
		
		BaseBlock block = new BaseBlock(0);
		
		if(es == null || cr == null || block == null){
			r.IntErr("A value we needed couldn't be determined!");
			return r;
		}
		bridge.loadChunk(Config.dimension, MinLocation.getBlockX(), MinLocation.getBlockY(), MinLocation.getBlockZ());
		try {
			
			es.setBlocks(cr, block);
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
		Log.debug("Cleared area successfully!");
		r.tru();
		
		return r;
	}
	
	public static DungeonData getDungeon(String name){
		Log.debug("DungeonManager.getDungeon");
		
		if(name == null || name == ""){
			Log.severe("getDungeon - Cannot get dungeon with a null/empty name!");
			return null;			
		}
		if(!dungeons.containsKey(name)){
			Log.warning("Cannot find dungeon '" + name + "' - Did you create it?");
			return null;
		}

		return dungeons.get(name);
	}
	
	public static boolean addDungeon(DungeonData dungeon){
		Log.debug("addDungeon");
		if(dungeon == null){
			Log.error("Cannot save a null dungeon!");
			return false;						
		}
		dungeons.put(dungeon.name, dungeon);
		return true;
	}
	
	public static RetVal createDungeon(String name){
		Log.debug("DungeonManager.createDungeon");
		RetVal r = new RetVal();
		
		if(name == null || name == ""){
			r.Err("Cannot create dungeon with no name!");
			return r;			
		}
		if(dungeons.containsKey(name)){
			r.Err("Cannot create dungeon '" + name + "' - Dungeon already exists!");
			return r;
		}
		DungeonData dungeon = new DungeonData(name);
		if(addDungeon(dungeon)){
			r.add("Created Dungeon '" +name+ "'!");
			r.status = true;
		} else {
			r.Err("Failed to create Dungeon '"+name+"'!");
		}
		return r;
	}
	
	//Reloads the clipboard selection - only use this during initial load!
	public static RetVal setTemplate(DungeonData d){
		Log.debug("DungeonManager.setTemplate");
		RetVal r = new RetVal();
		
		if(d == null){
			Log.severe("setTemplate - Can't set template on a null dungeon!");
			r.IntErr();
			return r;
		}
		//If we're not already prepared
		if(d.templateLoc != ""){
			try {
								
				d.setTemplate(mcee.load(bridge.getWEditSchematic(d.templateLoc)));
			} catch (DataException | IOException e) {
				e.printStackTrace();
				r.IntErr();
				return r;
			}
		} else {
			Log.warning("Tried to setTemplate on a dungeon without a template loc - This dungeon is probably corrupted!");
			r.Err("Dungeon '"+d.name+"' does not have a template set! This dungeon might be corrupted!");
			return r;
		}
		
		r.status = true;
		return r;
	}
	
	//Sets the Schematic, and the Edit Schematic.
	public static RetVal setSchematic(DungeonData d){
		Log.debug("DungeonManager.setSchematic");
		RetVal r = new RetVal();
		
		if(d == null){
			Log.severe("setSchematic was passed null for DungeonData!");
			r.Err("Cannot set the schematic for a null dungeon!");
			return r;
		}
		//If we're not already prepared
		if(d.schematicLoc != ""){
			try {
				d.setSchematic(mcee.load(new File(bridge.getDataDir(),Config.pathToDungeons + d.schematicLoc )));
			} catch (DataException | IOException e) {
				r.IntErr("Error reading schematic from disk!");
				e.printStackTrace();
				return r;
			}
		}
		//If we've got an edit schematic location set, we'll set that here as well.
		if(d.editSchematicLoc != ""){
			try {
				d.setEditSchematic(mcee.load(new File(bridge.getDataDir(),Config.pathToDungeons + d.editSchematicLoc )));
			} catch (DataException | IOException e) {
				r.IntErr("Error reading edit schematic from disk!");
				e.printStackTrace();
				return r;
			}			
		}
		r.status = true;
		return r;
	}
	
	
	public static void printNBT(Vector Location, CompoundTag tag){
	
		String indentSpace = StringUtils.repeat(" ", 4);
		if(tag != null){
			Log.info(Location.toString() + " : " + tag.getName());
		
			for(String sss : tag.getValue().keySet()){
				Log.info(indentSpace + sss + " = " + tag.getValue().get(sss).getValue());
				
			}
		}
	}
	
	//Just saves out the currently stored clipboard - needed for Edit Dungeon saving.
	public static RetVal saveSchematic(DungeonData d){
		Log.debug("DungeonManager.saveSchematic");
		RetVal r = new RetVal();
		
		try {
			File f = new File(bridge.getDataDir(),Config.pathToDungeons + d.schematicLoc );
			mcee.save(d.getSchematic(), f);
		} catch (IOException | DataException e) {
			e.printStackTrace();
			r.Err("Failed to save out schematic : " + d.schematicLoc);
			return r;
		}
		r.add("Saved out schematic '"+d.schematicLoc+"'!");
		r.tru();
		return r;
		
	}
	
	//Just saves out the currently stored editing clipboard - needed for Edit Dungeon saving.
	public static RetVal saveEditSchematic(DungeonData d){
		Log.debug("DungeonManager.saveEditSchematic");
		RetVal r = new RetVal();
		
		try {
			File f = new File(bridge.getDataDir(),Config.pathToDungeons + d.editSchematicLoc );
			mcee.save(d.getEditSchematic(), f);
		} catch (IOException | DataException e) {
			e.printStackTrace();
			r.Err("Failed to save out schematic : " + d.schematicLoc);
			return r;
		}
		r.add("Saved out edit schematic '"+d.editSchematicLoc+"'!");
		r.tru();
		return r;
		
	}
	
	@SuppressWarnings("deprecation")
	public static RetVal prepDungeon(String name){
		Log.debug("DungeonManager.prepDungeon");
		RetVal r = new RetVal();
		
		DungeonData d = getDungeon(name);
		if(d == null){
			Log.debug("Cannot prep '"+name+"' - Dungeon not found!");
			r.Err("Cannot prep '"+name+"' - Dungeon not found!");
			// let the getDungeon error message stand.
			return r;
		}
		
		if(d.state >= dungeonState.READY){
			r.Err("This dungeon is already in a READY state!");
			Log.info("Dungeon '"+name+"' is ready! This dungeon is already prepared!");
			return r;
		}
		//Grab our clipboard region - we'll need it shortly.
		CuboidClipboard t = d.getTemplate();
		if(t == null){
			Log.info("Trying to prep dungeon '"+name+"' without a template set - attempting to set template!");
			DungeonManager.setTemplate(d);
			t = d.getTemplate();
			if(t == null){
				Log.error("Couldn't get the template for dungeon '" + name+ "'! This dungeon is likely in an INVALID state!");
				r.message.add(Config.ecol + " Error getting template schematic - Dungeon is likely invalid!");
				return r;
			}
		}
		
		Log.debug("BEGINNING PREP : "+t.getWidth()+","+t.getHeight()+","+t.getLength()+"!");
		t.setOffset(new Vector(0,0,0));
		t.setOrigin(new Vector(0,0,0));
		
		Material borderMat = Material.getMaterial(Config.border);
		BaseBlock border = new BaseBlock(borderMat.getId());
	
		for(float x = 0; x < t.getWidth(); x++)
			for(float y = 0; y < t.getHeight(); y++)
				for(float z = 0; z < t.getLength(); z++){
					Vector v = new Vector(x,y,z);
					BaseBlock b = t.getPoint(v);
					
					//Material m = Material.getMaterial(b.getId());
					//CompoundTag tag = b.getNbtData();
					/*
					if(tag != null){
						Log.info("NBT for block " + m.name() + " : ");
						printNBT(v,tag);
					}
					LocalEntity[] l = t.pasteEntities(new Vector(x,y,z));
					if(l.length != 0){
						Log.info("Got entities : ");
						for(LocalEntity e : l){
							Log.info(e.toString());
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
			mcee.save(t, f);
		} catch (IOException | DataException e) {
			e.printStackTrace();
			r.IntErr("Failed to save prepared Schematic!");
			return r;
		}
		d.schematicLoc = d.templateLoc;
		d.state = dungeonState.PREPPED;
		DungeonManager.setSchematic(d);
		r.message.add("Successfully prepared Dungeon : " +Config.bcol + name);
		r.status = true;
		return r;
		
	}
	
	public static Set<String> getDungeonList(){
		return dungeons.keySet();
	}
	
	public static RetVal saveDungeons(){
		Log.debug("DungeonManager.saveDungeons");
		RetVal r = new RetVal();
		
		if(dungeons.isEmpty()){
			Log.debug("No data to save - Nothing to do!");
			r.add("No data to save - Nothing to do!");
			r.status = true;
			return r;
		}
		
		File f = new File(bridge.getDataDir(),Config.pathToDungeons + "dungeons.csv");
		if(!f.getParentFile().exists()){
			Log.debug("Creating path : " + f.getParent());
			f.getParentFile().mkdirs();
		}
		
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		int c = 0;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
		    Collection<DungeonData> d = dungeons.values();
		    
		    for(DungeonData dat : d){
		    	String s = dat.toCSV();
		    	bw.write(s);
		    	bw.newLine();
		    	c++;
		    	//if we're editing, we definately want to save out the state of the edit schematic
		    	if(dat.editSchematicLoc != ""){
		    		DungeonManager.saveEditSchematic(dat);
		    	}
		    	
		    }
		    r.add("Read in " + c + " dungeons!");
		    r.status = true;
		    return r;
		} catch (FileNotFoundException e) {
			Log.debug("Couldn't find file : " + f.getPath());
			r.add("No Dungeons found!");
			return r;
		} catch (IOException e) {
			
			r.add(Config.ecol + " Error : IOException writing data!");
			e.printStackTrace();
			return r;
		}
	}
	
	public static RetVal loadDungeons(){
		Log.debug("DungeonManager.loadDungeons");
		RetVal r = new RetVal();
		

		String fp = InstancedDungeon.getInstance().getDataFolder().toString() + Config.pathToDungeons + "dungeons.csv";
		File f = new File(fp);
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		
		try {
			if(f.createNewFile()){
				//If the file doesn't exist, and we just created it, then there's really no error.
				r.add("Created new Dungeon storage file!");
				r.status = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				DungeonData d = new DungeonData();
				if(line == "") continue;
				
				if(d.fromCSV(line)){
					d.synch();
					Log.debug("Loaded Dungeon '" + d.name + "'");
					r.add("Loaded Dungeon '" + d.name + "'");
					dungeons.put(d.name, d);
				} else {
					Log.severe("Failed to load dungeon : " + line);
					r.IntErr("Failed to load a dungeon!");
				}
			}
		} catch (FileNotFoundException e) {
			r.IntErr("Dungeons file not found!");
			return r;
		} catch (IOException e) {
			r.IntErr("Error reading data!");
			e.printStackTrace();
		}
		r.add("Loaded all Dungeons!");
		return r;

	}
	
	//Returns a dungeon to it's PREPPED state, so it can be re-edited.
	public static RetVal unReadyDungeon(String name){
		Log.debug("DungeonManager.unReadyDungeon");
		RetVal r = new RetVal();
		
		DungeonData d = DungeonManager.getDungeon(name);
		if(d == null){
			r.Err("Dungeon '"+name+"' not found!");
			return r;
		}
		
		if(d.state == dungeonState.READY){
			d.state = dungeonState.PREPPED;
			r.add("Dungeon '"+ name +"' has been returned to a PREPPED state!");
			r.tru();
			return r;
		} else {
			r.Err("You can only un-READY dungeons that are in the READY state! Dungeons in EDITING can be freely re-edited. Dungeons prior to PREPPED can be prepped!");
			return r;
		}
	}
	
	//Saves the current Edit Schematic to the regular schematic, and cleans up edit data.
	public static RetVal applyEditSchematic(DungeonData d){
		Log.debug("DungeonManager.applyEditSchematic");
		RetVal r = new RetVal();
		
		CuboidClipboard edit = d.getEditSchematic();
		
		//We'll go ahead and backup the pre-edit schematic to the file the edit schematic used to be
		try {
			File f = new File(bridge.getDataDir(),Config.pathToDungeons + d.editSchematicLoc );
			mcee.save(d.getSchematic(), f);
		} catch (IOException | DataException e) {
			e.printStackTrace();
			r.Err("Failed to save out pre-edit schematic : " + d.editSchematicLoc);
			return r;
		}
		d.setEditSchematic(null);
		d.editSchematicLoc = "";
		d.setSchematic(edit);
		
		r.add("Saved out pre-edit schematic to '"+d.editSchematicLoc+"'!");
		r.tru();
		return r;
		
	}
	
	public static void notifyPasteDone(InstanceData i){
		Log.info("DungeonManager.NotifyPasteDone("+i.name+")");
		InstanceManager.notifyInstanceReady(i);
	}
	
	
}
