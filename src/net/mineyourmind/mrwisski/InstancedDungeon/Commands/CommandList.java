package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.Set;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;

public class CommandList extends CommandFunctor {
	private String message = "No Message.";
	
	CommandList(){
		
	}
	
	@Override
	public boolean execute(String[] args, String pName) {
		message = Config.header + " Dungeons : \n";
		DungeonManager.getInstance();
		InstanceManager.getInstance();
		
		Set<String> k = DungeonManager.getDungeonList();
		for(String S : k){
			message += Config.header + " " + DungeonManager.getDungeon(S).getStatusDisplay() + "\n";
		}
		message += "\n" + Config.header + " Instances : \n";
		k = InstanceManager.getInstanceList();
		for(String S : k){
			InstanceData i = InstanceManager.getInstance(S);
			message += Config.header + " " + InstanceManager.getInstance(S).getStatusDisplay() + "\n";
		}
		return true;
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getFullHelp() {
		return 	"Lists various things : \n" + 
				"list dungeon : Lists all created dungeons, and displays status information on each.\n" + 
				"list instance : Lists all valid instances, and displays status information on each.";
	}

	@Override
	public String getBriefHelp() {
		return 	"list dungeon " + Config.bcol + "- Lists dungeon info\n" + 
				"list instance " + Config.bcol + "- Lists instances info\n";
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
		return true;
	}

}
