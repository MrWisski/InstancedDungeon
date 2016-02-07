package net.mineyourmind.mrwisski.InstancedDungeon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.google.common.io.Files;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.NBTManager;
import multiworld.MultiWorldPlugin;
import multiworld.api.ConfigurationSaveException;
import multiworld.api.MultiWorldAPI;
import multiworld.api.MultiWorldWorldData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;

import net.mineyourmind.mrwisski.InstancedDungeon.Commands.HandleCommand;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.ConfigMan;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.NBTStore;
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
	
	private final int CONF_VER = 5;
	
	EventListener eListener = null;
	HandleCommand cHandler = null;
	
	public static WorldEditPlugin worldEdit = null;
	public WorldGuardPlugin worldGuard = null;
	public static MultiWorldPlugin mw = null;
	public MultiWorldAPI mwAPI = null;
	public static PluginMain awe = null;
	public static PowerNBT PNBT = null;
	public static NBTManager NBTM = null;
	
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
		this.myConfFile = new ConfigMan(configFile);
		
		//Attempt to read in the config on disk.
		if(myConfFile.loadConfig()){
			if(myConfFile.getConfig().getInt("configver") == this.CONF_VER){
				//If we're using the proper config ver, make this the official config!
				pluginConf = myConfFile.getConfig();
			} else {
				this.replaceConfig();
			}
			
		} else {
			Log.warning("Warning : Failed to load config? This is almost certainly very, very bad.");
		}
		
		
		
	}
	
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
				Log.info("Using material '"+m.toString()+"' for instance border - sure hope that's not passable!");
				break;
			}
		}
		List<String> bossrooms = pluginConf.getStringList("thaumcraft.bosses");
		String[] newbr = new String[bossrooms.size()];
		for(int i = 0; i < bossrooms.size(); i++){
			newbr[i] = bossrooms.get(i);
		}
		Config.thaumbosses = newbr;
		Config.thaumenable = pluginConf.getBoolean("thaumcraft.enable");
		Config.thaumlockclass = pluginConf.getString("thaumcraft.lockclass");
		Config.thaumportal = pluginConf.getString("thaumcraft.portal");
		Config.thaumtabletmeta = pluginConf.getInt("thaumcraft.tabletmeta");
		
		List<String> nbtig = pluginConf.getStringList("dungeon.ignoreNBT");
		String[] newignore = new String[nbtig.size()];
		for(int i = 0; i < nbtig.size(); i++){
			newignore[i] = nbtig.get(i);
			Config.nbtIgnoreList.add(nbtig.get(i));
			Log.debug("Adding '"+nbtig.get(i)+"' to ignore list!");
		}
		
		Config.nbtIgnore = newignore;
	}
	
	private boolean loadDepends(){
		
	    try {
	    	Plugin powernbtplug = this.getServer().getPluginManager().getPlugin("PowerNBT");
	    	if(powernbtplug instanceof PowerNBT){
	    		InstancedDungeon.PNBT = (PowerNBT)powernbtplug;
	    		NBTM = NBTManager.getInstance();
	    		Log.info("Found PowerNBT (" + powernbtplug.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		Log.warning("Could not find PowerNBT - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	Log.severe("Caught exception establishing WorldEdit services : ");
	    	Log.severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    try {
	    	Plugin mWorld = this.getServer().getPluginManager().getPlugin("MultiWorld");
	    	if(mWorld instanceof MultiWorldPlugin){
	    		InstancedDungeon.mw = (MultiWorldPlugin)mWorld;
	    		this.mwAPI = ((MultiWorldPlugin)mWorld).getApi();
	    		Log.info("Found MultiWorld (" + mWorld.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		Log.warning("Could not find MultiWorld v5.2.4 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	Log.severe("Caught exception establishing WorldEdit services : ");
	    	Log.severe(e.getMessage());
	    	e.printStackTrace();
	    }
		
	    try {
	    	Plugin wEdit = this.getServer().getPluginManager().getPlugin("WorldEdit");
	    	if(wEdit instanceof WorldEditPlugin){
	    		InstancedDungeon.worldEdit = (WorldEditPlugin)wEdit;
	    		Log.info("Found WorldEdit (" + wEdit.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		Log.warning("Could not find WorldEdit 5.6.3 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	Log.severe("Caught exception establishing WorldEdit services : ");
	    	Log.severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    
	    try {
	    	Plugin wGuard = this.getServer().getPluginManager().getPlugin("WorldGuard");
	    	if(wGuard instanceof WorldGuardPlugin){
	    		this.worldGuard = (WorldGuardPlugin)wGuard;
	    		Log.info("Found WorldGuard (" + wGuard.getDescription().getVersion() + ") plugin!");
	    	} else {
	    		Log.warning("Could not find WorldGuard 5.9 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch (Exception e) {
	    	Log.severe("Caught exception establishing WorldGuard services : ");
	    	Log.severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    
	    try {
	    	Plugin pm = this.getServer().getPluginManager().getPlugin("AsyncWorldEdit");
	    	if(pm instanceof PluginMain){
	    		InstancedDungeon.awe = (PluginMain)pm;
	    		Log.info("Found AsynchWorldEdit ("+awe.getDescription().getVersion()+") plugin!");
	    	} else {
	    		Log.severe("Could not find AsynchWorldEdit 1.3 - This plugin is a REQUIRED DEPENDENCY!");
	    		return false;
	    	}
	    } catch(Exception e) {
	    	Log.severe("Caught exception establishing WorldGuard services : ");
	    	Log.severe(e.getMessage());
	    	e.printStackTrace();
	    }
	    
	    String P = InstancedDungeon.worldEdit.getDataFolder().getAbsolutePath();
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
		//Setup our instance var
		instance = this;
		
		//Stash the server!
		server = getServer();
				
		//Load our config!
		this.initConfig();
		this.loadConfig();
		
		if(Config.enabled){
			//Let the rest of the plugin know we are in fact enabled.
			this.enabled = true;
		} else {
			this.enabled = false;
			Log.info("Plugin disabled via config!");
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
			Log.info("Server version detected as 1.6.4");
			break;
		case ONE_SEVEN_TEN :
			Log.info("Server version detected as 1.7.10");
			break;
		case ONE_EIGHT :
			Log.info("Server version detected as 1.8");
			break;
		case ONE_EIGHT_ONE :
			Log.info("Server version detected as 1.8.1");
			break;
		case ONE_EIGHT_TWO :
			Log.info("Server version detected as 1.8.2");
			break;
		case ONE_EIGHT_THREE :
			Log.info("Server version detected as 1.8.3");
			break;
		case ONE_EIGHT_FOUR :
			Log.info("Server version detected as 1.8.4");
			break;
		case UNKNOWN :
			Log.info("Server version failed detection.");
			break;
			
		}
		
		if(!this.loadDepends()){
			Log.severe("A required dependency was NOT found. Please install this plugin and restart your server!");
			this.enabled = false;
			Config.enabled = false;
			return;
		}
		
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
		
		Log.info("Plugin enabled successfully!");
	}
 
	/** Handles a request from the Bukkit server to disable the plugin - Server is generally
	 * shutting down, so clean up everything here! */
	@Override
	public void onDisable() {
		DungeonManager.saveDungeons();
		InstanceManager.saveInstances();
		this.enabled = false;
		//Unregister our events listener.
		HandlerList.unregisterAll(eListener);
		this.eListener = null;
		
		//Remove our command handler and unregister our command.
		PluginCommand c = this.getCommand(Config.command);
		this.unRegisterBukkitCommand(c);
		this.cHandler = null;
		
		Log.info("Plugin disabled successfully!");
	}
		
	public void onStart(){
		Log.debug("onStart()");

		iDungeon = mw.getApi().getWorld(Config.dimension);
		
		if(iDungeon == null && mw.getApi().isWorldExisting(Config.dimension) == false){
			if(Config.makeDim){
				Log.debug("Creating InstancedDungeon Dimension!");
				getServer().dispatchCommand(getServer().getConsoleSender(), "mw create " + Config.dimension + " " + Config.generator); 
				iDungeon = mw.getApi().getWorld(Config.dimension);
				if(iDungeon != null){
					if(!iDungeon.isLoaded()){
						try {
							if(iDungeon.loadWorld()){
								mw.getApi().saveConfig();
								Log.info("Dimension " + Config.dimension + " was successfully created!");
							} else {
								Log.warning("Failed to load " + Config.dimension + " after creation!");
								Log.warning("Please run /mw load " + Config.dimension);
							}
						} catch (ConfigurationSaveException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					Log.error("Tried to create dimension " + Config.dimension + " but something went wrong.");
					Log.error("Please run /mw create " + Config.dimension + " " + Config.generator);
				}
			} else {
				Log.info("iDungeon Dimension not found, and I'm configured not to create it for you : Please run /mw create " + Config.dimension + " " + Config.generator);
				Log.info("Then, please follow the directions for loading this dimension!");
			}
		} else {
			Log.debug("Found InstancedDungeon Dimension!");
			if(iDungeon.isLoaded()){
				Log.debug("Dimension is loaded!");
			} else {
				Log.error("Please edit your server config files to auto-load, and keep loaded, the " + Config.dimension + " dimension!");
			}

		}
		
		// Force a load for our schematic type
		MCEditExtendedSchematicFormat mcee = new MCEditExtendedSchematicFormat();
		
		//Load in our dungeons - Since we depend on WorldEdit to be fully loaded,
		//We're doing this here, once the server comes up.
		DungeonManager.getInstance();
		if(DungeonManager.loadDungeons().status){
			Log.warning("Errors detected reading Dungeons Data - Check the console for errors!");
		}
		//Load in our instances
		InstanceManager.getInstance();
		if(InstanceManager.loadInstances().status){
			Log.warning("Errors detected reading Instances Data - Check the console for errors!");
		}
		
		//Setup our events
		this.eListener = new EventListener(this);
		this.cHandler = new HandleCommand(this);
		server.getPluginManager().registerEvents(eListener, this);
		this.getCommand(Config.command).setExecutor(cHandler);
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
		Log.severe("**********************************************\n");
		Log.severe("Configuration file version mismatch detected!\n"  );
		Log.severe("Backing up current config, and replacing!\n" ); 
		Log.severe("Plugin will be disabled! Review plugin config");
		Log.severe("and re-enable!");
		Log.severe("**********************************************\n");
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
		this.myConfFile = new ConfigMan(configFile);

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
    
	///////////////////////////////////////////////////////////////////////////////////
	// METHODS FROM FUNCTIONSBRIDGE
	///////////////////////////////////////////////////////////////////////////////////
	// This is all Bukkit specific stuff that we don't want floating around in our
    // classes when we go porting this plugin to Sponge, or other server techs.
	///////////////////////////////////////////////////////////////////////////////////


	@Override
	public boolean weSchematicExists(String filename) {
		String P = InstancedDungeon.worldEdit.getDataFolder().getAbsolutePath() + File.separator + "schematics" + File.separator + filename;
		Log.debug("File name : " + P);
		return new File(P).exists();
	}

	@Override
	public File getWEditSchematic(String filename) {
		String P = InstancedDungeon.worldEdit.getDataFolder().getAbsolutePath() + File.separator + "schematics" + File.separator + filename;
		Log.debug("File name : " + P);
		return new File(P);
	}

	@Override
	public com.sk89q.worldedit.Location getPlayerLoc(String Player) {
		org.bukkit.Location locB = server.getPlayer(Player).getLocation();
		
		Vector V = new Vector(locB.getX(), locB.getY(), locB.getZ());
		
		return new com.sk89q.worldedit.Location(new BukkitWorld(server.getPlayer(Player).getWorld()),V);
	}

	@Override
	public itemInfo getPlayerItemInHand(String Player) {
		itemInfo i = new itemInfo();
		ItemStack is = server.getPlayer(Player).getItemInHand();
		i.material = is.getData().getItemType().toString();
		i.count = is.getAmount();
		i.name = is.toString();
		i.id = is.getData().getItemTypeId();
		i.meta = is.getData().getData();
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
		Log.debug("InstancedDungeon.getUUID");
		if(server.getPlayer(player) == null){
			return server.getOfflinePlayer(player).getPlayer().getUniqueId();
		} else {
			return server.getPlayer(player).getUniqueId(); 
		}
	}

	@Override
	public Player getPlayer(String Player) {
		return server.getPlayer(Player);
	}

	@Override
	public void tpPlayerSimple(String Player, String dim, int x, int y, int z) {
		Location l = new Location(mw.getApi().getWorld(Config.dimension).getBukkitWorld(), x, y, z);
		
		getPlayer(Player).teleport(l);
		
	}

	@Override
	public void tpPlayer(String Player, String dim, int x, int y, int z, float yaw, float pitch) {
		Location l = new Location(mw.getApi().getWorld(Config.dimension).getBukkitWorld(), x, y, z, yaw, pitch);
		
		getPlayer(Player).teleport(l);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean loadChunk(String world, int x, int y, int z) {
		World w = mw.getApi().getWorld(Config.dimension).getBukkitWorld();
		org.bukkit.Location block_loc = new org.bukkit.Location(w, x, y-1 , z);
		Block b = w.getBlockAt(block_loc);
		b.getTypeId();
		return b != null;
	}

	@Override
	public HashMap<com.sk89q.worldedit.Location, String> getAllPlayerLocs(String world) {
		HashMap<com.sk89q.worldedit.Location, String> ll = new HashMap<com.sk89q.worldedit.Location, String>();
		
		for(Player P : server.getOnlinePlayers()){
			Location locB = P.getLocation();
			Vector V = new Vector(locB.getX(), locB.getY(), locB.getZ());
			BukkitWorld W = new BukkitWorld(P.getWorld());
			
			com.sk89q.worldedit.Location locW = new com.sk89q.worldedit.Location(W,V);
			
			ll.put(locW, P.getName());
		}
		return ll;
	}

	@Override
	public void tpPlayerToSpawn(String Player, String Reason) {
		World w = server.getWorlds().get(0);
		Location spawn = w.getSpawnLocation();
		Player P = getPlayer(Player);
		P.sendMessage(Reason);
		P.teleport(spawn);	
	}
	
	public EditSession getAsyncEditSession(){
		AsyncEditSession aes = null;
		
		AsyncEditSessionFactory aesf = new AsyncEditSessionFactory(awe);
		BukkitWorld W = new BukkitWorld(mw.getApi().getWorld(Config.dimension).getBukkitWorld());
		aes = (AsyncEditSession)aesf.getEditSession(W, 999999999);
		aes.setFastMode(true);
		aes.setAsyncForced(true);
		
		return aes;
	}
	
	public static BukkitWorld getIDungeonDim(){
		return new BukkitWorld(mw.getApi().getWorld(Config.dimension).getBukkitWorld());
	}

	@Override
	public NBTStore pullNBTDataForSchematic(String playername) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CuboidClipboard pullClipboardFromSchematic(Selection s) {


		
		Vector min = s.getNativeMinimumPoint();
		Vector max = s.getNativeMaximumPoint();

		CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(Vector.ONE), min); 

		for (int x = 0; x < s.getWidth(); ++x) { 
			for (int y = 0; y < s.getHeight(); ++y) { 
				for (int z = 0; z < s.getLength(); ++z) { 
					Vector vector = new Vector(x, y, z); 
					Block block = s.getWorld().getBlockAt(s.getMinimumPoint().getBlockX() + x, 
							s.getMinimumPoint().getBlockY() + y, 
							s.getMinimumPoint().getBlockZ() + z); 
					BaseBlock baseBlock = new BaseBlock(block.getTypeId(), block.getData()); 
					
					clipboard.setBlock(vector, baseBlock); 
				} 
			} 
		} 
		return clipboard; 

	} 



}
