package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandReload implements CommandFunctor {
	private FunctionsBridge bridge;
	
	CommandReload(FunctionsBridge bridge){
		this.bridge = bridge;
	}
	
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public RetVal execute(ArrayList<String> args, String pName) {
		RetVal r = new RetVal();
		r.add("Reloading Plugin and all configs.\n");
		boolean status = bridge.ConfigReload();
		if(status){
			r.add("Plugin and all configs reloaded successfully!\n");
		} else {
			r.Err("There seems to have been an error - Please check console/server log!");
		}
		return r;
	}

	@Override
	public String getPerm() {
		return "instanceddungeon.reload";
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

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("This command will purge current data, disable the plugin, and then re-enable it - Please bear in mind any changes made in the config since this plugin was loaded WILL BE LOST.");
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("reload " + Config.bcol + "- Reloads configuration from disk.");
		return m;
	}
	

}
