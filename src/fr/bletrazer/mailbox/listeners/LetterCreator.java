package fr.bletrazer.mailbox.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.DataManager.Data;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.LetterType;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;
import fr.bletrazer.mailbox.DataManager.factories.LetterDataFactory;
import fr.bletrazer.mailbox.inventory.inventories.PlayerSelectorInventory;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiableAuthors;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.minuskube.inv.ClickableItem;

public class LetterCreator implements Listener {
	
	private static List<UUID> activity = new ArrayList<>();
	
	private UUID uuid;
	private IdentifiableAuthors recipients = new IdentifiableAuthors();
	private String object;
	private List<String> content;
	private Boolean forcedStop = false;
	private Boolean showLastStep = true;
	
	/* * * * * * * * * * * * * * * * *
	 * * * * constructor(s) * * * * *
	 * * * * * * * * * * * * * * * */
	public LetterCreator() {
		
	}
	
	
	/* * * * * * * * * * * * * * * * *
	 * * * * * manipulation * * * * *
	 * * * * * * * * * * * * * * * */
	public void startCreation(Player player) {
		this.setUuid(player.getUniqueId());
		getActivity().add(player.getUniqueId());
		player.sendMessage(LangManager.getValue("help_letter_creation_start_1"));
		player.sendMessage(LangManager.getValue("help_letter_creation_start_2"));
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
		this.next(player);
		
	}
	
	public void stopCreation() {
		this.setForcedStop(true);
		getActivity().remove(this.getUuid());
		AsyncPlayerChatEvent.getHandlerList().unregister(this);
	}
	
	private void next(Player player) {
		this.execute(player, "###");
	}
	
	private void sendRecap(Player player) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n");
		sb.append("§8"+LangManager.getValue("string_object")+":§r " + this.getObject() + "\n");
		sb.append("§8"+LangManager.getValue("string_recipients")+":§r " + this.getRecipients().getPreview() + "\n");
		sb.append("§8Message:§r\n" + StringUtils.join(this.getContent(), " ") + "\n");
		
		if(this.getShowLastStep() ) {
			sb.append("§o§6"+LangManager.getValue("string_recipients"));
			this.setShowLastStep(false);
		}
		
		player.sendMessage(sb.toString());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	private void onLetterCreation(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		Player ePlayer = event.getPlayer();
		String eMessage = event.getMessage();
		
		if(isCreatingLetter(ePlayer) ) {
			
			if(eMessage.startsWith("#")) {
				if(eMessage.equals("#stop")) {
					ePlayer.sendMessage(LangManager.getValue("information_letter_creation_quit"));
					this.stopCreation();
					return;
				}
				
				if(eMessage.equals("#recap")) {
					this.sendRecap(ePlayer);
					return;
					
				}
				
				String[] args = eMessage.split(" ");
				
				if(args.length == 2 && args[0].equals("#clear")) {
					eMessage = eMessage.toLowerCase();
					if (args[1].equals("1")) {
						this.reObject(ePlayer);
						return;
		
					} else if (args[1].equals("2")) {
						this.reContent(ePlayer);
						return;
		
					} else if (args[1].equals("3")) {
						this.reRecipients(ePlayer);
						return;
					}
				}
			}
			
			this.execute(ePlayer, eMessage);
			
		}
	}
	
	private void execute(Player ePlayer, String eMessage) {
		if(this.getForcedStop() ) {
			return;
		}
		
		if(this.getObject() == null) {
			ePlayer.sendMessage(LangManager.getValue("question_lettre_creation_object"));
			this.setObject("");
			
		} else if(this.getObject().isEmpty() ) {
			if(!eMessage.equals("###")) {
				this.setObject(eMessage);
				ePlayer.sendMessage(LangManager.getValue("information_letter_creation_object", this.getObject()) );
				this.next(ePlayer);
			}
			
		} else if (this.getContent() == null ) {
			ePlayer.sendMessage(LangManager.getValue("question_letter_creation_message"));
			this.setContent(new ArrayList<>());
			
		} else if (this.getContent().isEmpty() ) {
			if(!eMessage.equals("###")) {
				this.setContent(Arrays.asList(new String[] {eMessage}));
				ePlayer.sendMessage(LangManager.getValue("information_letter_creation_message", eMessage));
				this.next(ePlayer);
			}
			
		} else if (this.getRecipients().getPlayerList().isEmpty() ) {
			PlayerSelectorInventory pci = new PlayerSelectorInventory(this.getRecipients(), "§l"+LangManager.getValue("string_menu_target_selection"));
			pci.setOptional(ClickableItem.of(new ItemStackBuilder(Material.BARRIER).setName("§4§l"+LangManager.getValue("string_cancel")).build(), e -> {
				pci.setFinalClose(true);
				ePlayer.sendMessage(LangManager.getValue("information_letter_creation_quit"));
				stopCreation();
				ePlayer.closeInventory();
			}));
			
			pci.onFinalClose(e -> {
				this.next(ePlayer);
			});
			
			pci.openInventory(ePlayer);
			
		} else if(eMessage.equals("#send") ){
			LetterType type = this.getRecipients().getPlayerList().size() > 1 ? LetterType.ANNOUNCE : LetterType.STANDARD;
			
			List<LetterData> toSend = new ArrayList<>();
			
			for(PlayerInfo pi : this.getRecipients().getPlayerList() ) {
				Data data = new DataFactory(pi.getUuid(), ePlayer.getName(), this.getObject());
				toSend.add(new LetterDataFactory(data, type, this.getContent(), false) );
				
			}
			
			MailBoxController.sendLetters(toSend);
			
			ePlayer.sendMessage(LangManager.getValue("send_item_notification")+": " + this.getRecipients().getPreview().toString().replace("#", "").replace("]", "") );
			this.stopCreation();
			
		} else {
			this.sendRecap(ePlayer);
		}
	}
	
	private void reObject(Player player) {
		this.setShowLastStep(false);
		if(this.getContent() != null && this.getContent().isEmpty() ) {
			this.setContent(null);
		}
		this.setObject(null);
		player.sendMessage(LangManager.getValue("information_letter_creation_object_deletion"));
		this.next(player);
		
	}
	
	private void reContent(Player player) {
		this.setShowLastStep(false);
		if(this.getObject() != null && this.getObject().isEmpty() ) {
			this.setObject(null);
		}
		
		this.setContent(null);
		
		player.sendMessage(LangManager.getValue("information_letter_creation_message_deletion"));
		this.next(player);
	}
	
	private void reRecipients(Player player) {
		this.setShowLastStep(false);
		if(this.getObject() != null && this.getObject().isEmpty() ) {
			this.setObject(null);
		}
		if(this.getContent() != null && this.getContent().isEmpty() ) {
			this.setContent(null);
		}
		
		this.setRecipients(new IdentifiableAuthors());
		
		player.sendMessage(LangManager.getValue("information_letter_creation_recipients_deletion"));
		this.next(player);
	}
	
	public static Boolean isCreatingLetter(Player player) {
		return LetterCreator.getActivity().contains(player.getUniqueId());
	}
	
	/* * * * * * * * * * * * * * * * 
	 * * * setters * getters * * * *
	 * * * * * * * * * * * * * * * */
	public IdentifiableAuthors getRecipients() {
		return recipients;
	}

	public void setRecipients(IdentifiableAuthors recipients) {
		this.recipients = recipients;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getObject() {
		return object;
	}


	public void setObject(String object) {
		this.object = object;
	}


	public List<String> getContent() {
		return content;
	}


	public void setContent(List<String> content) {
		this.content = content;
	}


	private static List<UUID> getActivity() {
		return activity;
	}


	public Boolean getForcedStop() {
		return forcedStop;
	}


	public void setForcedStop(Boolean forcedStop) {
		this.forcedStop = forcedStop;
	}


	public Boolean getShowLastStep() {
		return showLastStep;
	}


	public void setShowLastStep(Boolean showLastStep) {
		this.showLastStep = showLastStep;
	}

}
