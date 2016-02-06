package net.mineyourmind.mrwisski.InstancedDungeon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.mineyourmind.mrwisski.InstancedDungeon.Util.NBTStore;

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
	
	public Player getPlayer(String Player);
	
	public void tpPlayerSimple(String Player, String dim, int x, int y, int z);
	public void tpPlayer(String Player, String dim, int x, int y, int z, float yaw, float pitch);
	public void tpPlayerToSpawn(String Player, String Reason);
	
	public itemInfo getPlayerItemInHand(String Player);
	public World getIDim();
	public UUID getUUID(String player);
	public boolean loadChunk(String world, int x, int y, int z);
	
	public HashMap<Location,String> getAllPlayerLocs(String world);
	
	public EditSession getAsyncEditSession();
	
	public NBTStore pullNBTDataForSchematic(String playername);
	public CuboidClipboard pullClipboardFromSchematic(Selection s);
	
}
