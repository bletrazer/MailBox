package fr.dornacraft.mailbox.inventory.providers.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;

public class IdentifiableAuthors {
	private String server = "";
	private String faction = "";
	private List<String> precise = new ArrayList<>();
	private Boolean calculList = false;
	private List<PlayerInfo> list = new ArrayList<>();
	private List<String> listPreview = new ArrayList<>();
	private Boolean calculPreview = true;
	
	public Boolean addIdentifier(String str) {
		Boolean res = false;
		String identifier = str.replace(" ", "");
	
		if(identifier.equals("#online") || identifier.equals("#offline") || identifier.equals("#registered")) {
			
			if(identifier.equals("#registered")) {
				this.server = identifier;
				
			} else {
				String antonym = identifier.equals("#online") ? "#offline" : "#online";
				
				if(this.server.equals(antonym) ) {
					this.server = "#registered";
					
				} else {
					this.server = identifier;
				}
			}
			this.calculList = true;
			this.calculPreview = true;
			res = true;
			
		} else if (identifier.startsWith("#faction")) {
			
			
		} else {
			UUID pUuid = PlayerManager.getInstance().getUUID(identifier);

			if (pUuid != null) {
				this.addPrecise(identifier);
				this.calculList = true;
				this.calculPreview = true;
				res = true;
				
			}
		}
		
		return res;
	}
	
	private void addPrecise(String id) {
		if(!this.precise.contains(id)) {
			this.precise.add(id);
		}
	}
	
	public void reset() {
		this.server = "";
		this.faction = "";
		this.precise = new ArrayList<>();
		this.calculList = false;
		this.list = new ArrayList<>();
		this.listPreview = new ArrayList<>();
		this.calculPreview = true;
	}
	
	public String addAllIdentifiers(List<String> list) {
		String res = null;

		if (!list.isEmpty()) {
			for (Integer index = 0; index < list.size() && res == null; index++) {
				String identifier = list.get(index).replace(" ", "");
				
				if(!this.addIdentifier(identifier) ) {
					res = identifier;
				}
			}
		}

		return res;
	}
	
	public List<String> getPreview() {//FIXME affichage out of index string
		if(this.calculPreview ) {
			List<String> temp = new ArrayList<>();
			
			if(!this.server.isEmpty() ) {
				
				if(this.server.equals("#registered")) {
					temp.add("Tout les joueurs du server.");
					
				} else if(this.server.equals("#online")) {
					temp.add("Tout les joueurs en ligne.");
					
				} else if(this.server.equals("#offline")) {
					temp.add("Tout les joueurs hors - ligne.");
					
				}
				
			}
			
			if(!this.precise.isEmpty() ) {
				String names = StringUtils.join(this.precise, ", ");
				
				StringBuilder b = new StringBuilder(names);
				if(b.toString().contains(",")) {
					b.replace(names.lastIndexOf(", "), names.lastIndexOf(", ") + 1, " et" );
				}
				names = b.toString();
				
				temp.add(names );
			}
			
			if(temp.isEmpty() ) {
				temp.add("Aucun joueur");
			}
			
			this.calculPreview = false;
			this.listPreview = temp;
		}
		
		return this.listPreview;
	}
	
	public List<PlayerInfo> getPlayerList(){
		if(this.calculList ) {
			List<PlayerInfo> temp = new ArrayList<>();
			
			if(!this.server.isEmpty() ) {
				if(this.server.equals("#registered")) {
					temp.addAll(PlayerManager.getInstance().getRegisteredPlayers() );
					
				} else if (this.server.equals("#online")) {
					temp.addAll(PlayerManager.getInstance().getOnlinePlayers() );
					
				} else if (this.server.equals("#offline")) {
					temp.addAll(PlayerManager.getInstance().getOfflinePlayers() );
					
				}
			}
			
			if(!this.precise.isEmpty() ) {
				for(String name : this.precise ) {
					UUID pUuid = PlayerManager.getInstance().getUUID(name);
					
					if(pUuid != null) {
						PlayerInfo pi =  new PlayerInfo(name, pUuid);
						
						if(!temp.contains(pi)) {
							temp.add(pi);
						}
					}
				}
			}
			this.calculList = false;
			this.list = temp;
		}
		
		return this.list;
	}
	
}
