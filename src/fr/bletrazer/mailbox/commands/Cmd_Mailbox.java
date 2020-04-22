package fr.bletrazer.mailbox.commands;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.DataManager;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.inventories.MailBoxInventory;
import fr.bletrazer.mailbox.inventory.inventories.utils.IdentifiableAuthors;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.playerManager.PlayerManager;

public class Cmd_Mailbox implements CommandExecutor {
	
	public static final String CMD_LABEL = "mailbox";
	
	/*
	 * mailbox 0
	 * mailbox check <joueur> 1/2
	 * mailbox open <joueur> 1/2
	 * mailbox senditem <duree> 1/2
	 * 
	 */
	
	private Map<UUID, IdentifiableAuthors> map = new HashMap<>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender instanceof Player) {
			Player player = (Player)sender;
			
			if(args.length == 0 && player.hasPermission("mailbox.openmenu.self")) {
				MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getDataHolder(player.getUniqueId()) );
				mailBox.openInventory(player);
				
			} else if (args.length >= 1 && args.length <= 2) {
				if(args[1].equalsIgnoreCase("check")) {
					UUID sourceUuid = player.getUniqueId();
					
					if(args.length == 2) {
						sourceUuid = PlayerManager.getInstance().getUUID(args[1]);
					}
					
					if(sourceUuid != null ) {
						if(sourceUuid.equals(player.getUniqueId()) && player.hasPermission("mailbox.check.self") || player.hasPermission("mailbox.check.other") ) {
							DataHolder sHolder = DataManager.getDataHolder(sourceUuid);
							Integer number = DataManager.getTypeData(sHolder, LetterData.class).size();
							
							if(sourceUuid.equals(player.getUniqueId())) {
								player.sendMessage(LangManager.getValue("result_command_check_self", number));
								
							} else {
								player.sendMessage(LangManager.getValue("result_command_check_other", args[1], number));
							}
							
						} else {
							player.sendMessage(LangManager.getValue("string_permission_needed"));
						}

					} else {
						player.sendMessage(LangManager.getValue("string_player_not_found", args[1]));
					}
					
				} else if(args[1].equalsIgnoreCase("open")){
					UUID sourceUuid = player.getUniqueId();
					
					if(args.length == 2) {
						sourceUuid = PlayerManager.getInstance().getUUID(args[1]);
					}
					
					if(sourceUuid != null ) {
						if(sourceUuid.equals(player.getUniqueId()) && player.hasPermission("mailbox.openmenu.self") || player.hasPermission("mailbox.openmenu.other") ) {
							MailBoxInventory inv = new MailBoxInventory(DataManager.getDataHolder(sourceUuid));
							inv.openInventory(player);
							
						} else {
							player.sendMessage(LangManager.getValue("string_permission_needed"));
						}

					} else {
						player.sendMessage(LangManager.getValue("string_player_not_found", args[1]));
					}
					
					
				} else if(args[1].equalsIgnoreCase("senditem") && player.hasPermission("mailbox.send.items")){
					Duration duration = durationFromArgument("0S");
					
					if(args.length == 2) {
						duration = durationFromArgument(args[1]);
					}
					
					if(duration != null) {//TODO ConfirmationMenu
						
						
					} else {
						player.sendMessage(LangManager.getValue("error_parsing_duration"));
					}
					
					
				} else {
					player.sendMessage(LangManager.getValue("string_command_not_found"));
				}
				
			} else {
				player.sendMessage(LangManager.getValue("string_command_not_found"));
			}
			
			
		} else {
			sender.sendMessage(LangManager.getValue("string_command_player_only"));
		}
		
		return false;
	}
	
	private Duration durationFromArgument(String str) {
		Duration res = null;

		try {
			String prefix = str.contains("D") ? "P" : "PT";
			String subD = str.replace("D", "DT");
			String strD = prefix + subD;

			res = Duration.parse(strD);
			
		} catch(DateTimeParseException e) {
			
		}	
		return res;
		
	}

	public Map<UUID, IdentifiableAuthors> getMap() {
		return map;
	}
}