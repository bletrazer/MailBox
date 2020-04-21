package fr.dornacraft.mailbox.playerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {
	
	private static PlayerManager INSTANCE = new PlayerManager();
	private PlayerInfoSQL playerInfoSql = new PlayerInfoSQL();
	public static PlayerManager getInstance() {
		return INSTANCE;
		
	}
	
	private List<PlayerInfo> cache = new ArrayList<>();
	
	private PlayerManager() {
		
	}

	public void init() {
		for(PlayerInfo pi : playerInfoSql.getAll()){
			this.addOnce(pi);
		}
		
	}
	
	private void addOnce(PlayerInfo playerInfo) {
		if(!this.getCache().contains(playerInfo) ) {
			this.getCache().add(playerInfo);
		}
	}
	
	public void load(Player player) {
		PlayerInfo pi = playerInfoSql.tryRegister(player);
		this.addOnce(pi);

		
	}
	
	public List<PlayerInfo> getOnlinePlayers() {
		return this.getRegisteredPlayers().stream()
			.filter(pi -> Bukkit.getPlayer(pi.getUuid()) != null )
			.collect(Collectors.toList());
		
	}
	
	public List<PlayerInfo> getOfflinePlayers() {
		return this.getRegisteredPlayers().stream()
			.filter(pi -> Bukkit.getPlayer(pi.getUuid()) == null )
			.collect(Collectors.toList());
		
	}
	
	public UUID getUUID(String name) {
		return this.getCache().stream()
				.filter(pi -> pi.getName().equals(name))
				.map(PlayerInfo::getUuid)
				.findAny()
				.orElse(null);
	}
	
	public String getName(UUID uuid) {
		return this.getCache().stream()
				.filter(pi -> pi.getUuid().equals(uuid))
				.map(PlayerInfo::getName)
				.findAny()
				.orElse(null);
	}
	
	private List<PlayerInfo> getCache() {
		return this.cache;
	}
	
	public List<PlayerInfo> getRegisteredPlayers(){
		List<PlayerInfo> res = new ArrayList<>();
		res.addAll(this.cache);
		return res;
	}
}
