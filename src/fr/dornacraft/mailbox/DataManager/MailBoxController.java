package fr.dornacraft.mailbox.DataManager;

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

import fr.dornacraft.mailbox.ItemStackBuilder;
import fr.dornacraft.mailbox.DataManager.factories.DataFactory;
import fr.dornacraft.mailbox.playerManager.PlayerInfo;
import fr.dornacraft.mailbox.playerManager.PlayerManager;
import fr.dornacraft.mailbox.sql.DataSQL;
import fr.dornacraft.mailbox.sql.ItemDataSQL;
import fr.dornacraft.mailbox.sql.LetterDataSQL;

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
			DataHolder holder = DataManager.getDataHolder(temp.getUuid());
			if (holder != null) {
				holder.addData(temp);
			}
			
			//notification
			Player recipient = Bukkit.getPlayer(temp.getUuid() );
			
			if(recipient != null) {
				recipient.getPlayer().sendMessage("Vous avez reçu un lettre de la part de " + letterData.getAuthor() );
			}
		} else {
			//TODO null pointer (erreur d'acces a la BDD
		}
		
	}
	
	public static void sendLetter(LetterData letterData, List<UUID> recipients) {
		for(UUID uuid : recipients ) {//FIXME si l'envoie echoue, ne pas faire le reste
			LetterData toSend = letterData.clone();
			toSend.setUuid(uuid );
			sendLetter(toSend);
		}
		
	}
	
	public static void sendLetter(UUID recipientUuid, ItemStack book) {//FIXME changer player par UUID ? TODO modifier parametres -> ajouter lettertype
		if (recipientUuid != null) {
			if (book.getType() == Material.WRITTEN_BOOK && book.hasItemMeta() && book.getItemMeta() instanceof BookMeta) {
				BookMeta bookMeta = (BookMeta) book.getItemMeta();

				String author = bookMeta.getAuthor();
				String object = bookMeta.getTitle();
				List<String> content = bookMeta.getPages();

				Data data = new DataFactory(recipientUuid, author, object);
				LetterData letterData = new LetterData(data, LetterType.STANDARD, content, false);
				
				sendLetter(letterData);
			}
		}
	}

	public static void respondToLetter(Player player, Long id, ItemStack book) {// TODO

	}
	
	private static ItemStack getBookView(LetterData letterData) {
		StringBuilder letterHead = new StringBuilder();
		letterHead.append(String.format("§lAutheur(e):§r %s\n", letterData.getAuthor()));
		
		SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy à HH:mm:ss");
		
		letterHead.append(String.format("§lDate de récéption:§r %s\n", sdf.format(letterData.getCreationDate()) ));
		letterHead.append(String.format("§lObject:§r %s\n", letterData.getObject() ) );
		
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
		
		if(letterData.getUuid().equals(player.getUniqueId())) {
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
					p.sendMessage("Vous avez reçu un objet de la part de " + pi.getName() );
					
				}
			} else {
				//TODO probleme with database
			}
			
		} else {
			//TODO player not found
		}
	}
}
