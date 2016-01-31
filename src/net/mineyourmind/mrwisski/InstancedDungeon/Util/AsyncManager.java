package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.util.ArrayList;
import java.util.HashMap;

import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerJobEntry;
import org.primesoft.asyncworldedit.blockPlacer.IBlockPlacerListener;
import org.primesoft.asyncworldedit.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.worldedit.AsyncCuboidClipboard;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import net.mineyourmind.mrwisski.InstancedDungeon.InstancedDungeon;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonData;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.DungeonManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceData;

public class AsyncManager implements IBlockPlacerListener, IJobEntryListener {
	
	private HashMap<Integer,InstanceData> fpRegistry = new HashMap<Integer,InstanceData>();
	private ArrayList<BlockPlacerJobEntry> je = new ArrayList<BlockPlacerJobEntry>();
	private PluginMain awe = null;
	private AsyncEditSessionFactory ess = null;
	private BlockPlacer bp = null;
	
	public AsyncManager(){
		this.awe = PluginMain.getInstance();
		this.ess = (AsyncEditSessionFactory) WorldEdit.getInstance().getEditSessionFactory();
		this.bp = awe.getBlockPlacer();

	}
	
	public void pasteClipboard(InstanceData i, Vector where){
		Log.debug("AsyncManager.pasteClipboard");
		DungeonData d = i.getDungeon();

		AsyncEditSession es = (AsyncEditSession) this.ess.getEditSession(InstancedDungeon.getIDungeonDim(), 999999999);
		//esRegistry.put(p, es);
		String p = es.getPlayer();
		Log.debug("Player is : " + p);
		es.setAsyncForced(true);
		es.setFastMode(true);
		
		AsyncCuboidClipboard cc = new AsyncCuboidClipboard(p, d.getSchematic());
		
		int id = bp.getJobId(p);
		id = id + 1;
		Log.debug("Job ID is reported as : " + id);
		fpRegistry.put(id, i);
		je.add(bp.getJob(p, id));
		this.bp.addListener((IBlockPlacerListener)this);
		cc.setOffset(new Vector(0, 0, 0));
		
		try {
			Log.debug("Doing Paste.");
			cc.paste(es, where, true);
			Log.debug("Post Paste.");
			
			
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void jobAdded(BlockPlacerJobEntry arg0) {
		for(BlockPlacerJobEntry e : je){
			if(e == arg0){
				Log.debug("MATCHED JOB ENTRY!");
			}
		}
		
		Log.debug("Job Added : ID : " + arg0.getJobId() + " -- Name : " + arg0.getName() + " -- Status String : " + arg0.getStatusString());
		if(fpRegistry.get(arg0.getJobId()) != null){ //This job is for us! :D
			InstanceData i = fpRegistry.get(arg0.getJobId());
			Log.debug("Job Added for Instance '"+i.name+"' -- ID : " + arg0.getJobId() + " -- Name : " + arg0.getName() + " -- Status String : " + arg0.getStatusString());
			arg0.addStateChangedListener((IJobEntryListener)this);			
		} else { // Else - ignore, not one of ours!
			Log.debug("Not for me?");
		}
		
	}

	@Override
	public void jobRemoved(BlockPlacerJobEntry arg0) {
	
		Log.debug("Job Removed : ID : " + arg0.getJobId() + " -- Name : " + arg0.getName() + " -- Status String : " + arg0.getStatusString());
		if(fpRegistry.get(arg0.getJobId()) != null){ //This job is for us! :D
			InstanceData i = fpRegistry.get(arg0.getJobId());
			arg0.removeStateChangedListener((IJobEntryListener)this);
			Log.debug("Job Removed for Instance '"+i.name+"' -- ID : " + arg0.getJobId() + " -- Name : " + arg0.getName() + " -- Status String : " + arg0.getStatusString());
		} else { // sadness, someone elses job.
			Log.debug("Not for me?");
		}
	}

	@Override
	public void jobStateChanged(BlockPlacerJobEntry arg0) {
		int id = arg0.getJobId();
		String n = arg0.getName();
		String s = arg0.getStatusString();
		Log.debug("Job State Changed : ID : " + id + " -- Name : " + n + " -- Status String : " + s);
		InstanceData i = fpRegistry.get(id);
		
		switch(arg0.getStatus()){
			case Done:
				Log.debug("Job for dungeon '"+i.name+"' is FINISHED!");
				DungeonManager.notifyPasteDone(i);
				break;
			case Initializing:
				Log.debug("Job for dungeon '"+i.name+"' is INITIALIZING!");
				break;
			case PlacingBlocks:
				Log.debug("Job for dungeon '"+i.name+"' is PLACINGBLOCKS!");
				break;
			case Preparing:
				Log.debug("Job for dungeon '"+i.name+"' is PREPARING!");
				break;
			case Waiting:
				Log.debug("Job for dungeon '"+i.name+"' is WAITING!");
				break;
		
		}
	}

}
