package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandMisc implements CommandFunctor {
	String message = "No Message.";
	FunctionsBridge bridge = null;
	
	CommandMisc(FunctionsBridge b){
		bridge = b;
	}
	
	@Override
	public String getName() {
		return "misc";
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

	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		RetVal r = new RetVal();
		FunctionsBridge.itemInfo i = bridge.getPlayerItemInHand(pName);
		message = "Item in hand : " + i.name + "x" + i.count + " :: " + i.material;
		return r;
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("Just some misc functions that don't fit anywhere else!");
		// TODO Auto-generated method stub
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("misc itemhand " + Config.bcol + "- returns the material name of the item in your hand!");
		return m;
	}

}
