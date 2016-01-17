package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;

public class CommandReload extends CommandFunctor {
	private String message = "No Message.";
	private FunctionsBridge bridge;
	
	CommandReload(FunctionsBridge bridge){
		this.bridge = bridge;
	}
	
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public boolean execute(String[] args, String pName) {
		message = "Reloading Plugin and all configs.\n";
		boolean status = bridge.ConfigReload();
		if(status){
			message += "Plugin and all configs reloaded successfully!\n";
		} else {
			message += "There seems to have been an error - Please check console/server log!";
		}
		return status;
	}

	@Override
	public String getMessage() {
		return message;
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
	public String getFullHelp() {
		return "This command will purge current data, disable the plugin, and then re-enable it - Please bear in mind any changes made in the config since this plugin was loaded WILL BE LOST.";
	}

	@Override
	public String getBriefHelp() {
		return "reload " + Config.bcol + "- Reloads configuration from disk.";
	}
	

}
