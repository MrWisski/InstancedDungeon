package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

public class CommandEdit implements CommandFunctor {

	@Override
	public RetVal execute(ArrayList<String> arg, String pName) {
		Log.debug("CommandEdit.execute");
		RetVal r = new RetVal();
		
		arg.remove(0);	//Remove 'edit' from the arguments list.
		if(arg.isEmpty()){
			Log.debug("arg is empty.");
			r.addAll(getBriefHelp());
			return r;
		}
		
		switch(arg.get(0)){
			case "help":
				r.addAll(getFullHelp());
				r.tru();
				return r;
			default:
				r.addAll(getBriefHelp());
				return r;
		}
	}

	@Override
	public String getName() {
		return "edit";
	}

	@Override
	public ArrayList<String> getFullHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("Implements a number of commands to get your Dungeon fixed, and ready for instancing!");
		return m;
	}

	@Override
	public ArrayList<String> getBriefHelp() {
		ArrayList<String> m = new ArrayList<String>();
		m.add("edit help " + Config.bcol + "- Shows the full help page on using Edit.");
		m.add("edit setspawn " + Config.bcol + "- Configures the spawn in point for the Dungeon.");
		m.add("edit setarea " + Config.bcol + "- Applies the current selection to the Dungeon's template size");
		m.add("edit setspawner <Mob Name>" + Config.bcol + "- Sets the spawner you're looking at to <Mob Name>");
		m.add("edit help " + Config.bcol + "- ");
		m.add("edit help " + Config.bcol + "- ");
		m.add("edit save " + Config.bcol + "- Saves the current state of the template.");
		m.add("edit finalize " + Config.bcol + "- Saves the current state of the template, finalizes it, and removes the Edit Instance.");
		m.add("edit exit " + Config.bcol + "- Exits and removes the current edit instance.");
		
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
