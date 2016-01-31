package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandInstance implements CommandFunctor {
	
	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		Log.debug("CommandInstance.execute");
		RetVal r = new RetVal();
		
		arg.remove(0);	//Remove 'instance' from the arguments list.
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
			case "create":
				arg.remove(0);
				return subCreate(arg, pName);
			case "remove":
				return InstanceManager.unmountRegion(arg.get(1));
			default:
				r.addAll(getFullHelp());
				r.Err("Couldn't find command '"+arg.get(0)+"'!");
				return r;
		}
	}

	private RetVal subCreate(ArrayList<String> arg, String sender){
		Log.debug("CommandInstance.subCreate");
		RetVal r = new RetVal();
		
		if(arg.size() == 1){
			Log.debug("arg.size == 1");
			//Player creating own instance
			
			return InstanceManager.createAndMountInstance(arg.get(0), sender, false);
			
		} else if(arg.size() == 2){
			//Player creating another player's instance
			Log.info("arg.size == 2");
			
			return InstanceManager.createAndMountInstance(arg.get(0), arg.get(1), false);
			
			
		} else {
			r.Err("Error - Invalid number of parameters! Usage is <Dungeon Name> or <Dungeon Name> <Player Name>!");
			return r;
		}
		
	}
	
	
	@Override
	public String getName() {
		return "instance";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("instance create <Dungeon Name> " + Config.bcol + "- Creates a new Dungeon Instance, and teleports you to it as the Owner!");
		m.add("instance create <Dungeon Name> <Player Name>" + Config.bcol + "- Creates a new Dungeon Instance, and teleports the specificied player to it as the Owner!");				
		m.add("instance unmount <Instance Name> " + Config.bcol + "- Removes an existing Dungeon Instance from the server, and teleports you to it as the Owner!");
		m.add("instance portal <Dungeon Name> " + Config.bcol + "- Creates a portal " +Config.ecol+ "from a worldedit selection"+Config.bcol+" that acts like 'instance create' on any player that goes through it.");
		m.add("instance removeportal <Dungeon Name> " + Config.bcol + "- Removes a portal" +Config.ecol+ "using a worldedit selection"+Config.bcol+".");
		m.add("instance remove <Instance Name> " + Config.bcol + "- Removes a specific Instance - All players inside will be teleported to spawn, the blocks removed, and the Instance flagged as RELEASED.");
		
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("instance " + Config.bcol + "- Commands relating to the creation and manipulation of Instances.");

		return m;
		//Config.header + "" + Config.bcol + "- " +;
	}

	@Override
	public String getPerm() {
		return "instanceddungeon.instance";
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
