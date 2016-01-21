package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.mineyourmind.mrwisski.InstancedDungeon.FunctionsBridge;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

//TODO : We'll Need a sponge-style Command Handler!

/** Bukkit Command Handler
 * 
 * @author MrWisski
 * @version 0.0.1a 17 Jan 2016 - Initial code.
 *
 */
public class HandleCommand implements CommandExecutor {
	Server server = null;
	private HashMap <String, CommandFunctor> commands = new HashMap<String, CommandFunctor>();
	
	CommandReload reload = null;
	CommandSave save = null;
	CommandDungeon dungeon = null;
	CommandList list = null;
	CommandMisc misc = null;
	CommandInstance instance = null;
	
	
	public HandleCommand(FunctionsBridge bridge){
		server = Bukkit.getServer();
		
		reload = new CommandReload(bridge);
		commands.put(reload.getName(), reload);
		save = new CommandSave(bridge);
		commands.put(save.getName(), save);
		dungeon = new CommandDungeon(bridge);
		commands.put(dungeon.getName(), dungeon);
		list = new CommandList();
		commands.put(list.getName(), list);
		misc = new CommandMisc(bridge);
		commands.put(misc.getName(), misc);
		instance = new CommandInstance();
		commands.put(instance.getName(), instance);
	}
	
	private boolean canRun(CommandSender sender, CommandFunctor C){
		//Check op.
		if(C.reqOp() && !sender.isOp()) return false;
		//Check console
		if(C.reqConsole() && !(server.getConsoleSender() == sender)) return false;
		//Can console run this command?
		if((C.allowConsole() == false) && (server.getConsoleSender() == sender)) return false;
		//Does it require a permission? does sender have that permission?
		if(C.getPerm() != null && !sender.hasPermission(C.getPerm())) return false;
			
		return true;
	}
	
	private void sendMessage(CommandSender to, ArrayList<String> message){
		for(String s : message){
			to.sendMessage(Config.header + Config.tcol + " " + s);
		}
	}
	
	private void showHelp(CommandSender sender, String[] args){
		ArrayList<String> message = new ArrayList<String>();
		boolean showMain = true;
		//Is player looking for help on a specific command?
		if(args != null && args.length > 1){
			//Do we know what command he's trying to get help with?
			String sub = args[1].toLowerCase();
			if(commands.containsKey(sub)){
				//Is it a command this sender has access to?
				if(canRun(sender, commands.get(sub))){
					message.addAll(commands.get(sub).getFullHelp());
					showMain = false;
				} else {
					//Nope, sender cannot access, so go ahead and send the default help.
				}
			} 
		}
		if(showMain){
			//Build the main help index.
			
			message.add("help " + Config.bcol + "- This screen");
			message.add("help <command> " + Config.bcol + "- Get help on a specific command.");
			//Add in our commands help, if the sender can run them.
			for(String N : commands.keySet()){
				if(canRun(sender,commands.get(N))) {
					message.addAll(commands.get(N).getBriefHelp());
				}
			}			
		}
		
		sendMessage(sender, message);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				
		if (cmd.getName().equals(Config.command)) {
			//Send basic help if no arguments
			if (args == null || args.length == 0) {
				showHelp(sender, args);
				return true;
			}

			if(args[0] != null && args[0].toLowerCase() == "help"){
				showHelp(sender, args);
				return true;
			}
			
			String sub = args[0].toLowerCase();
			ArrayList<String> arg = Util.argsToList(args);
			
			//Do we have this command? Can the sender run it?
			if(commands.containsKey(sub) && canRun(sender,commands.get(sub))){
				
				//Execute the command.
				RetVal r = commands.get(sub).execute(arg,sender.getName());
				//Send the sender the results of this operation.
				sendMessage(sender,r.message);
			} else if(commands.containsKey(sub)){
				
				if(sender == server.getConsoleSender()){
					//Inform console that this command requires an in-game player.
					sender.sendMessage(Config.header + Config.ecol + " Sorry, Console, but this command requires an in-game player!");
				} else {
					//Show the help! but without arguments, dont want people peeking!
					showHelp(sender,null);
				}
			} else {
				showHelp(sender,args);
			}
			
			//This command is for us - return true!
			return true;
		} else {
			//This command isn't for us - return false!
			return false;
		}
		
	}

}
