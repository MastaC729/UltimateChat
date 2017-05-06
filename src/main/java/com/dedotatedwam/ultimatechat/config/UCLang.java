package com.dedotatedwam.ultimatechat.config;

import com.dedotatedwam.ultimatechat.UCUtil;
import com.dedotatedwam.ultimatechat.UltimateChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UCLang {

	//TODO convert to singleton

	private static final HashMap<CommandSource, String> DelayedMessage = new HashMap<CommandSource, String>();
	private static HashMap<String, String> BaseLang = new HashMap<String, String>();
	private static HashMap<String, String> Lang = new HashMap<String, String>();
	//static List<String> langString = new ArrayList<String>();
    private static String pathLang;
    private static File langFile;
    private static String resLang; 
        	
	public static void init() {
		pathLang = UltimateChat.configDir() + File.separator + "lang" + UCConfig.getInstance().getString("language")
				+ ".properties";		// Asset to be obtained from asset folder based on config setting
		langFile = new File(pathLang);
		resLang = "lang/lang" + UCConfig.getInstance().getString("language") + ".properties";

		File lang = new File(pathLang);
		if (!lang.exists()) {
			if (!UltimateChat.plugin.getAsset(resLang).isPresent()){		// If that lang file isn't in the jar, default to english
				UCConfig.getInstance().getString("language");
				UCConfig.getInstance().save();
				resLang = "lang/langEN-US.properties";
				pathLang = UltimateChat.configDir() + File.separator + "langEN-US.properties";
			}
			UCUtil.saveResource(resLang, langFile);
			UltimateChat.plugin.getLogger().info("Created lang file: " + pathLang);
        }

        try {
			loadLang();
			loadBaseLang();
		} catch (IOException e) {
			UltimateChat.getLogger().error("Error loading lang file! ", e);
		}
		UltimateChat.getLogger().info("Language file loaded - Using: "+  UCConfig.getInstance().getString("language"));
	}

	// Loads the default language file (English) from the jar file
	private static void loadBaseLang() throws IOException{
	    BaseLang.clear();
	    Properties properties = new Properties();
		URL langURL = UltimateChat.plugin.getAsset("lang/langEN-US.properties").get().getUrl();
		URLConnection connection = langURL.openConnection();
		if (connection != null) {
			InputStream is = null;
			// Disable caches to get fresh data for reloading.
			connection.setUseCaches(false);
			is = connection.getInputStream();
			Reader reader = new InputStreamReader(is, "UTF-8");
			properties.load(reader);
		}

	    for (Object key : properties.keySet()) {
	      if ((key instanceof String)) {
	    	  BaseLang.put((String)key, properties.getProperty((String)key));
	      }
	    }
	    updateLang();
	}
	
	private static void loadLang() {
		Lang.clear();
		Properties properties = new Properties();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Object key : properties.keySet()) {
			if (!(key instanceof String)) {
				continue;
			}			
			String keylang = properties.getProperty((String) key);
			Lang.put((String) key, keylang.replace("owner", "leader"));
		}		

		// Updating the plugin updates the lang file
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").replace(".", ""));
			int rpv = Integer.parseInt(UltimateChat.plugin.getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				UltimateChat.plugin.getLogger().info("Your lang file is outdated. Probably need strings updates!");
				UltimateChat.plugin.getLogger().info("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", UltimateChat.plugin.getVersion().get());
			}
		}
	}
	
	private static void updateLang(){
	    for (String linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", UltimateChat.plugin.getVersion().get());
    	}
	    try {
	      Properties properties = new Properties()
	      {
	        private static final long serialVersionUID = 1L;	        
	        public synchronized Enumeration<Object> keys(){
	          return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	        }
	      };
	      FileReader reader = new FileReader(pathLang);
	      BufferedReader bufferedReader = new BufferedReader(reader);
	      properties.load(bufferedReader);
	      bufferedReader.close();
	      reader.close();
	      properties.clear();
	      for (String key : Lang.keySet()) {
	        if ((key instanceof String)) {
	          properties.put(key, Lang.get(key));
	        }
	      }
	      properties.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	
	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key);
		}
		
		return FMsg;
	}
	
	public static Text getText(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key);
		}
		
		return UCUtil.toText(FMsg);
	}
	
	public static void sendMessage(final CommandSource p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(UCUtil.toText(get("_UChat.prefix")+ " " + key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(UCUtil.toText(get("_UChat.prefix")+ " " + get(key)));
		}		
		
		DelayedMessage.put(p,key);
		Sponge.getScheduler().createSyncExecutor(UltimateChat.plugin).schedule(new Runnable() {
			public void run() {
				if (DelayedMessage.containsKey(p)){
					DelayedMessage.remove(p);
				}
				} 
			},1, TimeUnit.SECONDS);	
	}
		
}
