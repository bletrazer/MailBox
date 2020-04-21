package fr.dornacraft.mailbox.playerManager;

import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerInfo {
	
	private UUID uuid;
	private String name;
	
	public PlayerInfo(String name, UUID uuid) {
		this.setName(name);
		this.setUuid(uuid);
	}
	
	public PlayerInfo(Player player) {
		this(player.getName(), player.getUniqueId());
	}

	public UUID getUuid() {
		return this.uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Boolean isComplete() {
		return this.getName() != null && this.getUuid() != null;
	}
	
	@Override
	public boolean equals(Object obj) {
		Boolean res = false;
		
		if(obj instanceof PlayerInfo) {
			PlayerInfo pi = (PlayerInfo) obj;
			
			if(this.getName().equals(pi.getName()) && this.getUuid().equals(pi.getUuid()) ) {
				res = true;
			}
			
		}
		
		
		return res;
	}
	
}