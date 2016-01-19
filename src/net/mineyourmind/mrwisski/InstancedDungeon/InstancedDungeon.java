package net.mineyourmind.mrwisski.InstancedDungeon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.google.common.io.Files;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import multiworld.MultiWorldPlugin;
import multiworld.api.ConfigurationSaveException;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import multiworld.api.PluginType;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.mineyourmind.mrwisski.InstancedDungeon.Commands.HandleCommand;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.ConfigMan;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

/** InstancedDungeon - A Bukkit plugin to add Instanced Dungeons.
 * 
 *  @author MrWisski
 *  @version 0.0.1a 16 Jan 2016 - Initial code.
 * 
 * */
public final class InstancedDungeon extends JavaPlugin implements FunctionsBridge {
	//
	public static enum MCVer {	UNKNOWN, 
								ONE_SIX_FOUR, 
								ONE_SEVEN_TEN, 
								ONE_EIGHT, ONE_EIGHT_ONE, ONE_EIGHT_TWO, ONE_EIGHT_THREE, ONE_EIGHT_FOUR}

	static Config conf = null;
	
	private final int CONF_VER = 4;
	
	EventListener eListener = null;
	HandleCommand cHandler = null;
	
	public WorldEditPlugin worldEdit = null;
	public WorldGuardPlugin worldGuard = null;
	public MultiWorldPlugin mw = null;
	public MultiWorldAPI mwAPI = null;
	
	MultiWorldWorldData iDungeon = null;
	
	//Stash for the server name
	public static String MCServerName = "Unknown Server";
	
	public MCVer serverVersion = MCVer.UNKNOWN;
	//Our instance
	public static InstancedDungeon instance;
	//Config file stuff
	private ConfigMan myConfFile;
	public static YamlConfiguration pluginConf;
	
	//Is this plugin enabled? Do its functions work?
	public boolean enabled = false;
	
	//Some more nice statics to have around.
	public static Server server;
	public static Logger Log;
	
	/**Internal function for initializing the configuration files, including version checking. */
	private void initConfig(){
		conf = Config.getInstance();
		//This is just to initialize the path, and default config, IF NEEDS BE!
		//createConfigPath does nothing if the files already exist!
		File configPath = getDataFolder();
		File configFile = new File(configPath, "config.yml");
		
		//Read our internal config file to a string.
		String conf = Util.getStringFromStream(this.getResource("config.yml"));

		// We use this static helper to ensure that the path and file both exist.
		// If they don't, we pull a config from inside the jar, and save it as the new config.
		ConfigMan.createConfigPath(configFile, configPath,conf,this);
		
		//Set up the plugin's ConfigManager config file.
		this.myConfFile = new ConfigMan(configFile,getLogger());
		
		//Attempt to read in the config on disk.
		if(myConfFile.loadConfig()){
			if(myConfFile.getConfig().getInt("configver") == this.CONF_VER){
				//If we're using the proper config ver, make this the official config!
				pluginConf = myConfFile.getConfig();
			} else {
				this.replaceConfig();
			}
			
		} else {
			getLogger().warning("Warning : Failed to load config? This is almost certainly very, very bad.");
		}
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	private void loadConfig(){
		Config.debug = pluginConf.getBoolean("plugin.debug",false);
		Config.enabled = pluginConf.getBoolean("plugin.enabled",false);
		Config.makeDim = pluginConf.getBoolean("dimension.forceGen",true);
		Config.dimension = pluginConf.getString("dimension.useDim","idungeon");
		Config.generator = pluginConf.getString("dimension.useGenerator","Empty");
		List<String> dbc = pluginConf.getStringList("dungeon.border");
		Log.info("dbc = " + dbc.toString());
		for(int x = 0; x < dbc.size(); x++){
			Material m = Material.getMaterial(dbc.get(x));
			if(m != null){
				Config.border = m.toString();
				this.getLogger().info("Using material '"+m.toString()+"' for instance border - sure hope that's not passable!");
				break;
			}
		}
	
	}
	
	private boolean loadDepends(){
				
	    try {
	    	Plugin mWorld = this.getServer().getPluginManager().getPlugin("MultiWorld");
	    	if(mWorld instanceof MultiWorldPlugin){
	    		this.mw = (MultiWorldPlugin)mWorld;
	    		this.mwAPI = ((MultiWorldPlugin)mWorld).getApi();
	    		getLogger().info("Found MultiWorld (" + mWorld.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		getLogger().warning("Could not find MultiWorld v5.2.4 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	getLogger().severe("Caught exception establishing WorldEdit services : ");
	    	getLogger().severe(e.getMessage());
	    	e.printStackTrace();
	    }
		
	    try {
	    	Plugin wEdit = this.getServer().getPluginManager().getPlugin("WorldEdit");
	    	if(wEdit instanceof WorldEditPlugin){
	    		this.worldEdit = (WorldEditPlugin)wEdit;
	    		getLogger().info("Found WorldEdit (" + wEdit.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		getLogger().warning("Could not find WorldEdit 5.6.3 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	getLogger().severe("Caught exception establishing WorldEdit services : ");
	    	getLogger().severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    
	    try {
	    	Plugin wGuard = this.getServer().getPluginManager().getPlugin("WorldGuard");
	    	if(wGuard instanceof WorldGuardPlugin){
	    		this.worldGuard = (WorldGuardPlugin)wGuard;
	    		getLogger().info("Found WorldGuard (" + wGuard.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		getLogger().warning("Could not find WorldGuard 5.9 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	getLogger().severe("Caught exception establishing WorldGuard services : ");
	    	getLogger().severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    
	    String P = this.worldEdit.getDataFolder().getAbsolutePath();
	    Log.info("WE data folder path : " + P);
	    return true;
	}
	
	@Override
	public boolean ConfigReload() {
		Config.reload();
	
		this.onDisable();
		this.onEnable();
		
		return this.enabled;
	}
	
	/**Handles a request from the Bukkit server to enable the plugin. */
	@Override
	public void onEnable() {
		this.initConfig();
		//Setup our instance var
		instance = this;
		
		//Stash the server!
		server = getServer();
				
		//Setup the log
		Log = getLogger();
		
		//Load our config!
		this.loadConfig();
		
		if(Config.enabled){
			//Let the rest of the plugin know we are in fact enabled.
			this.enabled = true;
		} else {
			this.enabled = false;
			getLogger().info("Plugin disabled via config!");
			return;
		}

		String p = new File(".").getAbsoluteFile().getParentFile().getName();
		Log.info("P = " + p);
		
		if(server.getServerName().equals("Unknown Server")){
			MCServerName = p;
		} else {
			MCServerName = server.getServerName();
		}
		
		//Log.info("Server Name = " + MCServerName);
		
		//Stash and store the current MC version of the server.
		String Ver = server.getVersion();
		if(Ver.endsWith("(MC: 1.6.4)")){
			this.serverVersion = MCVer.ONE_SIX_FOUR;
		} else if(Ver.endsWith("(MC: 1.7.10)")){
			this.serverVersion = MCVer.ONE_SEVEN_TEN;
		} else if(Ver.endsWith("(MC: 1.8)")){
			this.serverVersion = MCVer.ONE_EIGHT;
		} else if(Ver.endsWith("(MC: 1.8.1)")){
			this.serverVersion = MCVer.ONE_EIGHT_ONE;
		}else if(Ver.endsWith("(MC: 1.8.2)")){
			this.serverVersion = MCVer.ONE_EIGHT_TWO;
		}else if(Ver.endsWith("(MC: 1.8.3)")){
			this.serverVersion = MCVer.ONE_EIGHT_THREE;
		}else if(Ver.endsWith("(MC: 1.8.4)")){
			this.serverVersion = MCVer.ONE_EIGHT_FOUR;
		}
		
		switch(this.serverVersion){
		case ONE_SIX_FOUR :
			getLogger().info("Server version detected as 1.6.4");
			break;
		case ONE_SEVEN_TEN :
			getLogger().info("Server version detected as 1.7.10");
			break;
		case ONE_EIGHT :
			getLogger().info("Server version detected as 1.8");
			break;
		case ONE_EIGHT_ONE :
			getLogger().info("Server version detected as 1.8.1");
			break;
		case ONE_EIGHT_TWO :
			getLogger().info("Server version detected as 1.8.2");
			break;
		case ONE_EIGHT_THREE :
			getLogger().info("Server version detected as 1.8.3");
			break;
		case ONE_EIGHT_FOUR :
			getLogger().info("Server version detected as 1.8.4");
			break;
		case UNKNOWN :
			getLogger().info("Server version failed detection.");
			break;
			
		}
		
		if(!this.loadDepends()){
			getLogger().severe("A required dependency was NOT found. Please install this plugin and restart your server!");
			this.enabled = false;
			Config.enabled = false;
			return;
		}
		
		//Setup our events
		this.eListener = new EventListener(this);
		this.cHandler = new HandleCommand(this);
		server.getPluginManager().registerEvents(eListener, this);
		this.getCommand(Config.command).setExecutor(cHandler);
		
		//This will only run once the server is full started.
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
	    {		
			private InstancedDungeon plugin = InstancedDungeon.getInstance();
	        @Override
	        public void run()
	        {
	        	plugin.onStart();
	        }
	    });
		
		getLogger().info("Plugin enabled successfully!");
	}
 
	/** Handles a request from the Bukkit server to disable the plugin - Server is generally
	 * shutting down, so clean up everything here! */
	@Override
	public void onDisable() {
		DungeonManager.saveDungeons();
		this.enabled = false;
		//Unregister our events listener.
		HandlerList.unregisterAll(eListener);
		this.eListener = null;
		
		//Remove our command handler and unregister our command.
		PluginCommand c = this.getCommand(Config.command);
		this.unRegisterBukkitCommand(c);
		this.cHandler = null;
		
		getLogger().info("Plugin disabled successfully!");
	}
		
	public void onStart(){

		iDungeon = mw.getApi().getWorld(Config.dimension);
		
		if(iDungeon == null && mw.getApi().isWorldExisting(Config.dimension) == false){
			if(Config.makeDim){
				Log.info("Creating InstancedDungeon Dimension!");
				getServer().dispatchCommand(getServer().getConsoleSender(), "mw create " + Config.dimension + " " + Config.generator); 
				iDungeon = mw.getApi().getWorld(Config.dimension);
				if(iDungeon != null){
					if(!iDungeon.isLoaded()){
						try {
							if(iDungeon.loadWorld()){
								mw.getApi().saveConfig();
								Log.info("Dimension " + Config.dimension + " was successfully created!");
							} else {
								Log.severe("Failed to load " + Config.dimension + " after creation!");
								Log.severe("Please run /mw load " + Config.dimension);
							}
						} catch (ConfigurationSaveException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					Log.severe("We tried to create dimension " + Config.dimension + " but something went wrong.");
					Log.severe("Please run /mw create " + Config.dimension + " " + Config.generator);
				}
			} else {
				Log.info("iDungeon Dimension not found : Please run /mw create " + Config.dimension + " " + Config.generator);
				Log.info("Then, please follow the directions for loading this dimension!");
			}
		} else {
			Log.info("Found InstancedDungeon Dimension!");
			if(iDungeon.isLoaded()){
				Log.info("Dimension is loaded!");
			} else {
				Log.severe("Please edit your server config files to auto-load, and keep loaded, the " + Config.dimension + " dimension!");
			}

		}
		
		//Load in our dungeons - Since we depend on WorldEdit to be fully loaded,
		//We're doing this here, once the server comes up.
		DungeonManager.getInstance();
		if(!DungeonManager.loadDungeons()){
			getLogger().warning(Config.ecol + "Errors detected reading Dungeons Data - Check the console for errors!");
		}
		//Load in our instances
		InstanceManager.getInstance();
		if(!InstanceManager.loadInstances()){
			getLogger().warning(Config.ecol + "Errors detected reading Instances Data - Check the console for errors!");
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// HELPER FUNCTIONS
	///////////////////////////////////////////////////////////////////////////////////
	
	
	/** Getter for the class instance
	 * 
	 * @return RainManager - The current instance of this particular class.
	 */
	public static InstancedDungeon getInstance() {
		return instance;
	}
	
	
	/** Helper function for handling config version mismatches. */
	private void replaceConfig(){
		getLogger().severe("**********************************************\n");
		getLogger().severe("Configuration file version mismatch detected!\n"  );
		getLogger().severe("Backing up current config, and replacing!\n" ); 
		getLogger().severe("Plugin will be disabled! Review plugin config");
		getLogger().severe("and re-enable!");
		getLogger().severe("**********************************************\n");
		File configPath = getDataFolder();
		File configFile = new File(configPath, "config.yml");
		File configBackup = new File(configPath, "config.old");

		//Read our internal config file to a string.
		String conf = Util.getStringFromStream(this.getResource("config.yml"));

		try {
			Files.copy(configFile, configBackup);
		} catch (IOException e) {
			e.printStackTrace();
		}

		configFile.delete();

		// We use this static helper to ensure that the path and file both exist.
		// If they don't, we pull a config from inside the jar, and save it as the new config.
		ConfigMan.createConfigPath(configFile, configPath,conf,this);

		//Set up the plugin's ConfigManager config file.
		this.myConfFile = new ConfigMan(configFile,getLogger());

		//Attempt to read in the config on disk.
		if(myConfFile.loadConfig()){
			if(myConfFile.getConfig().getInt("configver") == this.CONF_VER){
				//If we're using the proper config ver, make this the official config!
				pluginConf = myConfFile.getConfig();
			}

		}

	}
	
	//Code by zeeveener - Thanks man!
	private Object getPrivateField(Object object, String field)throws SecurityException,
	NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		Field objectField = clazz.getDeclaredField(field);
		objectField.setAccessible(true);
		Object result = objectField.get(object);
		objectField.setAccessible(false);
		return result;
	}

	//Code by zeeveener - Thanks man!
    public void unRegisterBukkitCommand(PluginCommand cmd) {
    	try {
    		Object result = getPrivateField(server.getPluginManager(), "commandMap");
    		SimpleCommandMap commandMap = (SimpleCommandMap) result;
    		Object map = getPrivateField(commandMap, "knownCommands");
    		@SuppressWarnings("unchecked")
    		HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
    		knownCommands.remove(cmd.getName());
    		for (String alias : cmd.getAliases()){
    			if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(getName())){
    				knownCommands.remove(alias);
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	@Override
	public boolean weSchematicExists(String filename) {
		String P = this.worldEdit.getDataFolder().getAbsolutePath() + File.separator + "schematics" + File.separator + filename;
		Log.info("File name : " + P);
		return new File(P).exists();
	}

	@Override
	public File getWEditSchematic(String filename) {
		String P = this.worldEdit.getDataFolder().getAbsolutePath() + File.separator + "schematics" + File.separator + filename;
		Log.info("File name : " + P);
		return new File(P);
	}

	@Override
	public org.bukkit.Location getPlayerLoc(String Player) {
		return Bukkit.getPlayer(Player).getLocation();
	}

	@Override
	public itemInfo getPlayerItemInHand(String Player) {
		itemInfo i = new itemInfo();
		ItemStack is = server.getPlayer(Player).getItemInHand();
		i.material = is.getData().getItemType().toString();
		i.count = is.getAmount();
		i.name = is.toString();
		return i;
	}

	@Override
	public File getDataDir() {
		return this.getDataFolder();
	}

	@Override
	public World getIDim() {
		return iDungeon.getBukkitWorld();
	}

	@Override
	public UUID getUUID(String player) {
		return server.getPlayer(player).getUniqueId();
	}

}
