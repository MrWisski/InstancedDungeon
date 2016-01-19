package net.mineyourmind.mrwisski.InstancedDungeon.Dungeons;

import java.util.UUID;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData.dungeonState;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.CSVable;

public class InstanceData extends CSVable{
	public final static class instanceState {
		public final static int INVALID = -1;
		public final static int NEW = 0;
		public final static int WAITING = 1;
		public final static int READY = 2;
		public final static int EDIT = 3;
		public final static int IN_USE = 4;
		public final static int RELEASED = 5;
		
		public final static String toString(int v){
			switch(v){
			case 0: return "NEW";
			case 1: return "WAITING";
			case 2: return "READY";
			case 3: return "EDIT";
			case 4: return "IN_USE";
			case 5: return "RELEASED";
			default: return "INVALID";
			}
		}
	}
	
	public String name = "";
	//TODO: Integrate with UUIDProvider.
	public String owner = null;
	//list of members - space is the seperator.
	public String members = null;
	//Origin point of our instance. The world of course is our iDungeon dimension!
	public int X = 0, Y = 0, Z = 0;
	
	//Origin point of our instance in region coords.
	public int RX = 0, RZ = 0;
	
	//What dungeon we're using.
	public String dungeonName = "";
	//Creation time of the instance
	public long created = System.currentTimeMillis();
	//Current status of this instance
	public int state = instanceState.INVALID;
	
	
	//Actual data relating to the instance 
	private DungeonData dungeon = null;
	public DungeonData getDungeon(){return dungeon;}
	
	private CuboidRegion bounds = new CuboidRegion(new Vector(0,0,0),new Vector(1,1,1));
	public CuboidRegion getBounds(){return bounds;}
	
	private Vector origin = new Vector(0,0,0);
	private Vector rOrigin = new Vector(0,0,0);
	
	public Vector getOrigin(){return origin;}
	public Vector getROrigin(){return rOrigin;}
	
	public int oLX = 0, oLY = 0, oLZ = 0;
	public String ownerLastWorld = "";
	
	private Vector ownerLastLoc = new Vector(0,0,0);
	
	
	//Owners UUID
	private UUID uuid = null;
	public UUID getUUID(){return uuid;}
	
	//Is this an edit-mode instance?
	public boolean isEditing = false;
	
	public InstanceData(){
		this.state = instanceState.INVALID;
	}
	
	public InstanceData(String name, String owner, UUID uuid, String dungeon, Vector loc, boolean edit){
		this.name = name;
		
		this.owner = owner;
		this.uuid = uuid;
				
		this.dungeonName = dungeon;
		this.dungeon = DungeonManager.getDungeon(dungeon);
		if(this.dungeon == null){
			log.severe("Cannot create instance! Dungeon '" + dungeon + "' not found!");
			this.state = instanceState.INVALID;
			return;
		}
		
		if(this.dungeon.getSchematic() == null){
			log.info("Schematic isn't loaded - attempting to set!");
			DungeonManager.setSchematic(this.dungeon);
			if(this.dungeon.getSchematic() == null){
				log.severe("Couldn't get Dungeon Schematic! DM : " + DungeonManager.message);
				log.severe(this.dungeon.getStatusDisplay());
				
			}
			
		}
		
		this.bounds = new CuboidRegion(loc,loc.add(this.dungeon.getSchematic().getSize()));
				
		this.origin = loc;
		this.X = loc.getBlockX();
		this.Y = loc.getBlockY();
		this.Z = loc.getBlockZ();
		
		this.rOrigin = InstanceManager.blockToRegion(loc);
		this.RX = rOrigin.getBlockX();
		this.RZ = rOrigin.getBlockZ();
		
		
		this.isEditing = edit;
		
		//Since we're not reading this in from a file, we'll go ahead and set this Instance to WAITING
		this.state = instanceState.WAITING;

	}

	@Override
	public boolean synch() {
		if(dungeonName != ""){
			this.uuid = UUID.fromString(dungeonName);
			this.dungeon = DungeonManager.getDungeon(this.dungeonName);
			if(dungeon == null){
				log.severe("Failed to synch Instance '" + name + "' - Dungeon does NOT exist!");
				this.state = instanceState.INVALID;
				
				return false;
			}
			this.origin = new Vector(X,Y,Z);
			this.rOrigin = new Vector(RX,0,RZ);
			this.ownerLastLoc = new Vector(oLX,oLY,oLZ);
			
			if(dungeon.state >= dungeonState.PREPPED){
				if(dungeon.getSchematic() == null){
					log.severe("Failed to synch Instance '" + name + "' - Dungeon does NOT have a schematic!");
					return false;
				} else {
					this.bounds = new CuboidRegion(origin, origin.add(dungeon.getSchematic().getSize()));
				}
			} else {
				log.severe("Failed to synch Instance '" + name + "' - Dungeon is in state "+dungeonState.toString(dungeon.state)+" - It MUST be at least PREPPED!");
				return false;
			}
		} else {
			this.log.severe("Failed to synch Instance '" + name + "' - dungeonName field is null! THIS INSTANCE IS INVALID!");
		}
		return false;
	}
	
	public String getStatusDisplay(){
		String t = "OFFLINE";
		t = Config.tcol + this.name + " -:[]:- " + Config.bcol + instanceState.toString(state) + " -:[]:- " +Config.tcol+ " Location : " + this.getOrigin().getBlockX() + ", " + this.getOrigin().getBlockY() + ", " + this.getOrigin().getBlockZ() + " -:[]:- " + Config.bcol + " Dungeon : " + this.getDungeon().name;
		return t;
		
	}
	
	
}
