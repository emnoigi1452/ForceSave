package me.stella.internal;

import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.stella.ForceSavePlugin;
import net.minecraft.server.v1_12_R1.EntityPlayer;

public class DataListeners {
	
	private Core coreListener;
	
	public DataListeners(ForceSavePlugin main) {
		this.coreListener = new Core(main);
	}
	
	public void register() {
		Server s = coreListener.getMain().getServer();
		s.getPluginManager().registerEvents(coreListener, coreListener.main);
	}
	
	public void unregister() {
		PlayerJoinEvent.getHandlerList().unregister(coreListener);
		InventoryCloseEvent.getHandlerList().unregister(coreListener);
		PlayerQuitEvent.getHandlerList().unregister(coreListener);
	}
	
	public class Core implements Listener {
		
		private ForceSavePlugin main;
		
		public Core(ForceSavePlugin plugin) {
			this.main = plugin;
		}
		
		public ForceSavePlugin getMain() {
			return this.main;
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onLogin(PlayerJoinEvent e) {
			final Player p = e.getPlayer();
			new Thread(new Update(p, getMain())).start();
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onInventoryClose(InventoryCloseEvent e) {
			if(!(e.getPlayer() instanceof Player))
				return;
			final Player p = (Player)e.getPlayer();
			new Thread(new Update(p, getMain())).start();
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onLogout(PlayerQuitEvent e) {
			final Player p = e.getPlayer();
			new Thread(new Update(p, getMain())).start();
			new Thread(new Save(p, getMain())).start();
		}
	}
 	
	private class Update implements Runnable {
		private EntityPlayer p;
		private ForceSavePlugin main;
		public Update(Player player, ForceSavePlugin plugin) {
			this.p = ((CraftPlayer)player).getHandle();
			this.main = plugin;
		}
		@Override
		public void run() {
			NBTRepository rep = this.main.getRepository();
			rep.updateProfile(this.p);
		}
	}
	
	private class Save implements Runnable {
		private EntityPlayer p;
		private ForceSavePlugin main;
		public Save(Player player, ForceSavePlugin plugin) {
			this.p = ((CraftPlayer)player).getHandle();
			this.main = plugin;
		}
		@Override
		public void run() {
			NBTRepository rep = this.main.getRepository();
			rep.mimicNBTSave(this.p);
			main.noteMods(ForceSavePlugin.color("&eSkyblock &3| &fSaved data for &a" + this.p.getName()));
		}
	}

}
