package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.io.IOException;
import java.util.ArrayList;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.MCEditExtendedSchematicFormat;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData.instanceState;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandDungeon implements CommandFunctor {
	private FunctionsBridge bridge = null;
	
	public CommandDungeon(FunctionsBridge bridge){
		DungeonManager.getInstance();
		this.bridge = bridge;
	}
	
	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		Log.debug("CommandDungeon.execute");
		RetVal r = new RetVal();
		
		arg.remove(0);	//Remove 'dungeon' from the arguments list.
		
		if(arg.isEmpty()){
			Log.debug("arg is empty.");
			r.addAll(getFullHelp());
			r.status = true;
			return r;
		}
		switch(arg.get(0)){
			case "help":
				r.addAll(getFullHelp());
				r.status = true;
				return r;
			case "create":
				arg.remove(0);
				return subCreate(arg, pName);
			case "delete":
				arg.remove(0);
				return subDelete(arg, pName);
			case "prepare":
				arg.remove(0);
				return DungeonManager.prepDungeon(arg.get(0));
			case "save":
				return subSave(arg.get(0));
			case "edit":
				arg.remove(0);
				return subEdit(arg, pName);
			case "finalize":
				arg.remove(0);
				return subFinalize(arg,pName);
			case "unready":
				arg.remove(0);
				return DungeonManager.unReadyDungeon(arg.get(0));
			default:
				Log.debug("default handler.");
				r.addAll(getFullHelp());
				r.Err("Couldn't find command '"+arg.get(0)+"'!");
				r.status = false;
				return r;
		}
	}
	
	private RetVal subFinalize(ArrayList<String> arg, String pName) {
		Log.debug("CommandDungeon.subFinalize");
		RetVal r = new RetVal();
		
		DungeonData d = null;
		if(arg.size() != 1){
			r.Err("Format is <Dungeon Name>!");
			r.status = false;
			return r;
		} 

		if(arg.get(0) == ""){
			r.Err("Format is <Dungeon Name>");
			return r;
		}

		d = DungeonManager.getDungeon(arg.get(0));
		
		if(d == null){
			r.Err("Couldn't find Dungeon '"+arg.get(0)+"'!");
			return r;
		}

		Log.debug("Getting Dungeon state.");
		if(d.state != dungeonState.EDITING && d.state != dungeonState.PREPPED){
			Log.debug("Dungeon is not in EDITING or PREPPED state!");
			r.Err("Dungeon needs to be in dungeon state EDITING or PREPPED, Dungeon is in state '"+dungeonState.toString(d.state)+"'!");
			return r;
		}
		
		Log.debug("Dungeon state IS valid - checking for edit instance.");
		InstanceData i = InstanceManager.getEditInstanceForDungeon(d.name);
		if(i == null && d.state == dungeonState.EDITING){
			Log.debug("Error : Dungeon '" +d.name+"' is in state EDITING, but no edit Instance found!");
			r.Err("Found dungeon in EDITING state, with no matching edit Instance - You may be losing data on the changes you made!!");
		}
		
		if(i != null){
			Log.debug("Instance is valid - checking instance state : " + instanceState.toString(i.state));
			if(i.state != instanceState.EDIT){
				Log.debug("Instance is not in state EDIT!");
				r.Err("Instance required to be in state EDIT! It's not!");
				//I'm pretty sure this isn't a critical error..and if it is, it will get caught
				//later on.
			}			
		
			//NOW we need to finish it up, and save out the schematic!
			Log.debug("Swapping edit schematic for main schematic!");
			RetVal rf = DungeonManager.applyEditSchematic(d);
			if(!rf.status){
				Log.error("Failed to swap out edit schematic to main schematic!");
				r.addAll(rf.message);
				return r;
			}
			Log.debug("Saving instance out.");
			rf = DungeonManager.saveSchematic(d);
			if(!rf.status){
				Log.error("Failed to save schematic for dungeon '"+d.name+"'!");
				r.addAll(rf.message);
				return r;
			} else {
				Log.debug("Save success!");
			}
		
			Log.debug("Instance is in EDIT state - unmounting region.");
			rf = InstanceManager.unmountRegion(i.name);
			if(!rf.status){
				r.addAll(rf.message);
				r.Err("Failed to unmount edit Instance for dungeon '"+d.name+"' at instance '"+i.name+"'!");
				i.state = instanceState.INVALID;
			} else {
				r.add("Edit Instance sucessfully removed from the world!");
				i.state = instanceState.RELEASED;
				Log.debug("Deleting instance data.");
				InstanceManager.delInstance(i.name);
			}
		}
		
		d.state = dungeonState.READY;
		r.add("Dungeon '" + d.name + "' has been finalized! It is now ready for instancing!");
		r.tru();
		return r;

	}

	private RetVal subDelete(ArrayList<String> arg, String pName) {
		// TODO Auto-generated method stub
		return null;
	}

	private RetVal subEdit(ArrayList<String> arg, String pName){
		Log.debug("CommandDungeon.subEdit");
		RetVal r = new RetVal();
		
		if(arg.isEmpty() || arg.get(0) == ""){
			r.Err("Usage is <Dungeon Name>!");
			return r;
		}
		
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		
		if(d == null){
			r.Err("Dungeon '"+arg.get(0)+"' not found.");
			return r;
		}
		
		//it might be in EDITING already if we had an issue - go ahead and allow editing of
		//dungeons in state EDITING.
		if(d.state != dungeonState.PREPPED && d.state != dungeonState.EDITING){
			r.Err("Dungeon is not in state PREPPED. Dungeon currently in state : " + dungeonState.toString(d.state));
			return r;
		}
		
		RetVal rf = InstanceManager.createAndMountEditInstance(d.name, pName);
		InstanceData i = (InstanceData)rf.retObj;
		
		if(!rf.status){
			r.addAll(rf.message);
			r.IntErr("Error mounting Edit-mode Instance");
			return r;
			
		} else {
			r.add("Teleporting you to your edit instance!");
			rf = InstanceManager.sendPlayerToInstance(pName, i);
			if(rf.status){
				r.tru();
				return r;
			} else {
				r.Err("An error occured teleporting you to your instance!");
				r.addAll(rf.message);
				return r;
			}
			//bridge.tpPlayer(pName, Config.dimension, i.getBounds().getCenter().getBlockX(), i.getBounds().getCenter().getBlockY(), i.getBounds().getCenter().getBlockZ(), 0, 0);
		}
	}
	
	private RetVal subSave(String sender){
		Log.info("Saving Dungeons at the request of "+sender+"!");
		return DungeonManager.saveDungeons();
	}
	
	private RetVal subCreate(ArrayList<String> arg, String playername){
		Log.debug("CommandDungeon.subCreate");
		RetVal r = new RetVal();
		
		String f = "";
		
		if(arg.size() == 0){
			r.Err("Error - Invalid number of arguments! Command is /" + Config.command + " dungeon create <dungeon name> [schematic template]!");
			return r;
		} else if(arg.size() == 2){
			f = arg.get(1);
		}
		if(arg.get(0) == ""){
			r.Err("Error - Invalid argument! Dungeon Name cannot be empty!");
			return r;
		}
		
		if(f != "" && !bridge.weSchematicExists(f)){
			//r.Err("Error - Invalid argument! Schematic Name cannot be empty!");
			//return r;
		//} else if(!bridge.weSchematicExists(f)){
			r.Err("Error - Invalid argument! Schematic file not found!");
			return r;
		} 
		
		RetVal rf = DungeonManager.createDungeon(arg.get(0));
		if(!rf.status){
			r.addAll(rf.message);
			return r;
		}
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		if(d == null){
			r.Err("ERROR : Failed to create new Dungeon!");
			r.addAll(rf.message);
			return r;
		}
		
		if(f != ""){
			//Command was run with optional schematic to import.
			Log.debug("Creating from Template.");
		} else {
			Selection s = InstancedDungeon.worldEdit.getSelection(bridge.getPlayer(playername));
			//CuboidSelection cs = (CuboidSelection) s;
			if(s == null){
				r.Err("You must make a WorldEdit selection - That area will be turned into a selection to use for the Dungeon!");
				DungeonManager.deleteDungeon(arg.get(0));
				return r;
			}
			//We need to pull the schematic out of the players WE selection.
			Log.debug("Creating dungeon from selection.");
			f = arg.get(0) + ".schematic";
			try {
				MCEditExtendedSchematicFormat.saveFromSelection(s, bridge.getWEditSchematic(f));
			} catch (IOException | DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.error("Failed to capture schematic for dungeon!");
				r.Err("Failed to capture schematic! :(");
				DungeonManager.deleteDungeon(arg.get(0));
				return r;
			}
		}
		
		d.templateLoc = f;
		//Load the schematic into the dungeon - very important!
		rf = DungeonManager.setTemplate(d);
		if(!rf.status){
			r.addAll(rf.message);
			DungeonManager.deleteDungeon(arg.get(0));
			return r;
		}

		Log.debug("WorldEdit Schematic '" + f + "' assigned to Dungeon '" + d.name +"'!");
		
		Log.debug("Prepping dungeon...");
		rf = DungeonManager.prepDungeon(d.name);
		if(!rf.status){
			r.addAll(rf.message);
			r.Err("Failed to create dungeon!");
		}
		
		Log.debug("Created new dungeon '" + d.name + "'!");
		r.add("Created new dungeon '" + d.name + "'!");
		r.tru();
		return r;

	}
	
	
	
	@Override
	public String getName() {
		return "dungeon";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("dungeon create <dungeon name> <template name>" + Config.bcol + "- Create a brand new dungeon and assigns a WorldEdit Schematic 'template' to a dungeon.");
		m.add(Config.ecol + "Warning : Do not use the above if your WorldEdit version does not save out NBT data properly!");
		m.add("dungeon create <dungeon name>" + Config.bcol + "- Create a brand new dungeon from a WE selection - Will create a new, NBT assured schematic called '<dungeon name>.schematic'.");
		m.add("dungeon prepare <dungeon name> " + Config.bcol + "- Clears out spawners and mobs, builds the instance walls - prepares dungeon for Editing");
		//m.add("dungeon entrance <dungeon name> " + Config.bcol + "- Sets the Spawn-in area for this dungeon. Required before readying!");
		m.add("dungeon save <dungeon name> " + Config.bcol + "- Saves the dungeon schematic to plugins/InstancedDungeon/schematics");
		m.add("dungeon edit <dungeon name> " + Config.bcol + "- Spawns in an editable instance for this dungeon. Records block breaks/placement. Allows Edit commands."); 
		m.add("dungeon finalize <dungeon name> " + Config.bcol + "- Finishes Edit mode (if in edit) on a dungeon, saves the edited schematic out, and marks it as ready to accept instancing.");				
		m.add("dungeon unready <dungeon name> " + Config.bcol + "- Returns a READY dungeon to PREPPED, so it can be modified.");
		m.add("dungeon delete <dungeon name> " + Config.bcol + "- Removes a dungeon from the registry, and removes all associated data from disk.");
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("dungeon " + Config.bcol + "- Commands relating to the manipulation of Dungeons.");
		return m;
	}

	@Override
	public String getPerm() {
		// TODO Auto-generated method stub
		return "instanceddungeon.dungeon";
	}

	@Override
	public boolean reqOp() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean reqConsole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowConsole() {
		// TODO Auto-generated method stub
		return true;
	}
}
