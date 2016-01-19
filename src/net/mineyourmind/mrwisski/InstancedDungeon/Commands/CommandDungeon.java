package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.sk89q.worldedit.Vector;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
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
		log.info("CommandDungeon Execute : " + m);
		
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
			case "prep":
				break;
			case "entrance":
				arg.remove(0);
				return subEntrance(arg, pName);
			case "save":
				return subSave();
			case "edit":
				break;
			case "test":
				arg.remove(0);
				log.info("DM message : " + DungeonManager.test(arg.get(0)));
				message = "Done!";
				break;
			case "pasteT":
				arg.remove(0);
				if(arg.size() != 4){
					message = Config.ecol + "Format is <Dungeon Name> <Paste X> <Paste Y> <Paste Z>!";
				} else {
					DungeonManager.pasteTemplate(arg.get(0), new Vector(Integer.decode(arg.get(1)),Integer.decode(arg.get(2)),Integer.decode(arg.get(3))));
					message = DungeonManager.message;
				}
				
				break;
			case "pasteS":
				arg.remove(0);
				
				if(arg.size() != 4){
					message = Config.ecol + "Format is <Dungeon Name> <Paste X> <Paste Y> <Paste Z>!";
				} else {
					DungeonManager.pasteSchematic(arg.get(0), new Vector(Integer.decode(arg.get(1)),Integer.decode(arg.get(2)),Integer.decode(arg.get(3))));
					message = DungeonManager.message;
					
				}
				
				break;
			case "finalize":
				arg.remove(0);
					
				if(arg.size() != 1){
					message = Config.ecol + "Format is <Dungeon Name>!";
					return false;
				} else {
					DungeonData d = DungeonManager.getDungeon(arg.get(0));
					d.state = dungeonState.READY;
					message = Config.tcol + "Dungeon '" + d.name + "' has been finalized! It is now ready for instancing!";
				}
				
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
		if(arg.size() < 2){
			message = Config.ecol + "Error - Invalid number of arguments! Command is /" + Config.command + " dungeon create <dungeon name> <schematic template>!";
			return false;
		}
		if(arg.get(0) == ""){
			message = Config.ecol + "Error - Invalid argument! Dungeon Name cannot be empty!";
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
	
		}
		DungeonData d = DungeonManager.createDungeon(arg.get(0));
		if(d == null){
			message = Config.ecol + " ERROR : Couldn't create dungeon! DungeonManager message : " + DungeonManager.message;
			return false;
		}
		d.templateLoc = f;
		//Load the schematic into the dungeon - very important!
		DungeonManager.setTemplate(d);
		DungeonManager.setDungeon(d);
		message = DungeonManager.message + "\nWorldEdit Schematic '" + f + "' assigned to Dungeon '" + d.name +"'!";
		
		log.info("Prepping dungeon...");
		DungeonManager.prepDungeon(d.name);
		log.info("message from DM : " + DungeonManager.message);
		message = "Created new dungeon '" + d.name + "'!";
		
		return true;
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
				Config.header + Config.bcol + " First, 'create' a new dungeon. This assigns a template that you can edit to personalize each instance.\n" +
				Config.header + Config.bcol + " Then 'prep' the dungeon - this clears out anything that won't copy (spawners), removes mobs, and constructs a shell around the template. At this point, a Worldedit Schematic of the prepped dungeon will be saved, and further editing will work from this schematic!\n" +
				Config.header + Config.bcol + " Once you 'save' the dungeon, you're ready to start editing!\n" +
				Config.header + Config.bcol + " In 'edit' mode, you'll get an edit-instance for your dungeon - its NOT ready for use yet. Stand where you want players to spawn in, and 'setspawn'.\n" +
				Config.header + Config.bcol + " When you're finished, just 'finalize' the dungeon, and it will be ready to accept instancing!";
	}

	@Override
	public String getBriefHelp() {
		return 	"dungeon " + Config.bcol + "- Shows the dungeon setup help screen.\n" + 
				Config.header + " dungeon create <dungeon name> <template name>" + Config.bcol + "- Create a brand new dungeon and assigns a WorldEdit Schematic 'template' to a dungeon. Required before prepping!.\n" + 
				Config.header + " dungeon prep <dungeon name> " + Config.bcol + "- Clears out spawners and mobs, builds the instance walls. Required before editing!\n" + 
				Config.header + " dungeon entrance <dungeon name> " + Config.bcol + "- Sets the Spawn-in area for this dungeon. Required before readying!\n" +
				Config.header + " dungeon save <dungeon name> " + Config.bcol + "- Saves out a new WorldEdit schematic, ready for tailoring to your needs! You can find this schematic in /InstancedDungeon/schematics\n" +
				Config.header + " dungeon edit <dungeon name> " + Config.bcol + "- Spawns in an edit-instance for this dungeon.\n" + 
				Config.header + " dungeon finalize <dungeon name> " + Config.bcol + "- Finishes Edit mode (if in edit) on a dungeon, and marks it as ready to accept instancing.\n";				
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
		return true;
	}
}
