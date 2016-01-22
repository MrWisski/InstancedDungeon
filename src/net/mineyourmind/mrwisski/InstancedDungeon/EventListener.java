package net.mineyourmind.mrwisski.InstancedDungeon;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;




public class EventListener implements Listener {
	
	InstancedDungeon plugin = null;
	
	long count = 0;
	
	public EventListener(InstancedDungeon p){
		Log.debug("Initialized event listener.");
		this.plugin = p;
		
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(final BlockBreakEvent event) {
		Log.debug("EventListener.onBlockBreak");
		Log.debug("Got break event in " + event.getBlock().getWorld().getName());
    	if(event.getBlock().getWorld().getName().equals(Config.dimension)){
    		
    		boolean res = InstanceManager.handleBlockBreak(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getName());
    		if(res && !event.isCancelled()){
    			event.setCancelled(res);
    		}    		
    	} else {
    		Log.debug("'"+event.getBlock().getWorld().getName()+"' != '"+Config.dimension+"'");
    	}
    }
	
	@EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(final BlockPlaceEvent event) {
		Log.debug("EventListener.onBlockPlace");
		Log.debug("Got place event : " + event.getBlock().getWorld().getName());
    	if(event.getBlock().getWorld().getName().equals(Config.dimension)){
    		
    		boolean res = InstanceManager.handleBlockPlace(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getPlayer().getName(), event.getBlock().getTypeId(), event.getBlock().getData());
    		if(res && !event.isCancelled()){
    			event.setCancelled(res);
    		}    		
    	} else {
    		Log.debug("'"+event.getBlock().getWorld().getName()+"' != '"+Config.dimension+"'");
    	}
    }
	
	
}