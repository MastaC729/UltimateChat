package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.config.UCConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public class UCUtil {

	public static Text toText(String str){
		str = str.replace("§", "&");
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

	static String toColor(String str){
    	return str.replaceAll("(&([a-fk-or0-9]))", "\u00A7$2"); 
    }
	
	static String stripColor(String str) {
		return str.replaceAll("(&([a-fk-or0-9]))", "");
	}
	
	public static void saveResource(String name, File saveTo){
		if (Files.notExists(saveTo.toPath())) {
			try {
				UltimateChat.plugin.getAsset(name).get().copyToFile(saveTo.toPath());
			} catch (IOException e) {
				UltimateChat.getLogger().error("Error when loading resource " + name + "!", e);
			}
		}
	}
		
	static boolean sendBroadcast(CommandSource sender, String[] args, boolean silent){
		StringBuilder message = new StringBuilder();
		 StringBuilder hover = new StringBuilder();
		 StringBuilder cmdline = new StringBuilder();
		 StringBuilder url = new StringBuilder();
		 boolean isHover = false;
		 boolean isCmd = false;
		 boolean isUrl = false;
		 for (String arg:args){
			 if (arg.contains(UCConfig.getInstance().getString("broadcast","on-hover"))){
				 hover.append(" "+arg.replace(UCConfig.getInstance().getString("broadcast","on-hover"), ""));
				 isHover = true;
				 isCmd = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UCConfig.getInstance().getString("broadcast","on-click"))){
				 cmdline.append(" "+arg.replace(UCConfig.getInstance().getString("broadcast","on-click"), ""));
				 isCmd = true;
				 isHover = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UCConfig.getInstance().getString("broadcast","url"))){
				 url.append(" "+arg.replace(UCConfig.getInstance().getString("broadcast","url"), ""));
				 isCmd = false;
				 isHover = false;
				 isUrl = true;
				 continue;
			 }
			 
			 if (isCmd){
				 cmdline.append(" "+arg);
			 } else
			 if (isHover){
				 hover.append(" "+arg);
			 } else
			 if (isUrl){
				 url.append(" "+arg);
			 } else {
				 message.append(" "+arg);
			 }
		 }
		 
		 if (message.toString().length() <= 1){			 
			 return false;
		 }
		 
		 if (!silent){
			 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("> Broadcast: &r"+message.toString().substring(1)));
		 }		 
		 			 
		 Builder fanci = Text.builder();
		 fanci.append(UCUtil.toText(message.toString().substring(1)));
		 
		 if (hover.toString().length() > 1){
			 fanci.onHover(TextActions.showText(UCUtil.toText(hover.toString().substring(1))));
			 if (!silent){
				 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnHover: &r"+hover.toString().substring(1)));
			 }
		 }				 
		 
		 if (cmdline.toString().length() > 1 && !silent){
			 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnClick: &r"+cmdline.toString().substring(1)));
		 }				 
		 
		 if (url.toString().length() > 1){
			 try {
				fanci.onClick(TextActions.openUrl(new URL(url.toString().substring(1))));
			} catch (MalformedURLException e) {}
			 if (!silent){
				 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> Url: &r"+url.toString().substring(1)));
			 }				  
		 }	
		 
		 for (Player p:Sponge.getServer().getOnlinePlayers()){
			 if (cmdline.toString().length() > 1){
				 fanci.onClick(TextActions.runCommand("/"+cmdline.toString().substring(1).replace("{clicked}", p.getName())));						 
			 }
			 p.sendMessage(fanci.build());
		 }
		 return true;
	}
}
