package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.Set;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandList implements CommandFunctor {
	
	CommandList(){
		
	}
	
	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		Log.debug("CommandList.execute");
		RetVal r = new RetVal();
		
		r.add("Dungeons :");
		DungeonManager.getInstance();
		InstanceManager.getInstance();
		
		Set<String> k = DungeonManager.getDungeonList();
		for(String S : k){
			r.add(DungeonManager.getDungeon(S).getStatusDisplay());
		}
		r.add("Instances :");
		k = InstanceManager.getInstanceList();
		for(String S : k){
			r.add(InstanceManager.getInstance(S).getStatusDisplay());
		}
		r.tru();
		return r;
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		return new ArrayList<String>();
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("list " + Config.bcol + "- Lists all available dungeons and instances.");
		return 	m;
	}

	@Override
	public String getPerm() {
		return "instanceddungeon.list";
	}

	@Override
	public boolean reqOp() {
		return false;
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
