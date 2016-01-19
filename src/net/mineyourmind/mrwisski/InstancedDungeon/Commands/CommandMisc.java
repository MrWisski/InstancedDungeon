package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;

public class CommandMisc extends CommandFunctor {
	String message = "No Message.";
	FunctionsBridge bridge = null;
	
	CommandMisc(FunctionsBridge b){
		bridge = b;
	}
	
	@Override
	public boolean execute(String[] args, String pName) {
		if(args.length == 0){message = this.getBriefHelp();}
		int c = 0;
		for(String s : args){
			InstancedDungeon.Log.info(c + " : " + s);
		}
		FunctionsBridge.itemInfo i = bridge.getPlayerItemInHand(pName);
		message = "Item in hand : " + i.name + "x" + i.count + " :: " + i.material;
		return true;
	}

	@Override
	public String getName() {
		return "misc";
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getFullHelp() {
		return "Just some misc commands to make life easier.";
	}

	@Override
	public String getBriefHelp() {
		return "itemhand - Get information about the item in your hand!";
	}

	@Override
	public String getPerm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean reqOp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reqConsole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowConsole() {
		// TODO Auto-generated method stub
		return false;
	}

}
