package fr.bletrazer.mailbox.utils;

import org.bukkit.command.CommandSender;

import fr.bletrazer.mailbox.Main;

public class MessageUtils {
	
	public static String PLUGIN_PREFIX = Main.getInstance().getConfig().getString("plugin_prefix");
	
	public static void sendMessage(CommandSender target, MessageLevel level, String msg) {
		if(target != null) {
			String tempMsg = msg.replace("§r", level.getColor() );
			String toSend = PLUGIN_PREFIX + level.getColor() + tempMsg;
			
			target.sendMessage(toSend);
			
		}
		
		
	}
	
	public static void sendMessage(CommandSender target, MessageLevel level, String msg, Object... args) {
		sendMessage(target, level, String.format(msg, args));
	}
	
}