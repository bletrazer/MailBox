package fr.bletrazer.mailbox.DataManager;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import fr.bletrazer.mailbox.ItemStackBuilder;
import fr.bletrazer.mailbox.DataManager.factories.DataFactory;
import fr.bletrazer.mailbox.lang.LangManager;
import fr.bletrazer.mailbox.playerManager.PlayerInfo;
import fr.bletrazer.mailbox.playerManager.PlayerManager;
import fr.bletrazer.mailbox.sql.DataSQL;
import fr.bletrazer.mailbox.sql.ItemDataSQL;
import fr.bletrazer.mailbox.sql.LetterDataSQL;

public class MailBoxController {
	
	private static DataHolder getHolderFromDataBase(UUID uuid) {
		DataHolder res = new DataHolder(uuid, new ArrayList<>());
		List<Data> dataList = DataSQL.getInstance().getDataList(uuid);

		for (Data data : dataList) {
			ItemData itemData = ItemDataSQL.getInstance().find(data.getId());
			LetterData letterData = LetterDataSQL.getInstance().find(data.getId());

			if (itemData != null) {
				res.addData(itemData);

			} else if (letterData != null) {
				res.addData(letterData);
			}
		}

		return res;
	}
	
	public static void load(UUID uuid) {
		DataManager.getCache().put(uuid, getHolderFromDataBase(uuid) );
	}
	
	public static void unload(UUID uuid) {
		DataManager.getCache().remove(uuid);
	}
	
	public static void initialize() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player.getUniqueId());
		}
	}
	
	public static DataHolder getDataHolder(UUID uuid) {
		DataHolder res = DataManager.getDataHolder(uuid);
		
		if(res == null) {
			res = getHolderFromDataBase(uuid);
		}
		
		return res;
	}
	
	public static void sendLetter(LetterData letterData) {
		LetterData temp = LetterDataSQL.getInstance().create(letterData);

		if(temp != null) {
			DataHolder holder = DataManager.getDataHolder(temp.getOwnerUuid());
			if (holder != null) {
				holder.addData(temp);
			}
			
			//notification
			Player recipient = Bukkit.getPlayer(temp.getOwnerUuid() );
			
			if(recipient != null) {
				recipient.getPlayer().sendMessage(LangManager.getValue("receive_item_notification", letterData.getAuthor()) );
			}
		} else {
			//TODO null pointer (erreur d'acces a la BDD
		}
		
	}
	
	//FIXME si l'envoie echoue, ne pas faire le reste
	public static Boolean sendLetters(List<LetterData> letters) {
		Boolean res = false;
		
		if(letters != null && !letters.isEmpty() ) {
			List<LetterData> sent = LetterDataSQL.getInstance().createAll(letters);
	
			if(sent != null) {
				for(LetterData letter : sent) {
					DataHolder holder = DataManager.getDataHolder(letter.getOwnerUuid() );
					
					if (holder != null) {
						holder.addData(letter);
					}
					
					//notification
					Player recipient = Bukkit.getPlayer(letter.getOwnerUuid() );
					
					if(recipient != null) {
						recipient.getPlayer().sendMessage(LangManager.getValue("receive_letter_notification", letter.getAuthor()) );
					}
				}
				
				res = true;
				
			}
		}
		
		return res;
	}
	
	//FIXME si l'envoie echoue, ne pas faire le reste
	public static Boolean sendItems(List<ItemData> items) {
		Boolean res = false;
		
		if(items != null && !items.isEmpty() ) {
			List<ItemData> sent = ItemDataSQL.getInstance().createAll(items);
	
			if(sent != null) {
				for(ItemData item : sent) {
					DataHolder holder = DataManager.getDataHolder(item.getOwnerUuid() );
					
					if (holder != null) {
						holder.addData(item);
					}
					
					//notification
					Player recipient = Bukkit.getPlayer(item.getOwnerUuid() );
					
					if(recipient != null) {
						recipient.getPlayer().sendMessage(LangManager.getValue("receive_item_notification", item.getAuthor()) );
					}
				}
				res = true;
				
			}
		}
			
		return res;
	}

	public static void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}
	
	private static ItemStack getBookView(LetterData letterData) {
		StringBuilder letterHead = new StringBuilder();
		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_author"), letterData.getAuthor()) );
		
		SimpleDateFormat sdf =  new SimpleDateFormat(LangManager.getValue("string_date_format"));
		
		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_reception_date"), sdf.format(letterData.getCreationDate()) ));
		letterHead.append(String.format("§l%s:§r %s\n", LangManager.getValue("string_object"), letterData.getObject() ) );
		
		ItemStack book = new ItemStackBuilder(Material.WRITTEN_BOOK).build();
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(letterData.getAuthor());
		List<String> pages = new ArrayList<>();
		pages.add(letterHead.toString());
		pages.addAll(letterData.getContent() );
		
		bookMeta.setPages(pages);
		bookMeta.setTitle(letterData.getObject());
		
		book.setItemMeta(bookMeta);
		
		return book;
	}
	
	public static void readLetter(Player player, LetterData letterData) {
		player.openBook(getBookView(letterData));
		
		if(letterData.getOwnerUuid().equals(player.getUniqueId())) {
			letterData.setIsRead(true);
			LetterDataSQL.getInstance().update(letterData);
			
		}
	}

	public static void deleteLetter(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data instanceof LetterData) {
			holder.removeData(id);
			LetterDataSQL.getInstance().delete((LetterData) data);
		}

	}
	
	public static void deleteData(DataHolder holder, Long id) {
		Data data = holder.getData(id);
		
		if(data instanceof ItemData) {
			deleteItem(holder, data.getId());
			
		} else if (data instanceof LetterData) {
			deleteLetter(holder, data.getId());
		}
	}

	public void purgeLetters(Player player) {
		DataManager.purgeData(DataManager.getDataHolder(player.getUniqueId()), LetterData.class);
	}

	public static Boolean recoverItem(Player player, Long id) {
		Boolean success = false;
		
		if(player.getInventory().firstEmpty() != -1) {
			DataHolder pHolder = DataManager.getDataHolder(player.getUniqueId());
			Data data = pHolder.getData(id);
	
			if (data instanceof ItemData) {
				ItemData itemData = (ItemData) data;
				player.getInventory().addItem(itemData.getItem());
				deleteItem(pHolder, id);
				success = true;
	
			}
		}
		
		return success;
	}
	
	public static void deleteItem(DataHolder holder, Long id) {
		Data data = holder.getData(id);

		if (data != null && data instanceof ItemData) {
			holder.removeData(id);
			ItemDataSQL.getInstance().delete((ItemData) data);
		}
	}
	
	public static void sendItem(String recipient, ItemStack itemstack, Duration d) {
		UUID pUuid = PlayerManager.getInstance().getUUID(recipient);
		
		if(pUuid != null) {
			PlayerInfo pi = new PlayerInfo(recipient, pUuid);
			
			Data data = new DataFactory(pi.getUuid(), recipient, "object hard code");
			ItemData temp = ItemDataSQL.getInstance().create(new ItemData(data, itemstack, d) );
			
			if(temp != null) {
				DataHolder rHolder = getDataHolder(pi.getUuid());
				if(rHolder != null) {
					rHolder.addData(temp);
				}
				
				Player p = Bukkit.getPlayer(pi.getUuid());
				if(p != null) {
					p.sendMessage(LangManager.getValue("receive_item_notification", pi.getName() ) );
					
				}
			} else {
				//TODO probleme with database
			}
			
		} else {
			//TODO player not found
		}
	}
}
