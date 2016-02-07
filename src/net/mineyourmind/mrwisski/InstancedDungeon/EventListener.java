package net.mineyourmind.mrwisski.InstancedDungeon;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import net.mineyourmind.mrwisski.InstancedDungeon.Config.Config;
import net.mineyourmind.mrwisski.InstancedDungeon.Dungeons.InstanceManager;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;




public class EventListener implements Listener {
	
	InstancedDungeon plugin = null;
	
	long count = 0;
	
	public EventListener(InstancedDungeon p){
		Log.debug("Initialized event listener.");
		this.plugin = p;
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
//		if(event.getPlayer().getWorld().getName().equalsIgnoreCase(Config.dimension)){
//			return; // not for us, or we don't actually care.
//		}
		if(event.getClickedBlock() == null || event.getItem() == null) return;
		Log.debug("onPlayerInteract :");
		Log.debug("With : " + Material.getMaterial(event.getItem().getType().getId()).toString() + ":" + event.getItem().getData().getData());
		Log.debug("On : " + event.getClickedBlock().getType().toString() + event.getClickedBlock().getData());
		Log.debug("By : " + event.getAction().toString());
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(Material.getMaterial(event.getItem().getType().getId()).toString().equalsIgnoreCase("THAUMCRAFT_ITEMELDRITCHOBJECT") && event.getItem().getData().getData() == Config.thaumtabletmeta){
				Log.debug("Player is using an eldritch tablet!");
				if(event.getClickedBlock().getType().toString().equalsIgnoreCase("THAUMCRAFT_BLOCKELDRITCH")){
					Log.debug("Got the location of the eldritch block player is using it on!");
					Log.debug("Aborting that call, and redirecting to DungeonManager for handling!");
					event.setCancelled(true);
					
					ThaumcraftBridge.handleEldritchLock(event.getClickedBlock(), event.getPlayer());
				}
				
			}
		}
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
	
	@SuppressWarnings("deprecation")
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