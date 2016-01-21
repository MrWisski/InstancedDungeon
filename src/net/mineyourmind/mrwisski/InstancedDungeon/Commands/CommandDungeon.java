package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
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
			r.addAll(getBriefHelp());
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
				return subCreate(arg);
			case "prep":
				arg.remove(0);
				return DungeonManager.prepDungeon(arg.get(0));
			case "entrance":
				arg.remove(0);
				return subEntrance(arg, pName);
			case "save":
				return subSave(arg.get(0));
			case "edit":
				arg.remove(0);
				return subEdit(arg.get(0), pName);
			case "finalize":
				arg.remove(0);
				DungeonData d = null;
				if(arg.size() != 1){
					r.Err("Format is <Dungeon Name>!");
					r.status = false;
					return r;
				} else {
					if(arg.get(0) == ""){
						r.Err("Format is <Dungeon Name>");
						return r;
					} else {
						d = DungeonManager.getDungeon(arg.get(0));
						if(d == null){
							r.Err("Couldn't find Dungeon '"+arg.get(0)+"'!");
							return r;
						}
					}
					d.state = dungeonState.READY;
					r.add("Dungeon '" + d.name + "' has been finalized! It is now ready for instancing!");
					return r;
				}
			default:
				Log.debug("default handler.");
				r.addAll(getBriefHelp());
				r.Err("Couldn't find command '"+arg.get(0)+"'!");
				r.status = false;
				return r;
		}
	}
	
	private RetVal subEdit(String arg, String pName){
		Log.debug("CommandDungeon.subEdit");
		RetVal r = new RetVal();
		
		if(arg == null || arg == ""){
			r.Err("Usage is <Dungeon Name>!");
			return r;
		}
		
		DungeonData d = DungeonManager.getDungeon(arg);
		
		if(d == null){
			r.Err("Dungeon '"+arg+"' not found.");
			return r;
		}
		
		if(d.state != dungeonState.PREPPED){
			r.Err("Dungeon is not in state PREPPED. Dungeon currently in state : " + dungeonState.toString(d.state));
			return r;
		}
		
		RetVal rf = InstanceManager.createInstance(pName, d.name, true);
		
		if(rf.retObj == null || !(rf.retObj instanceof InstanceData) || !rf.status){
			r.addAll(rf.message);
			r.IntErr("Error creating Edit-mode Instance");
			return r;
		}
		
		InstanceData i = (InstanceData)rf.retObj;
		
		rf = InstanceManager.mountRegion(i.name);
		
		if(!rf.status){
			r.addAll(rf.message);
			r.IntErr("Error mounting Edit-mode Instance");
			return r;
			
		}
		
		EditSession es = InstancedDungeon.instance.worldEdit.createEditSession(bridge.getPlayer(pName));
		
		
		return r;
	}
	
	private RetVal subEntrance(ArrayList<String> arg, String pName){
		Log.debug("CommandDungeon.subEntrance");
		RetVal r = new RetVal();

		arg.remove(0); // remove entrance
		
		if(arg.isEmpty()){
			r.Err(Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon entrance <dungeon name> (while standing at the warp-in point)!");
			return r;
		}
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		if(d == null){
			r.Err(Config.ecol + "Error - Invalid argument! '" + arg.get(0) + "' not a valid Dungeon!");
			return r;
		} 
		Location l = bridge.getPlayerLoc(pName);
		
		d.setSpawn(l.getPosition().getBlockX(),l.getPosition().getBlockY(),l.getPosition().getBlockZ(),l.getYaw(), l.getPitch());
		r.add("Successfully added spawn in location to Dungeon '"+arg.get(0)+"'");
		r.tru();
		
		return r;
	
	}
	
	
	private RetVal subSave(String sender){
		Log.info("Saving Dungeons at the request of "+sender+"!");
		return DungeonManager.saveDungeons();
	}
	
	private RetVal subCreate(ArrayList<String> arg){
		Log.debug("CommandDungeon.subCreate");
		RetVal r = new RetVal();

		if(arg.size() < 2){
			r.Err("Error - Invalid number of arguments! Command is /" + Config.command + " dungeon create <dungeon name> <schematic template>!");
			return r;
		}
		if(arg.get(0) == ""){
			r.Err("Error - Invalid argument! Dungeon Name cannot be empty!");
			return r;
		}
		String f = arg.get(1);
		if(f == ""){
			r.Err("Error - Invalid argument! Schematic Name cannot be empty!");
			return r;
		} else if(!bridge.weSchematicExists(f)){
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
		
		d.templateLoc = f;
		//Load the schematic into the dungeon - very important!
		rf = DungeonManager.setTemplate(d);
		if(!rf.status){
			r.addAll(rf.message);
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
		m.add("Allows creation and manipulation of Dungeons.");
		m.add(Config.bcol + "First, 'create' a new dungeon. This assigns a template that you can edit to personalize each instance.");
		m.add(Config.bcol + "Then 'prep' the dungeon - this clears out anything that won't copy (spawners), removes mobs, and constructs a shell around the template. At this point, a Worldedit Schematic of the prepped dungeon will be saved, and further editing will work from this schematic!");
		m.add(Config.bcol + "Once you 'save' the dungeon, you're ready to start editing!");
		m.add(Config.bcol + "In 'edit' mode, you'll get an edit-instance for your dungeon - its NOT ready for use yet. Stand where you want players to spawn in, and 'setspawn'.");
		m.add(Config.bcol + "When you're finished, just 'finalize' the dungeon, and it will be ready to accept instancing!");
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("dungeon " + Config.bcol + "- Shows the dungeon setup help screen.");
		m.add("dungeon create <dungeon name> <template name>" + Config.bcol + "- Create a brand new dungeon and assigns a WorldEdit Schematic 'template' to a dungeon. Required before prepping!."); 
		m.add("dungeon prep <dungeon name> " + Config.bcol + "- Clears out spawners and mobs, builds the instance walls. Required before editing!");
		m.add("dungeon entrance <dungeon name> " + Config.bcol + "- Sets the Spawn-in area for this dungeon. Required before readying!");
		m.add("dungeon save <dungeon name> " + Config.bcol + "- Saves out a new WorldEdit schematic, ready for tailoring to your needs! You can find this schematic in /InstancedDungeon/schematics");
		m.add("dungeon edit <dungeon name> " + Config.bcol + "- Spawns in an edit-instance for this dungeon."); 
		m.add("dungeon finalize <dungeon name> " + Config.bcol + "- Finishes Edit mode (if in edit) on a dungeon, and marks it as ready to accept instancing.");				
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
