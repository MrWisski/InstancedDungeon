package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.util.UUID;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

public class DungeonInstance {
	UUID owner = null;
	DungeonData dungeon = null;
	private CuboidRegion bounds = new CuboidRegion(new Vector(0,0,0),new Vector(1,1,1));
}
