package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandSave implements CommandFunctor {
	
	public CommandSave(FunctionsBridge bridge){
	}
	
	@Override
	public RetVal execute(ArrayList<String> args, String pName) {
		RetVal r = new RetVal();
		
		r.add("Saving Dungeons...");
		RetVal ret = DungeonManager.saveDungeons();
		if(ret.status){
			r.add("Success!");
		} else {
			r.Err("Failure? Check console!");
		}
		r.add("Saving Instances...");
		ret = InstanceManager.saveInstances();
		if(ret.status){
			r.add("Success!");
		} else {
			r.Err("Failure? Check console!");
		}
		
		return r;
	}

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("Saves out Dungeon & Instance settings files to disk.");
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("save " + Config.bcol + "- Saves current settings to disk.");
		return m;
	}

	@Override
	public String getPerm() {
		return "instanceddungeon.save";
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
		return true;
	}

}
