package net.mineyourmind.mrwisski.InstancedDungeon.Commands;


/**Command Functor Class
 * 
 * @author MrWisski
 * @version 0.0.1a 17 Jan 2016 - Initial code.
 */
public abstract class CommandFunctor {
	public String message = "";
	
	/** Executes this specific command.
	 * 
	 * @param 	args 	Arguments passed in for command execution
	 * @param	pName	Name of the sender
	 * @return 	True if successful, false otherwise
	 * 			In either case, getMessage must return a resulting message.
	 */
	public abstract boolean execute(final String[] args, final String pName);
	
	/** 
	 * @return	The name of this command.
	 */
	public abstract String getName();
	/**
	 * @return	The message result from execute - IE, (Success) "(playername) was nuked" or 
	 * 			"(Failure) "Could not find (playername)!"
	 */
	public abstract String getMessage();
	/**
	 * @return	The full help text for this command.
	 */
	public abstract String getFullHelp();
	
	/**
	 * @return	A brief, one line synopsis of this command : "command : what command does"
	 */
	public abstract String getBriefHelp();
	
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
