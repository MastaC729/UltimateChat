package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.config.UCConfig;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

class UCChatProtection {
	
	private static HashMap<Player,String> chatSpam = new HashMap<Player,String>();
	private static HashMap<String,Integer> msgSpam = new HashMap<String,Integer>();
	private static HashMap<Player,Integer> UrlSpam = new HashMap<Player,Integer>();
	private static List<String> muted = new ArrayList<String>();
	
	public static String filterChatMessage(CommandSource source, String msg, UCChannel chan){
		if (!(source instanceof Player)){
			return msg;
		}
		
		final Player p = (Player) source;
		
		if (msg.length() <= 1){
			return msg;
		}
		
		//mute check
		if (muted.contains(p.getName())){
			p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","mute-msg"));
			return null;
		}
		
		//antispam
		if (UCConfig.getInstance().getProtBool("chat-protection","antispam","enable") && !p.hasPermission("uchat.bypass-spam")){
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Sponge.getScheduler().createSyncExecutor(UltimateChat.plugin).schedule(new Runnable() {
					public void run() {
						if (chatSpam.containsKey(p)){
							chatSpam.remove(p);
						}						
					}						
				},UCConfig.getInstance().getProtInt("chat-protection","antispam","time-between-messages"), TimeUnit.SECONDS);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","antispam","cooldown-msg"));
				return null;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				final String nmsg = msg;
				Sponge.getScheduler().createSyncExecutor(UltimateChat.plugin).schedule(new Runnable() {
					public void run() {
						if (msgSpam.containsKey(nmsg)){
							msgSpam.remove(nmsg);
						}						
					}						
					},UCConfig.getInstance().getProtInt("chat-protection","antispam","time-between-same-messages"), TimeUnit.SECONDS);
			} else {
				msgSpam.put(msg, msgSpam.get(msg)+1);				
				if (msgSpam.get(msg) >= UCConfig.getInstance().getProtInt("chat-protection","antispam","count-of-same-message")){
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(),UCConfig.getInstance().getProtString("chat-protection.antispam.cmd-action").replace("{player}", p.getName()));
					msgSpam.remove(msg);
					return null;
				} else {
					p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","antispam","wait-message"));
				}
			}			
		}
				
		//censor
		if (UCConfig.getInstance().getProtBool("chat-protection","censor","enable") && !p.hasPermission("uchat.bypass-censor")){
			int act = 0;
			for (String word:UCConfig.getInstance().getProtStringList("chat-protection","censor","replace-words")){
				if (!StringUtils.containsIgnoreCase(msg, word)){
					continue;
				} 				
				String replaceby = UCConfig.getInstance().getProtString("chat-protection","censor","by-word");
				if (UCConfig.getInstance().getProtBool("chat-protection","censor","replace-by-symbol")){
					replaceby = word.replaceAll("(?s).", UCConfig.getInstance().getProtString("chat-protection","censor","by-symbol"));
				}
				
				if (!UCConfig.getInstance().getProtBool("chat-protection","censor","replace-partial-word")){
					msg = msg.replaceAll("(?i)"+"\\b"+Pattern.quote(word)+"\\b", replaceby);
					if (UCConfig.getInstance().getProtBool("chat-protection","censor","action","partial-words")){
						act++;
					}
				} else {
					msg = msg.replaceAll("(?i)"+word, replaceby);
					act++;
				}				
			}
			if (act > 0){
				String action = UCConfig.getInstance().getProtString("chat-protection","censor","action","cmd");
				if (action.length() > 1){
					List<String> chs = UCConfig.getInstance().getProtStringList("chat-protection","censor","action","only-on-channels");
					if (chs.size() > 0 && chs.get(0).length() > 1 && chan != null){
						for (String ch:chs){
							if (ch.length() > 1 && (ch.equalsIgnoreCase(chan.getName()) || ch.equalsIgnoreCase(chan.getAlias()))){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), action.replace("{player}", p.getName()));
								break;
							}
						}
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(), action.replace("{player}", p.getName()));
					}					
				}
			}
		}
		
		String regexIP = UCConfig.getInstance().getProtString("chat-protection","anti-ip","custom-ip-regex");
		String regexUrl = UCConfig.getInstance().getProtString("chat-protection","anti-ip","custom-url-regex");
		
		//check ip and website
		if (UCConfig.getInstance().getProtBool("chat-protection","anti-ip","enable") && !p.hasPermission("uchat.bypass-anti-ip")){
			
			//check whitelist
			for (String check:UCConfig.getInstance().getProtStringList("chat-protection","anti-ip","whitelist-words")){
				if (Pattern.compile(check).matcher(msg).find()){	
					continue;
				}
			}
			
			//continue
			if (Pattern.compile(regexIP).matcher(msg).find()){	
				addURLspam(p);
				if (UCConfig.getInstance().getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
					p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","cancel-msg"));
					return null;
				} else {
					msg = msg.replaceAll(regexIP, UCConfig.getInstance().getProtString("chat-protection","anti-ip","replace-by-word"));
				}
			}
			if (Pattern.compile(regexUrl).matcher(msg).find()){
				addURLspam(p);
				if (UCConfig.getInstance().getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
					p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","cancel-msg"));
					return null;
				} else {
					msg = msg.replaceAll(regexUrl, UCConfig.getInstance().getProtString("chat-protection","anti-ip","replace-by-word"));
				}
			}
			
			for (String word:UCConfig.getInstance().getProtStringList("chat-protection","anti-ip","check-for-words")){
				if (Pattern.compile("(?i)"+"\\b"+word+"\\b").matcher(msg).find()){
					addURLspam(p);
					if (UCConfig.getInstance().getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
						p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","cancel-msg"));
						return null;
					} else {
						msg = msg.replaceAll("(?i)"+word, UCConfig.getInstance().getProtString("chat-protection","anti-ip","replace-by-word"));
					}
				}
			}		
		}	
		
		//capitalization verify
		if (UCConfig.getInstance().getProtBool("chat-protection","chat-enhancement","enable") && !p.hasPermission("uchat.bypass-enhancement")){
			int length = UCConfig.getInstance().getProtInt("chat-protection","chat-enhancement","minimum-length");
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msg.length() > length){
				msg = msg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase()+msg.substring(1);
				if (UCConfig.getInstance().getProtBool("chat-protection","chat-enhancement","end-with-dot") && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(".") && msg.split(" ").length > 2){
					msg = msg+".";
				}
			}				
		}
		
		//anti-caps
		if (UCConfig.getInstance().getProtBool("chat-protection","caps-filter","enable") && !p.hasPermission("uchat.bypass-enhancement")){
			int length = UCConfig.getInstance().getProtInt("chat-protection","caps-filter","minimum-length");
			int msgUppers = msg.replaceAll("\\p{P}", "").replaceAll("[a-z ]+", "").length();
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msgUppers >= length){
				msg = msg.substring(0, 1).toUpperCase()+msg.substring(1).toLowerCase();
			}
		}
		
		//antiflood
		if (UCConfig.getInstance().getProtBool("chat-protection","anti-flood","enable")){
			for (String flood:UCConfig.getInstance().getProtStringList("chat-protection","anti-flood","whitelist-flood-characs")){
				if (Pattern.compile("(["+flood+"])\\1+").matcher(msg).find()){	
					return msg;
				}
			}
			msg = msg.replaceAll("([A-Za-z])\\1+", "$1$1");
		}
		return msg;
	}	
	
	private static void addURLspam(final Player p){
		if (UCConfig.getInstance().getProtBool("chat-protection","anti-ip","punish","enable")){
			if (!UrlSpam.containsKey(p)){
				UrlSpam.put(p, 1);
			} else {
				UrlSpam.put(p, UrlSpam.get(p)+1);
				p.sendMessage(UCUtil.toText("UrlSpam: "+UrlSpam.get(p)));
				if (UrlSpam.get(p) >= UCConfig.getInstance().getProtInt("chat-protection","anti-ip","punish","max-attempts")){
					if (UCConfig.getInstance().getProtString("chat-protection","anti-ip","punish","mute-or-cmd").equalsIgnoreCase("mute")){
						muted.add(p.getName());
						p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","punish","mute-msg"));
						Sponge.getScheduler().createSyncExecutor(UltimateChat.plugin).schedule(new Runnable() {
							public void run() {
								if (muted.contains(p.getName())){						
									muted.remove(p.getName());
									p.sendMessage(UCConfig.getInstance().getProtMsg("chat-protection","anti-ip","punish","unmute-msg"));
								}
							}						
						},UCConfig.getInstance().getProtInt("chat-protection","anti-ip","punish","mute-duration"),TimeUnit.MINUTES);
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(), UCConfig.getInstance().getProtString("chat-protection","anti-ip","punish","cmd-punish"));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}
