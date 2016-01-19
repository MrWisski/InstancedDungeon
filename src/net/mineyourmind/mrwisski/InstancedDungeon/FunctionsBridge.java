package net.mineyourmind.mrwisski.InstancedDungeon;

import java.io.File;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public interface FunctionsBridge {
	public boolean ConfigReload();
	public boolean weSchematicExists(String filename);
	public File getWEditSchematic(String filename);
	public File getDataDir();
	
	public Location getPlayerLoc(String Player);
	
	public class itemInfo{
		public String name;
		public String material;
		public int count;
	}
	
	public itemInfo getPlayerItemInHand(String Player);
	public World getIDim();
	public UUID getUUID(String player);
	
}
