package net.mineyourmind.mrwisski.InstancedDungeon.Commands;

import java.util.ArrayList;

import net.mineyourmind.mrwisski.InstancedDungeon.Util.RetVal;

/**Command Functor Class
 * 
 * @author MrWisski
 * @version 0.0.1a 17 Jan 2016 - Initial code.
 */
public interface CommandFunctor {
	
	/** Executes this specific command.
	 * 
	 * @param 	args 	Arguments passed in for command execution
	 * @param	pName	Name of the sender
	 * @return 	True if successful, false otherwise
	 * 			In either case, getMessage must return a resulting message.
	 */
	public abstract RetVal execute(final ArrayList<String> arg, final String pName);
	
	/** 
	 * @return	The name of this command.
	 */
	public abstract String getName();

	/**
	 * @return	The full help text for this command.
	 */
	public abstract ArrayList<String> getFullHelp();
	
	/**
	 * @return	A brief, one line synopsis of this command, or commands : "command : what command does"
	 */
	public abstract ArrayList<String> getBriefHelp();
	
	/**
	 * 
	 * @return	The permission this command requires for execution, or null if none is required.
	 */
	public abstract String getPerm();
	
	/**
	 * 
	 * @return	true if this command requires Server Operator status, false if not.
	 */
	public abstract boolean reqOp();
	
	/**
	 * 
	 * @return	true if this command can only be run by the Console, false if not.
	 */
	public abstract boolean reqConsole();
	
	/**
	 * 
	 * @return	true if Console can run this command, false if not.
	 */
	public abstract boolean allowConsole();

}
