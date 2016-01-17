package net.mineyourmind.mrwisski.InstancedDungeon;

import java.io.File;

import org.bukkit.Location;

public interface FunctionsBridge {
	public boolean ConfigReload();
	public boolean weSchematicExists(String filename);
	public File getWEditSchematic(String filename);
	public Location getPlayerLoc(String Player);
	
}
