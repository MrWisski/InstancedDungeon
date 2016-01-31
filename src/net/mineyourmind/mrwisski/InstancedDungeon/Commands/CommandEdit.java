package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandEdit implements CommandFunctor {
	FunctionsBridge bridge = InstancedDungeon.getInstance();
	
	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		Log.debug("CommandEdit.execute");
		RetVal r = new RetVal();
		
		arg.remove(0);	//Remove 'edit' from the arguments list.
		
		if(arg.isEmpty()){
			Log.debug("arg is empty.");
			r.addAll(getFullHelp());
			return r;
		}
		
		switch(arg.get(0)){
			case "help":
				r.addAll(getFullHelp());
				r.tru();
				return r;
			case "entrance":
				arg.remove(0);
				return subEntrance(arg, pName);
			default:
				r.addAll(getFullHelp());
				r.Err("Couldn't find command '"+arg.get(0)+"'!");
				return r;
		}
	}

	private RetVal subEntrance(ArrayList<String> arg, String pName){
		Log.debug("CommandDungeon.subEntrance");
		RetVal r = new RetVal();

		if(arg.isEmpty()){
			r.Err(Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon entrance <dungeon name> (while standing at the warp-in point)!");
			return r;
		}
		
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		if(d == null){
			r.Err(Config.ecol + "Error - Invalid argument! '" + arg.get(0) + "' not a valid Dungeon!");
			return r;
		}
		
		InstanceData i = InstanceManager.getEditInstanceForDungeon(d.name);
		
		if(i == null){
			r.Err(Config.ecol + "Error - Invalid argument! '" + arg.get(0) + "' does not have an edit Instance!");
			return r;
		}
		
		Location l = bridge.getPlayerLoc(pName);
		Vector v = l.getPosition();
		Vector vOff = v.subtract(i.getBounds().getMinimumPoint());
		
		d.setSpawn(vOff.getBlockX(),vOff.getBlockY(),vOff.getBlockZ(),l.getYaw(), l.getPitch());
		
		r.add("Successfully added spawn in location to Dungeon '"+arg.get(0)+"'");
		
		r.tru();
		
		return r;
	
	}
	
	@Override
	public String getName() {
		return "edit";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("edit setspawn " + Config.bcol + "- Configures the spawn in point for the Dungeon when Instanced.");
		m.add("edit setarea " + Config.bcol + "- Changes the Dungeon's size to your current worldedit selection.");
		m.add("edit setspawner <Mob Name>" + Config.bcol + "- Sets the spawner you're looking at to <Mob Name>.");
		m.add("edit save " + Config.bcol + "- Saves the current state of the edit schematic.");
		m.add("edit exit " + Config.bcol + "- Exits and removes the current edit instance without saving the edit data.");
		
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("edit " + Config.bcol + "- Implements Commands to be used while editing a Dungeon.");

		return m;
	}

	@Override
	public String getPerm() {
		return "instanceddungeon.edit";
	}

	@Override
	public boolean reqOp() {
		return true;
	}

	@Override
	public boolean reqConsole() {
		return false;
	}

	@Override
	public boolean allowConsole() {
		return false;
	}

}
