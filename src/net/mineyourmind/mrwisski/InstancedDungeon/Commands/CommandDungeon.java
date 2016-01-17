package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;

public class CommandDungeon extends CommandFunctor {
	private String message = "No Message.";
	private FunctionsBridge bridge = null;
	private Logger log = null;
	
	public CommandDungeon(FunctionsBridge bridge){
		DungeonManager.getInstance();
		log = InstancedDungeon.getInstance().getLogger();
		this.bridge = bridge;
	}
	
	@Override
	public boolean execute(String[] args, String pName) {
		String m = "";
		ArrayList<String> arg = new ArrayList<String>();
		for(String s : args){
			m += s + " "; 
			arg.add(s);
		}
		log.info("Execute : " + m);
		
		arg.remove(0);	//Remove 'dungeon' from the arguments list.
		if(arg.isEmpty()){
			log.info("arg is empty.");
			message = getBriefHelp();
			return true;
		}
		switch(arg.get(0)){
			case "create":
				arg.remove(0);
				return subCreate(arg);
			case "define":
				arg.remove(0);
				return subDefine(arg);
			case "prep":
				break;
			case "entrance":
				arg.remove(0);
				return subEntrance(arg, pName);
			case "save":
				return subSave();
			case "edit":
				break;
			default:
				log.info("default handler.");
				message = getBriefHelp();
				break;
		}
		
		// TODO Auto-generated method stub
		
		return true;
	}
	
	private boolean subEntrance(ArrayList<String> arg, String pName){
		if(arg.isEmpty()){
			message = Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon entrance <dungeon name> (while standing at the warp-in point)!";
			return false;
		}
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		if(d == null){
			message = Config.ecol + "Error - Invalid argument! '" + arg.get(0) + "' not a valid Dungeon!";
			return false;
		} 
		Location l = bridge.getPlayerLoc(pName);
		
		return false;
		
		//TODO - Finish this!
		
	}
	private boolean subSave(){
		message = "Saving Dungeons!";
		return DungeonManager.saveDungeons();
	}
	private boolean subCreate(ArrayList<String> arg){
		log.info("subCreate");
		if(arg.isEmpty()){
			message = Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon create <dungeon name>!";
			return false;
		}
		DungeonData d = DungeonManager.createDungeon(arg.get(0));
		message = DungeonManager.message;
		log.info("Message from dungeon Manager : " + message);
		return (d != null);
	}
	
	private boolean subDefine(ArrayList<String> arg){
		log.info("subDefine");
		if(arg.size() != 2){
			message = Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon define <dungeon name> <schematic name>!";
			return false;
		}
		DungeonData d = DungeonManager.getDungeon(arg.get(0));
		if(d == null){
			message = Config.ecol + "Error - Invalid argument! Dungeon '" + arg.get(0) + "' not found!";
			return false;
		}
		String f = arg.get(1);
		if(f == ""){
			message = Config.ecol + "Error - Invalid argument! Schematic Name cannot be empty!";
			return false;
		} else if(!f.endsWith(".schematic")){
			message = Config.ecol + "Error - Invalid argument! Schematic Name must end in .schematic!";
			return false;
		} else if(!bridge.weSchematicExists(f)){
			message = Config.ecol + "Error - Invalid argument! Schematic file not found!";
			return false;
		} else {
			d.setTemplateLoc(f);
			DungeonManager.setDungeon(d);
			message = "WorldEdit Schematic '" + f + "' assigned to Dungeon '" + d.getName() +"'!";
			return true;
		}
	}

	@Override
	public String getName() {
		return "dungeon";
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getFullHelp() {
		return 	"Allows creation and manipulation of Dungeons.\n\n" +
				Config.header + Config.bcol + " First, 'create' a new dungeon.\n" +
				Config.header + Config.bcol + " Next, 'define' the template to use. This is just a filename in worldedit's schematic directory!" + 
				Config.header + Config.bcol + " Then 'prep' the dungeon - this clears out anything that won't copy (spawners), removes mobs, and constructs a shell around the template." +
				Config.header + Config.bcol + " Once you 'save' the dungeon, you're ready to start editing!\n\n" +
				Config.header + Config.bcol + " In 'edit' mode, your actions ";
	}

	@Override
	public String getBriefHelp() {
		return 	"dungeon " + Config.bcol + "- Shows the dungeon setup help screen.\n" + 
				Config.header + " dungeon create <dungeon name> " + Config.bcol + "- Create a brand new dungeon.\n" + 
				Config.header + " dungeon define <dungeon name> <template name> " + Config.bcol + "- Assigns a WorldEdit Schematic to a dungeon. Required before use!\n" +
				Config.header + " dungeon prep <dungeon name> " + Config.bcol + "- Clears out spawners and mobs, builds the instance walls. Required before use!\n" + 
				Config.header + " dungeon entrance <dungeon name> " + Config.bcol + "- Sets the Spawn-in area for this dungeon.\n" +
				Config.header + " dungeon save <dungeon name> " + Config.bcol + "- Saves out a new WorldEdit schematic, ready for tailoring to your needs! You can find this schematic in /InstancedDungeon/schematics\n" +
				Config.header + " dungeon edit <dungeon name> " + Config.bcol + "- Spawns in an instance \n";				
	}

	@Override
	public String getPerm() {
		// TODO Auto-generated method stub
		return "instanceddungeon.dungeon";
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
		return false;
	}
}
