package me.stella;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.stella.internal.DataListeners;
import me.stella.internal.NBTRepository;
import net.md_5.bungee.api.ChatColor;

public class ForceSavePlugin extends JavaPlugin {
	
	public static Logger logger = Logger.getLogger("Minecraft");
	private List<Player> moderators;
	private NBTRepository repository;
	private DataListeners listener;
	public Map<UUID, Boolean> joinMap;
	
	@Override
	public void onEnable() {
		this.repository = new NBTRepository(this);
		this.moderators = new ArrayList<Player>();
		this.listener = new DataListeners(this);
		joinMap = new HashMap<UUID, Boolean>();
		this.listener.register();
		logger.log(Level.INFO, color("&eSkyblock &3| &fLoading &aForceSave v0.1 &fby &bDucTrader"));
	}
	
	@Override
	public void onDisable() {
		this.repository.unloadRepository();
		this.listener.unregister();
		logger.log(Level.INFO, color("&eSkyblock &3| &fUnloading &aForceSave v0.1"));
	}
	
	public void addMod(Player p) {
		this.moderators.add(p);
	}
	
	public boolean isAllowedJoin(Player p) {
		if(!joinMap.containsKey(p.getUniqueId()))
			return true;
		return joinMap.get(p.getUniqueId());
	}
	
	public void noteMods(String message) {
		if(moderators.isEmpty())
			return;
		moderators.stream().forEach(m -> {
			m.sendMessage(message);
		});
	}
	
	public void removeMods(Player p) {
		if(this.moderators.contains(p))
			this.moderators.remove(p);
		return;
	}
	
	public NBTRepository getRepository() {
		return this.repository;
	}
	
	public void logWarning(String message) {
		logger.log(Level.WARNING, message);
	}
	
	public static String color(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

}
