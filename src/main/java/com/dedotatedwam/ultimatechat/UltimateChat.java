package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.config.UCConfig;
import com.dedotatedwam.ultimatechat.config.UCLang;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Plugin(
		id = "ultimatechat",
		name = "UltimateChat",
		version = "1.8.0",
		description="Fork of UltimateChat by FabioZumbi12 with some additional features and tweaks.",
		url = "http://www.phynixmc.com",
		authors = {
				"FabioZumbi12",
				"MastaC",
				"kosakriszi"
		}
)
public class UltimateChat {

	private static UltimateChat instance;

	@Inject private Logger logger;
	@Inject private Game game;
	public static Logger getLogger(){
		return UltimateChat.instance.logger;
	}

	public Game getGame(){
		return instance.game;
	}

	private String configDir;
	public static String configDir(){
		return instance.configDir;
	}
	
	public static PluginContainer plugin;
	
	private UCCommands cmds;

	private static Server serv;

	private EconomyService econ;

	private UCPerms perms;
	public static UCPerms getPerms(){
		return instance.perms;
	}
	
	public static EconomyService getEco(){
		return instance.econ;
	}

	static HashMap<String,String> pChannels = new HashMap<String,String>();
	static HashMap<String,String> tempChannels = new HashMap<String,String>();
	static HashMap<String,String> tellPlayers = new HashMap<String,String>();
	static HashMap<String,String> tempTellPlayers = new HashMap<String,String>();
	static HashMap<String,String> respondTell = new HashMap<String,String>();
	static HashMap<String,List<String>> ignoringPlayer = new HashMap<String,List<String>>();
	static List<String> mutes = new ArrayList<String>();
	static List<String> isSpy = new ArrayList<String>();

	@Listener
	public void onPreInitialization(GamePreInitializationEvent event) {
		UltimateChat.instance = this;
		// this.logger.info("UltimateChat Phynix version " + plugin.getVersion() + " is loading..."); TODO get this working again, for now use the below line
		this.logger.info("UltimateChat Phynix is now loading...");
		UltimateChat.serv = this.game.getServer();
	}

	@Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
        	plugin = Sponge.getPluginManager().getPlugin("ultimatechat").get();
        	this.configDir = game.getConfigManager().getSharedConfig(plugin).getDirectory()+File.separator+plugin.getName()+File.separator;		// TODO Simplify this to use injection annotation

        	// Initialize config
        	UCConfig.getInstance().init();
    		// Initialize lang
            UCLang.init();
            // Initialize perms TODO ???? initialize perms? wtf?
            this.perms = new UCPerms(this.game);

            // Register commands
            logger.info("Init commands module...");
    		this.cmds = new UCCommands();
    		
    		game.getEventManager().registerListeners(plugin, new UCListener());         
                        
            for (Player p:serv.getOnlinePlayers()){
            	if (!pChannels.containsKey(p.getName())){
            		pChannels.put(p.getName(), UCConfig.getInstance().getDefChannel().getAlias());
            	}
            }
            
            logger.info(plugin.getName()+" "+plugin.getVersion().get()+" enabled!");
            
        } catch (Exception e){
			logger.error("UltimateChat Phynix version " + plugin.getVersion() + " failed to start properly!", e);
        }
	}
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	public static void reload() throws IOException{
		instance.cmds.removeCmds();
		UCConfig.getInstance().init();
		UCLang.init();
		instance.cmds = new UCCommands();
		for (Player p:serv.getOnlinePlayers()){
			if (UCConfig.getInstance().getChannel(UltimateChat.pChannels.get(p.getName())) == null){
				UltimateChat.pChannels.put(p.getName(), UCConfig.getInstance().getDefChannel().getAlias());
			}					 
		}		
	}
	
	@Listener
    public void onReloadPlugins(GameReloadEvent event) {
		try {
			reload();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		logger.info(plugin.getName()+" disabled!");
	}		
}
