package fr.bletrazer.mailbox.commands;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.bletrazer.mailbox.DataManager.DataHolder;
import fr.bletrazer.mailbox.DataManager.DataManager;
import fr.bletrazer.mailbox.DataManager.ItemData;
import fr.bletrazer.mailbox.DataManager.LetterData;
import fr.bletrazer.mailbox.DataManager.MailBoxController;
import fr.bletrazer.mailbox.inventory.inventories.MailBoxInventory;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.bletrazer.mailbox.playerManager.PlayerManager;
import fr.bletrazer.mailbox.sql.ItemDataSQL;

public class Cmd_Mailbox implements CommandExecutor {
	
	public static final String CMD_LABEL = "mailbox";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player player = (Player) sender;

		if (args.length > 0) {
			DataHolder holder = DataManager.getDataHolder(player.getUniqueId());
			
			if (args.length == 4) {
				if(args[0].equalsIgnoreCase("item")) {
					if(args[1].equalsIgnoreCase("send")) {
						UUID pUuid = PlayerManager.getInstance().getUUID(args[2]);
						
						if(pUuid != null) {
							PlayerInfo pi = new PlayerInfo(args[2], pUuid);
						
							try {
								String prefix = args[3].contains("D") ? "P" : "PT";
								String subD = args[3].replace("D", "DT");
								String strD = prefix + subD;

								Duration d = Duration.parse(strD);
								MailBoxController.sendItem(pi.getName(), player.getInventory().getItemInMainHand(), d );
								player.sendMessage(LangManager.getValue("send_item_notification", pi.getName()));
								
							} catch(DateTimeParseException e) {
								player.sendMessage("wrong duration");
							}								
						} else {
							player.sendMessage("Cible inconnu");
						}
					}
				}
				
			} else if(args.length == 3) {
				UUID pUuid = PlayerManager.getInstance().getUUID(args[2]);
				
				if(pUuid != null) {
					PlayerInfo pi = new PlayerInfo(args[2], pUuid);
					
					if(args.length == 3) {
						if(args[0].equalsIgnoreCase("item")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.sendItem(pi.getName(), player.getInventory().getItemInMainHand(), Duration.ofSeconds(20) );
								player.sendMessage("Vous avez envoyé un objet a " + pi.getName() );
								
							}
						} else if (args[0].equalsIgnoreCase("letter")) {
							if(args[1].equalsIgnoreCase("send")) {
								MailBoxController.sendLetter(pi.getUuid(), player.getInventory().getItemInMainHand() );
								player.sendMessage("Vous avez envoyé une lettre a " + pi.getName() );
								
							}
							
						}
					}
					
				} else {
					player.sendMessage("Joueur inconnu");
				}
			} else if(args.length == 2) {
				if (args[0].equalsIgnoreCase("item")) {
					if (args[1].equalsIgnoreCase("send")) {
						MailBoxController.sendItem(player.getName(), player.getInventory().getItemInMainHand(), Duration.ofSeconds(20) );
	
					} else if (args[1].equalsIgnoreCase("getall")) {
						
						List<ItemData> list = DataManager.getTypeData(holder, ItemData.class);
						Iterator<ItemData> it = list.iterator();
						
						while(it.hasNext() ) {
							ItemData id = it.next();
							if(id.isOutOfDate()) {
								ItemDataSQL.getInstance().delete(id);
								MailBoxController.getDataHolder(player.getUniqueId()).removeData(id.getId());
								player.sendMessage("un objet a été supprimer car il été périmé.");
								
							} else {
								player.getInventory().addItem(id.getItem());
								player.sendMessage("vous avez récupéré " + DataManager.getTypeData(holder, LetterData.class)+ " de la database.");
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("letter")) {
					if(args.length == 2) {
						if(args[1].equalsIgnoreCase("send")) {
							MailBoxController.sendLetter(player.getUniqueId(), player.getInventory().getItemInMainHand() );
							
						} else if (args[1].equalsIgnoreCase("size")) {
							String msg = "Vous avez " + DataManager.getTypeData(holder, LetterData.class).size() + " lettres dans votre boite";
							player.sendMessage(msg);
							
						}
						
	
						
					}
				}
			} else if (args.length == 1) {
				UUID pUuid = PlayerManager.getInstance().getUUID(args[0]);
				
				if(pUuid != null) {
					PlayerInfo pi = new PlayerInfo(args[0], pUuid);
					
					MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getDataHolder(pi.getUuid()) );
					mailBox.openInventory(player);
					
				} else {
					player.sendMessage("Joueur " + args[0] + " inconnu");
				}
			}
		} else {
			MailBoxInventory mailBox = new MailBoxInventory(MailBoxController.getDataHolder(player.getUniqueId()) );
			mailBox.openInventory(player);
			
		}
		
		return false;
	}
}