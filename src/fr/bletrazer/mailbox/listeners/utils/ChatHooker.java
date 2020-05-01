package fr.bletrazer.mailbox.listeners.utils;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.bletrazer.mailbox.Main;
import fr.bletrazer.mailbox.lang.LangManager;

public abstract class ChatHooker {
	
	private String Id;
	
	private UUID target;
	private Listener listener;
	private Consumer<AsyncPlayerChatEvent> execution;

	public ChatHooker(String id) {
		this.setId(id);
		
	}

	public void start(Player player) {
		player.sendMessage(LangManager.getValue("information_chat_selection_start"));

		this.setTarget(player.getUniqueId());
		this.setListener(new Listener() {

			@EventHandler
			private void onChatHooking(AsyncPlayerChatEvent event) {
				if (event.getPlayer().getUniqueId().equals(getTarget())) {
					getExecution().accept(event);
				}
			}

		});

		Main.getInstance().getServer().getPluginManager().registerEvents(this.getListener(), Main.getInstance());
		this.load(this.getTarget());
	}

	public void stop() {
		this.unload(this.getTarget());
		AsyncPlayerChatEvent.getHandlerList().unregister(this.getListener());
	}
	
	public abstract void load(UUID uuid);
	public abstract void unload(UUID uuid);

	public UUID getTarget() {
		return target;
	}

	public void setTarget(UUID target) {
		this.target = target;
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public Consumer<AsyncPlayerChatEvent> getExecution() {
		return execution;
	}

	public void setExecution(Consumer<AsyncPlayerChatEvent> execution) {
		this.execution = execution;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}
}
