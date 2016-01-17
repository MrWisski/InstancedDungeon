package net.mineyourmind.mrwisski.InstancedDungeon;

import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class EventListener implements Listener {
	
	InstancedDungeon plugin = null;
	
	long count = 0;
	
	public EventListener(InstancedDungeon p){
		this.plugin = p;
		
	}
	
	
}