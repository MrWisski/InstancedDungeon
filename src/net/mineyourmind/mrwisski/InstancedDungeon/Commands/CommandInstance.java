package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData.instanceState;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;

public class CommandInstance extends CommandFunctor {
	public Logger log = InstancedDungeon.Log;
	
	@Override
	public boolean execute(String[] arguments, String pName) {
		String m = "";
		ArrayList<String> arg = new ArrayList<String>();
		int c = 0;
		for(String s : arguments){
			m += s + " ";
			log.info(c + " : " + s);
			c++;
			arg.add(s);
		}
		log.info("CommandInstance Execute : " + m);
		
		arg.remove(0);	//Remove 'instance' from the arguments list.
		if(arg.isEmpty()){
			log.info("arg is empty.");
			message = getBriefHelp();
			return true;
		}
		
		switch(arg.get(0)){
			case "create":
				arg.remove(0);
				return subCreate(arg, pName);
			default:
				message = getBriefHelp();
		}
		return false;
	}

	private boolean subCreate(ArrayList<String> arg, String sender){
		log.info("instance subCreate");
		if(arg.size() == 1){
			log.info("arg.size == 1");
			//Player creating own dungeon
			InstanceData i = InstanceManager.createInstance(sender, arg.get(0), false);
			if(i == null){
				log.severe("Failed to create instance! IM : " + InstanceManager.message);
				return false;
			} else {
				log.info("Created InstanceData.");
			}
			InstanceManager.mountRegion(i.name);
			if(i.state == instanceState.READY){
				log.info("Instance State : " + i.getStatusDisplay());
				message = "New Instance successfully created and mounted! Instance '" + i.name + "' is up and running!";
				InstanceManager.sendPlayerToInstance(sender, i);
				return true;
			} else {
				log.info("Instance State : " + i.getStatusDisplay());
				message = "Something went wrong : IM = " + InstanceManager.message;
				return false;
			}
		} else if(arg.size() == 2){
			//Player creating other dungeon
			log.info("arg.size == 2");
		} else {
			message = Config.ecol + "Error - Invalid number of parameters! Usage is <Dungeon Name> or <Dungeon Name> <Player Name>!";
			return false;
		}
		
		return true;
		
	}
	
	
	@Override
	public String getName() {
		return "instance";
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getFullHelp() {
		return "Commands for interacting with a Dungeon Instance.\n";
	}

	@Override
	public String getBriefHelp() {
		return 	"list " + Config.bcol + "- Displays a list of all current Dungeon Instances.\n" +
				Config.header + "instance create <Dungeon Name> " + Config.bcol + "- Creates a new Dungeon Instance, and teleports you to it as the Owner!\n"+
				Config.header + "instance create <Dungeon Name> <Player Name>" + Config.bcol + "- Creates a new Dungeon Instance, and teleports the specificied player to it as the Owner!\n"+				
				Config.header + "instance unmount <Dungeon Name> " + Config.bcol + "- Removes an existing Dungeon Instance from the server, and teleports you to it as the Owner!\n"+
				Config.header + "instance portal <Dungeon Name> " + Config.bcol + "- Creates a portal " +Config.ecol+ "from a worldedit selection"+Config.bcol+" that acts like 'instance create' on any player that goes through it.\n"+
				Config.header + "instance removeportal <Dungeon Name> " + Config.bcol + "- Removes a portal" +Config.ecol+ "using a worldedit selection"+Config.bcol+".\n" +
				"";
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
