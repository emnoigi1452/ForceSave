package me.stella.internal;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.stella.ForceSavePlugin;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

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
			UUID id = e.getPlayer().getUniqueId();
			NBTTagCompound t = ((CraftPlayer)e.getPlayer()).getHandle().save(new NBTTagCompound());
			new Thread(new Update(getMain(), id, t)).start();
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onInventoryClose(InventoryCloseEvent e) {
			if(!(e.getPlayer() instanceof Player))
				return;
			UUID id = e.getPlayer().getUniqueId();
			NBTTagCompound t = ((CraftPlayer)e.getPlayer()).getHandle().save(new NBTTagCompound());
			new Thread(new Update(getMain(), id, t)).start();
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onInventoryMod(InventoryClickEvent e) {
			if(!(e.getWhoClicked() instanceof Player))
				return;
			UUID id = ((Player)e.getWhoClicked()).getUniqueId();
			NBTTagCompound t = ((CraftPlayer)((Player)e.getWhoClicked())).getHandle().save(new NBTTagCompound());
			new Thread(new Update(getMain(), id, t)).start();
		}
		
		@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
		public void onLogout(PlayerQuitEvent e) {
			UUID id = e.getPlayer().getUniqueId();
			NBTTagCompound t = ((CraftPlayer)e.getPlayer()).getHandle().save(new NBTTagCompound());
			new Thread(new Update(getMain(), id, t)).start();
			new Thread(new Save(id, getMain())).start();
			main.removeMods(e.getPlayer());
		}
	}
 	
	private class Update implements Runnable {
		private ForceSavePlugin main;
		private UUID id;
		private NBTTagCompound nbt;
		public Update(ForceSavePlugin plugin, UUID id, NBTTagCompound e) {
			this.main = plugin;
			this.id = id;
			this.nbt = e;
		}
		@Override
		public void run() {
			NBTRepository repo = main.getRepository();
			repo.updateProfile(id, nbt);
		}
	}
	
	private class Save implements Runnable {
		private UUID id;
		private ForceSavePlugin main;
		public Save(UUID id, ForceSavePlugin plugin) {
			this.id = id;
			this.main = plugin;
		}
		@Override
		public void run() {
			NBTRepository rep = this.main.getRepository();
			rep.mimicNBTSave(this.id);
		}
	}

}
