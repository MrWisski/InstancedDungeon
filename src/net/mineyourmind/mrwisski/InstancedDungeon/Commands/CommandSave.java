package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;

public class CommandSave extends CommandFunctor {
	private String message = "No Message.";
	private FunctionsBridge bridge = null;
	public CommandSave(FunctionsBridge bridge){
		this.bridge = bridge;
	}
	
	@Override
	public boolean execute(String[] args, String pName) {
		message = "Saving Dungeons...\n";
		boolean ret = DungeonManager.saveDungeons();
		if(ret){
			message += "Success!";
		} else {
			message += "Failure? Check console!";
		}
		return ret;
	}

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}

	@Override
	public String getFullHelp() {
		return "Saves out Dungeon Instance settings and configuration files to disk.";
	}

	@Override
	public String getBriefHelp() {
		// TODO Auto-generated method stub
		return "save " + Config.bcol + "- Saves current settings to disk.";
	}

	@Override
	public String getPerm() {
		// TODO Auto-generated method stub
		return "instanceddungeon.reload";
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
