package net.mineyourmind.mrwisski.InstancedDungeon.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class Config {
	private static Config instance = null;
	
	protected Config() {
	      // Exists only to defeat instantiation.
	}
	
	public static Config getInstance() {
		if(instance == null) {
			instance = new Config();
		}
		return instance;
	}
	
	public static void reload(){
		debug = true;
		enabled = false;
		dimension = "idungeon";
		generator = "Empty";
		makeDim = true;
		border = "BEDROCK";
		
		name = "Instanced Dungeon";
		command = "id";
		tcol = "§2";
		ecol = "§c";
		bcol = "§f";
		header = tcol + "[§4Instanced§8Dungeon"+ tcol + "]";
		
	}

//Internal Configuration Options
	public static String name = "Instanced Dungeon";
	public static String command = "id";
	//Text Color
	public static String tcol = "§2";
	public static String ecol = "§c";
	public static String bcol = "§f";
	//What we should display before all output.
	public static String header = tcol + "[§4Instanced§8Dungeon"+ tcol + "]";
	public static String pathToDungeons = File.separator + "dungeons" + File.separator;
	public static String pathToInstances = File.separator + "instances" + File.separator;
	
//External Configuration Options
	public static boolean debug = true;
	public static boolean enabled = false;
	public static String dimension = "idungeon";
	public static String generator = "Empty";
	public static boolean makeDim = true;
	public static String border = "BEDROCK";
	public static String[] thaumbosses = {"spawnWardenBossRoom", "spawnGolemBossRoom", "spawnCultistBossRoom", "spawnTaintBossRoom"};
	public static String thaumlockclass = "thaumcraft.common.tiles.TileEldritchLock";
	public static boolean thaumenable = true;
	public static String thaumportal = "THAUMCRAFT_BLOCKPORTALELDRITCH";
	public static int thaumtabletmeta = 2;
	public static String[] nbtIgnore = {"THAUMCRAFT_BLOCKELDRITCHNOTHING"};
	public static HashSet<String> nbtIgnoreList = new HashSet<String>();

}
