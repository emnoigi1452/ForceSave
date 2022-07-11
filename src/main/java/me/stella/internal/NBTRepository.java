package me.stella.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;

import me.stella.ForceSavePlugin;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.WorldNBTStorage;

public class NBTRepository {
	
	private Map<UUID, NBTTagCompound> repo;
	private ForceSavePlugin main;
	private File base;
	
	public NBTRepository(ForceSavePlugin main) {
		this.main = main;
		this.base = ((WorldNBTStorage)(((CraftServer)main.getServer()).getHandle().playerFileData)).getDirectory();
		this.repo = buildServerProfile(main);
	}
	
	public int getRepositorySize() {
		return this.repo.keySet().size();
	}
	
	public void updateProfile(Player player) {
		NBTTagCompound profileData = ((CraftPlayer)player).getHandle().save(new NBTTagCompound());
		this.repo.put(player.getUniqueId(), profileData);
	}
	
	public void updateProfile(EntityPlayer player) {
		this.repo.put(UUID.fromString(player.bn()), player.save(new NBTTagCompound()));
	}
	
	public void unloadRepository() {
		class AsyncSave implements Runnable {
			@Override
			public void run() {
				Collection<? extends Player> online = main.getServer().getOnlinePlayers();
				online.stream().forEach(o -> {
					updateProfile(o);
					mimicNBTSave(o.getUniqueId());
				});
			}
		}
		new Thread(new AsyncSave()).start();
	}
	
	private Map<UUID, NBTTagCompound> buildServerProfile(ForceSavePlugin main) {
		Map<UUID, NBTTagCompound> profile = new HashMap<UUID, NBTTagCompound>();
		Collection<? extends Player> online = main.getServer().getOnlinePlayers();
		online.stream().forEach(player -> {
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			NBTTagCompound nbt = handle.save(new NBTTagCompound());
			profile.put(player.getUniqueId(), nbt);
		});
		return profile;
	}
	
	public boolean mimicNBTSave(EntityPlayer player) {
		UUID uid = UUID.fromString(player.bn());
		boolean b = mimicNBTSave(uid);
		if(b)
			main.noteMods(ForceSavePlugin.color("&eSkyblock &3| &fSaved data for &a" + player.getName()));
		return b;
		
	}
	
	public boolean mimicNBTSave(UUID uid) {
		try {
			NBTTagCompound profile = this.repo.get(uid);
			File tmp = new File(this.base, String.valueOf(uid.toString()) + ".dat.tmp");
			File f1 = new File(this.base, String.valueOf(uid.toString()) + ".dat");
			NBTCompressedStreamTools.a(profile, new FileOutputStream(tmp));
			if(f1.exists())
				f1.delete();
			tmp.renameTo(f1); return true;
		} catch(Exception e) {
			this.main.logWarning(ForceSavePlugin.color(
					"&eSkyblock &3| &cError: Unable to save profile with id " + uid.toString()));
			e.printStackTrace();
			return false;
		}
	}

}
