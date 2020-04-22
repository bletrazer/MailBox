package fr.bletrazer.mailbox.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.IllegalFormatException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.bletrazer.mailbox.Main;

public class LangManager {
	
	private static FileConfiguration configuration;
	
	public static void load() {
		saveDefaultLangFile();
		
		String fileName = Main.getInstance().getConfig().getString("lang_file");
		
		if(fileName == null || fileName.isEmpty() ) {
			fileName = "FR_fr";
		}
		
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + "lang" + File.separator + fileName + ".yml"));
		
		if(cfg != null) {
			setConfiguration(cfg);
			Main.getInstance().getLogger().log(Level.INFO, "Lang file \"" + fileName + "\" loaded.");
			
		} else {
			Main.getInstance().getLogger().log(Level.SEVERE, "Can't load the lang file \"" + fileName + "\". The default lang file will be used.");
		}
		
	}
	
	public static String getValue(String id, Object... args) {
		String res = null;
		
		try {
			res = String.format(getValue(id), args);
			
		} catch(IllegalFormatException e) {
			res = id + " : §4wrong formating§r";
		}
		
		
		
		return res;
	}
	
	public static String getValue(String id) {
		String res = getConfiguration().getString(id);
		
		if(res == null || res.isEmpty() ) {
			res = id + " : §4missing translation§r";
		}
		
		res = res.replace(".#*", "%s");
		
		return res;
	}
	
	private static FileConfiguration getConfiguration() {
		return configuration;
	}

	private static void setConfiguration(FileConfiguration configuration) {
		LangManager.configuration = configuration;
	}
	
	private static void saveDefaultLangFile() {
		File folder = new File(Main.getInstance().getDataFolder() + File.separator + "lang");
		File file = new File(folder.getPath() + File.separator + "FR_fr.yml");
		
		YamlConfiguration newConfig = null;
		
		if(!folder.exists() ) {
			folder.mkdirs();
		}
		
		if (!file.exists()) {
			try {
				file.createNewFile();

			} catch (IOException e) {
				Main.getInstance().getLogger().log(Level.SEVERE, "Could not create config to " + file.getName() + " to " + file.getPath());
				e.printStackTrace();
			}

			InputStream customClassStream = Main.getInstance().getResource("FR_fr.yml");
			InputStreamReader strR = new InputStreamReader(customClassStream, Charset.forName("UTF-8"));
			newConfig = YamlConfiguration.loadConfiguration(strR);

			try {
				newConfig.save(file);
			} catch (IOException e) {
				Main.getInstance().getLogger().log(Level.SEVERE, "Could not save config: " + newConfig.getName());
				e.printStackTrace();
			}

		} else {
			newConfig = YamlConfiguration.loadConfiguration(file);
		}
		
		setConfiguration(newConfig);

	}

}