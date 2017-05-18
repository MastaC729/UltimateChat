package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.API.uChatAPI;
import com.dedotatedwam.ultimatechat.config.UCConfig;
import com.dedotatedwam.ultimatechat.config.UCLang;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.io.IOException;
import java.util.Optional;

public class UCCommands {

	// Reloads / loads the commands
	// NOTE:
	UCCommands() {
		unregisterCmd("uchat");
		Sponge.getCommandManager().register(UltimateChat.plugin, UltimateChat(),"ultimatechat","uchat","chat");
		
		if (UCConfig.getInstance().getBool("tell","enable")){
			registerTellAliases();
		}
		if (UCConfig.getInstance().getBool("broadcast","enable")){
			registerUbroadcastAliases();
		}
		registerChannelAliases();		
		registerUmsgAliases();
		registerChAliases();
		
	}
	

	void removeCmds(){
		CommandManager c = Sponge.getCommandManager();			// Just to simplify the calls to the CommandManager
		c.removeMapping(c.get("ultimatechat").get());
		
		if (UCConfig.getInstance().getBool("tell","enable")){
			for (String cmd:UCConfig.getInstance().getTellAliases()){
				c.removeMapping(c.get(cmd).get());
			}
		}
		if (UCConfig.getInstance().getBool("broadcast","enable")){
			for (String cmd:UCConfig.getInstance().getBroadcastAliases()){
				c.removeMapping(c.get(cmd).get());
			}
		}
		for (String cmd:UCConfig.getInstance().getChAliases()){
			c.removeMapping(c.get(cmd).get());
		}		
		for (String cmd:UCConfig.getInstance().getMsgAliases()){
			c.removeMapping(c.get(cmd).get());
		}
		for (String cmd:UCConfig.getInstance().getChCmd()){
			c.removeMapping(c.get(cmd).get());
		}
	}
		
	private void unregisterCmd(String cmd){
		if (Sponge.getCommandManager().get(cmd).isPresent()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
	}
	
	private void registerTellAliases() {		
		//register tell aliases
		for (String tell:UCConfig.getInstance().getTellAliases()){
			unregisterCmd(tell);
			if (tell.equals("r")){
				Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
						.arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
						.permission("uchat.cmd.tell")
					    .description(Text.of("Respond to the tell of other players."))
					    .executor((src, args) -> { {
					    	if (src instanceof Player){
					    		Player p = (Player) src;						
					    		if (UltimateChat.respondTell.containsKey(p.getName())){
									Optional<Player> receiver = Sponge.getServer().getPlayer(UltimateChat.respondTell.get(p.getName()));
									
									sendTell(p, receiver, args.<String>getOne("message").get());
									return CommandResult.success();	
								} else {
									throw new CommandException(UCLang.getText("cmd.tell.nonetorespond"));
								}
					    	}				    	
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			} else {
				Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
						.arguments(GenericArguments.player(Text.of("player")), GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
					    .description(Text.of("Lock your chat with a player."))
					    .permission("uchat.cmd.tell")
					    .executor((src, args) -> { {
					    	Player receiver = args.<Player>getOne("player").get();
					    	if (src instanceof Player){					    		
					    		Player p = (Player) src;
					    		if (args.<String>getOne("message").isPresent()){
					    			if (receiver.equals(p)){
					    				throw new CommandException(UCLang.getText("cmd.tell.self"), true);
									}

									UltimateChat.tempTellPlayers.put(p.getName(), receiver.getName());
									sendTell(p, args.<Player>getOne("player"), args.<String>getOne("message").get());
									return CommandResult.success();
					    		} else {
					    			if (receiver.equals(p)){
										throw new CommandException(UCLang.getText("cmd.tell.self"), true);
									}
									
									if (UltimateChat.tellPlayers.containsKey(p.getName()) && UltimateChat.tellPlayers.get(p.getName()).equals(receiver.getName())){
										UltimateChat.tellPlayers.remove(p.getName());
										UCLang.sendMessage(p, UCLang.get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
									} else {
										UltimateChat.tellPlayers.put(p.getName(), receiver.getName());
										UCLang.sendMessage(p, UCLang.get("cmd.tell.locked").replace("{player}", receiver.getName()));
									}
					    		}					    		
					    	} else if (args.<String>getOne("message").isPresent()){
					    		String msg = args.<String>getOne("message").get();
					    		String prefix = UCConfig.getInstance().getString("tell","prefix");
								String format = UCConfig.getInstance().getString("tell","format");
								
								prefix = UCMessages.formatTags("", prefix, Sponge.getServer().getConsole(), receiver, msg, new UCChannel("tell"));
								format = UCMessages.formatTags("tell", format, Sponge.getServer().getConsole(), receiver, msg, new UCChannel("tell"));
										
								receiver.sendMessage(UCUtil.toText(prefix+format));
								Sponge.getServer().getConsole().sendMessage(UCUtil.toText(prefix+format));
					    	}
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			}
			
		}
	}

	private void registerChAliases() {
		//register ch cmds aliases
		for (String cmd:UCConfig.getInstance().getChCmd()){
			unregisterCmd(cmd);
			Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
					.arguments(new ChannelCommandElement(Text.of("channel")))
				    .description(Text.of("Join in a channel if you have permission."))
				    .executor((src, args) -> { {
				    	if (src instanceof Player){
				    		Player p = (Player) src;
				    		// If the channel the player specified doesn't exist
				    		if (!args.getOne("channel").isPresent()) {
								UCLang.sendMessage(p, UCLang.get("channel.dontexist"));
								return CommandResult.success();
							}
				    		UCChannel ch = args.<UCChannel>getOne("channel").get();							
							if (UltimateChat.getPerms().channelPermReceive(p, ch) || UltimateChat.getPerms().channelPermSend(p, ch) || UltimateChat.getPerms().channelPerm(p, ch)) {
								if (UltimateChat.pChannels.containsKey(p.getName()) && UltimateChat.pChannels.get(p.getName()).equals(ch.getAlias())) {
									UCLang.sendMessage(p, UCLang.get("channel.alreadyon").replace("{channel}", ch.getName()));
									return CommandResult.success();
								}

								UltimateChat.pChannels.put(p.getName(), ch.getAlias());
								UCLang.sendMessage(p, UCLang.get("channel.entered").replace("{channel}", ch.getName()));
							} else {
								throw new CommandException(UCUtil.toText(UCLang.get("channel.nopermission.receive").replace("{channel}", ch.getName())));
							}
				    	} 
				    	return CommandResult.success();	
				    }})
				    .build(), cmd);
		}
	}

	private void registerUmsgAliases() {
		//register umsg aliases
		for (String msga:UCConfig.getInstance().getMsgAliases()){
			unregisterCmd(msga);
			Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
					.permission("uchat.cmd.message")
				    .description(Text.of("Send a message directly to a player."))
				    .executor((src, args) -> { {
				    	Player receiver = args.<Player>getOne("player").get();
				    	String msg = args.<String>getOne("message").get();
				    	receiver.sendMessage(UCUtil.toText(msg));
						Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> Private to &6"+receiver.getName()+"&8: &r"+msg));
				    	return CommandResult.success();	
				    }})
				    .build(), msga);
		}
	}

	private void registerUbroadcastAliases() {
		//register ubroadcast aliases
		for (String brod:UCConfig.getInstance().getBroadcastAliases()){
			unregisterCmd(brod);
			Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
					.permission("uchat.cmd.broadcast")
				    .description(Text.of("Command to send broadcast to server."))
				    .executor((src, args) -> { {
				    	if (!UCUtil.sendBroadcast(src, args.<String>getOne("message").get().split(" "), false)){
							sendHelp(src);
						}  
				    	return CommandResult.success();	
				    }})
				    .build(), brod);
		}
	}

	private void registerChannelAliases() {
		//register channel aliases
		for (String cha:UCConfig.getInstance().getChAliases()){
			UltimateChat.getLogger().debug("Attempting to register channel of name " + cha);
			unregisterCmd(cha);
			UCChannel ch = UCConfig.getInstance().getChannel(cha);
			UltimateChat.getLogger().debug("Channel name from UCChannel class: " + ch.getName());
			Sponge.getCommandManager().register(UltimateChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
				    .description(Text.of("Command to use channel "+ch.getName()+"."))
				    .executor((src, args) -> { {
				    	if (src instanceof Player){
				    		if (args.<String>getOne("message").isPresent()){
								UltimateChat.tempChannels.put(src.getName(), ch.getAlias());
				    			/* Disabled by FabioZumbi for some unknown reason
				    			Text msg = Text.of(args.<String>getOne("message").get());				    			
				    			MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
		    							Cause.source(src).named(NamedCause.notifier(src)).build(),
		    							src.getMessageChannel(), 
		    							Optional.of(src.getMessageChannel()), 				    							
		    							new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
		    		                            .onShiftClick(TextActions.insertText(src.getName()))
		    		                            .onClick(TextActions.suggestCommand("/msg " + src.getName()))
		    		                            .build(), msg), 
		    		                    msg, 
		    							false);
				    			Sponge.getEventManager().post(event);*/
				    					
				    			if (UltimateChat.mutes.contains(src.getName()) || ch.isMuted(src.getName())){
				    				UCLang.sendMessage(src, "channel.muted");
				    				return CommandResult.success();
				    			}
				    			
				    			Object[] chArgs = UCMessages.sendFancyMessage(new String[0], args.<String>getOne("message").get(), ch, src, null);  
				    			if (chArgs != null){
				    				// MutableMessageChannel msgCh = (MutableMessageChannel) chArgs[0]; TODO Remove this if the new method works
									MutableMessageChannel msgCh = MessageChannel.permission("channel." + ch.getName() + ".receive").asMutable();
									msgCh.addMember(src);
				    				msgCh.send(src, Text.join((Text)chArgs[1],(Text)chArgs[2],(Text)chArgs[3]));
				    			}
				    		} else {
				    			if (!ch.canLock()){
				    				UCLang.sendMessage(src, "help.channels.send");
									return CommandResult.success();
								}
					    		if (UltimateChat.pChannels.containsKey(src.getName()) && UltimateChat.pChannels.get(src.getName()).equalsIgnoreCase(ch.getAlias())){
					    			UltimateChat.tempChannels.put(src.getName(), ch.getAlias());
					    			UCLang.sendMessage(src, UCLang.get("channel.alreadyon").replace("{channel}", ch.getName()));
									return CommandResult.success();
								}
					    		UltimateChat.pChannels.put(src.getName(), ch.getAlias());
					    		UCLang.sendMessage(src, UCLang.get("channel.entered").replace("{channel}", ch.getName()));	
				    		}
				    	} else {
				    		UCMessages.sendFancyMessage(new String[0], args.<String>getOne("message").get(), ch, src, null);  
				    	}				    	
				    	return CommandResult.success();	
				    }})
				    .build(), cha);
		}
	}
	
	private CommandCallable UltimateChat() {
		CommandSpec reload = CommandSpec.builder()
				.description(Text.of("Command to reload uchat."))
				.permission("uchat.cmd.reload")
				.executor((src,args) -> {{
					//uchat reload
					try {
						UltimateChat.reload();
						UCLang.sendMessage(src, "plugin.reloaded");
					} catch (Exception e) {
						UltimateChat.getLogger().error("Error reloading plugin! ", e);
					}
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec clear = CommandSpec.builder()
				.description(Text.of("Clear your chat."))
				.permission("uchat.cmd.clear")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat clear
						for (int i = 0; i < 100; i++){
							p.sendMessage(Text.of(" "));
						}						 
			    		UCLang.sendMessage(src, "cmd.clear.cleared");	
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec clearAll = CommandSpec.builder()
				.description(Text.of("Clear the chat of all online players."))
				.permission("uchat.cmd.clear-all")
				.executor((src,args) -> {{
					//uchat clear-all
					for (Player play:Sponge.getServer().getOnlinePlayers()){
						for (int i = 0; i < 100; i++){
							if (!play.isOnline()){
								continue;
							}
							play.sendMessage(Text.of(" "));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec spy = CommandSpec.builder()
				.description(Text.of("Turn on the social spy."))
				.permission("uchat.cmd.spy")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat spy
						if (!UltimateChat.isSpy.contains(p.getName())){
							UltimateChat.isSpy.add(p.getName());
							UCLang.sendMessage(src, "cmd.spy.enabled");
						} else {
							UltimateChat.isSpy.remove(p.getName());
							UCLang.sendMessage(src, "cmd.spy.disabled");
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec ignorePlayer = CommandSpec.builder()
				.arguments(GenericArguments.player(Text.of("player")))
				.description(Text.of("Ignore a player."))
				.permission("uchat.cmd.ignore.player")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat ignore player
						Player play = args.<Player>getOne("player").get();
						if (play.equals(p)){
							throw new CommandException(UCLang.getText("cmd.ignore.self"), true);
						}
		    			if (UCMessages.isIgnoringPlayers(p.getName(), play.getName())){
							UCMessages.unIgnorePlayer(p.getName(), play.getName());
							UCLang.sendMessage(src, UCLang.get("player.unignoring").replace("{player}", play.getName()));
						} else {
							UCMessages.ignorePlayer(p.getName(), play.getName());
							UCLang.sendMessage(src, UCLang.get("player.ignoring").replace("{player}", play.getName()));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec ignoreChannel = CommandSpec.builder()
				.arguments(new ChannelCommandElement(Text.of("channel")))
				.description(Text.of("Ignore a channel."))
				.permission("uchat.cmd.ignore.channel")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat ignore channel
						UCChannel ch = args.<UCChannel>getOne("channel").get();

						// This command is disabled by default
						if ((src.hasPermission("uchat.admin.ignoreoverride." + ch.getName()) || src.hasPermission("uchat.admin.ignoreoverride." + ch.getAlias()))
								&& UCConfig.getInstance().isChannelIgnoringEnabled()) {
							if (ch.isIgnoring(p.getName())) {
								ch.unIgnoreThis(p.getName());
								UCLang.sendMessage(src, UCLang.get("channel.notignoring").replace("{channel}", ch.getName()));
							} else {
								ch.ignoreThis(p.getName());
								UCLang.sendMessage(src, UCLang.get("channel.ignoring").replace("{channel}", ch.getName()));
							}
						} else {
							if (!UCConfig.getInstance().isChannelIgnoringEnabled()) {
								UCLang.sendMessage(src, UCLang.get("channel.override.disabled"));
								return CommandResult.success();
							}
							if (src.hasPermission("uchat.admin.ignoreoverride." + ch.getName()) || src.hasPermission("uchat.admin.ignoreoverride." + ch.getAlias())) {
								UCLang.sendMessage(src, UCLang.get("channel.ignore.nopermission").replace("{channel}", ch.getName()));
								return CommandResult.success();
							}
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec mute = CommandSpec.builder()
				.arguments(GenericArguments.player(Text.of("player")),GenericArguments.optional(new ChannelCommandElement(Text.of("channel"))))
				.description(Text.of("Mute a player."))
				.permission("uchat.cmd.mute")
				.executor((src,args) -> {{
					//uchat ignore channel
					Player play = args.<Player>getOne("player").get();
					if (args.<UCChannel>getOne("channel").isPresent()){
						UCChannel ch = args.<UCChannel>getOne("channel").get();
						if (ch.isMuted(play.getName())){
							ch.unMuteThis(play.getName());
							UCLang.sendMessage(src, UCLang.get("channel.unmuted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
						} else {
							ch.muteThis(play.getName());
							UCLang.sendMessage(src, UCLang.get("channel.muted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
						}
					} else {
						if (UltimateChat.mutes.contains(play.getName())){
							 UltimateChat.mutes.remove(play.getName());
							 UCConfig.getInstance().unMuteInAllChannels(play.getName());
							 UCLang.sendMessage(src, UCLang.get("channel.unmuted.all").replace("{player}", play.getName()));
						 } else {
							 UltimateChat.mutes.add(play.getName());
							 UCConfig.getInstance().muteInAllChannels(play.getName());
							 UCLang.sendMessage(src, UCLang.get("channel.muted.all").replace("{player}", play.getName()));
						 }
					}										
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec help = CommandSpec.builder()
				.description(Text.of("Prints out information about this plugin's commands and the user's available channels."))
				.executor((src,args) -> {{
					sendHelp(src);
		    		return CommandResult.success();	
				}})
				.build();

		//chat create [name] [alias] [color] [prefix]
		CommandSpec create = CommandSpec.builder()
				.arguments(GenericArguments.string(Text.of("chName")),
						GenericArguments.string(Text.of("chAlias")),
						GenericArguments.string(Text.of("color")))
				.description(Text.of("Creates a new channel, then saves it as its own config file in the channels folder."))
				.permission("uchat.cmd.create")
				.executor((src,args) -> {{

					String chName = args.<String>getOne("chName").get();
					String chAlias = args.<String>getOne("chAlias").get();
					String color = args.<String>getOne("color").get();

					// If they're missing any arguments
					if (chName.isEmpty() || chAlias.isEmpty() || color.isEmpty()) {
						UCLang.sendMessage(src, UCLang.get("channel.create.invalidArguments"));
						return CommandResult.empty();
					}

					// If the name is taken
					if (UCConfig.getInstance().getChAliases().contains(chName)) {
						UCLang.sendMessage(src, UCLang.get("channel.create.nameTaken"));
						return CommandResult.empty();
					}

					// If the alias is taken
					if (UCConfig.getInstance().getChAliases().contains(chAlias)) {
						UCLang.sendMessage(src, UCLang.get("channel.create.aliasTaken"));
						return CommandResult.empty();
					}

					try {
						uChatAPI.registerNewChannel(chName, chAlias, color);
					} catch (IOException e) {
						UltimateChat.getLogger().error("Error when creating new channel of name " + chName + "!", e);
					}

					UCLang.sendMessage(src, UCLang.get("channel.create").replace("{channel}", chName).replace("{alias}", chAlias).replace("{ch-color}", color));

					return CommandResult.success();
				}})
				.build();

		//uchat <args...>
		CommandSpec uchat = CommandSpec.builder()
			    .description(Text.of("Main command for uchat."))
			    .executor((src, args) -> { {	    	
			    	//no args
			    	src.sendMessage(UCUtil.toText("&b---------------- "+UltimateChat.plugin.getName()+" "+UltimateChat.plugin.getVersion().get()+" ---------------"));
			    	src.sendMessage(UCUtil.toText("&bDeveloped by &6" + UltimateChat.plugin.getAuthors().get(0) + "."));
			    	src.sendMessage(UCUtil.toText("&bFor more information about the commands, type [" + "&6/uchat ?&b]."));
			    	src.sendMessage(UCUtil.toText("&b---------------------------------------------------"));			         
			    	return CommandResult.success();	
			    }})
			    .child(help, "?")
			    .child(reload, "reload")
			    .child(clear, "clear")
			    .child(clearAll, "clear-all")
			    .child(spy, "spy")
			    .child(CommandSpec.builder()
			    		.child(ignorePlayer, "player")
			    		.child(ignoreChannel, "channel")
			    		.build(), "ignore")
			    .child(mute, "mute")
				.child(create, "create")
			    .build();
		
		return uchat;
	}

	private void sendTell(Player p, Optional<Player> receiver, String msg){		
		if (!receiver.isPresent() || !receiver.get().isOnline() || !p.canSee(receiver.get())){
			UCLang.sendMessage(p, "listener.invalidplayer");
			return;
		}	
		Player tellreceiver = receiver.get();	
		UltimateChat.respondTell.put(tellreceiver.getName(),p.getName());
		UCMessages.sendFancyMessage(new String[0], msg, null, p, tellreceiver);			
	}
	
	private void sendHelp(CommandSource p){		
		p.sendMessage(UCUtil.toText("&7--------------- "+UCLang.get("_UChat.prefix")+" Help &7---------------"));		
		p.sendMessage(UCLang.getText("help.channels.enter"));
		p.sendMessage(UCLang.getText("help.channels.send"));
		if (p.hasPermission("uchat.cmd.tell")){
			p.sendMessage(UCLang.getText("help.tell.lock"));
			p.sendMessage(UCLang.getText("help.tell.send"));
			p.sendMessage(UCLang.getText("help.tell.respond"));
		}
		if (p.hasPermission("uchat.broadcast")){
			p.sendMessage(UCLang.getText("help.cmd.broadcast"));
		}
		if (p.hasPermission("uchat.cmd.umsg")){
			p.sendMessage(UCLang.getText("help.cmd.umsg"));
		}
		if (p.hasPermission("uchat.cmd.clear")){
			p.sendMessage(UCLang.getText("help.cmd.clear"));
		}
		if (p.hasPermission("uchat.cmd.clear-all")){
			p.sendMessage(UCLang.getText("help.cmd.clear-all"));
		}
		if (p.hasPermission("uchat.cmd.spy")){
			p.sendMessage(UCLang.getText("help.cmd.spy"));
		}
		if (p.hasPermission("uchat.cmd.mute")){
			p.sendMessage(UCLang.getText("help.cmd.mute"));
		}
		if (p.hasPermission("uchat.cmd.ignore.player")){
			p.sendMessage(UCLang.getText("help.cmd.ignore.player"));
		}
		if (p.hasPermission("uchat.cmd.ignore.channel")){
			p.sendMessage(UCLang.getText("help.cmd.ignore.channel"));
		}
		if (p.hasPermission("uchat.cmd.reload")){
			p.sendMessage(UCLang.getText("help.cmd.reload"));
		}
		if (p.hasPermission("uchat.cmd.create")){
			p.sendMessage(UCLang.getText("help.cmd.create"));
		}
		StringBuilder channels = new StringBuilder();
		String channelFocus = UCConfig.getInstance().getDefChannel().getName();
		for (UCChannel ch: UCConfig.getInstance().getChannels()){
			String color = "&f";
			if ((p instanceof Player) || UltimateChat.getPerms().channelPerm(p, ch)){

				// Can they both send and receive messages to and from this channel?
				// Extra check here in case the server doesn't use the parent permission and only the children permission nodes
				if (UltimateChat.getPerms().channelPerm(p, ch) || (UltimateChat.getPerms().channelPermReceive(p,ch) && UltimateChat.getPerms().channelPermSend(p,ch))) {
					color = "&b";
				}
				// Can they only receive from this channel?
				else if (UltimateChat.getPerms().channelPermReceive(p,ch)) {
					color = "&e";
				}
				// Can they only send to this channel?
				else if (UltimateChat.getPerms().channelPermSend(p,ch)) {
					color = "&6";
				}

				// Are they ignoring this channel?
				if (ch.isIgnoring(p.getName())) {
					color = "&c";
				}

				// Active channel
				if (ch.getName().equals(UltimateChat.pChannels.get(p.getName()))) {
					channelFocus = ch.getName();
				}

				channels.append(", ").append(color).append(ch.getName()).append("&r");
			}
		}
		p.sendMessage(UCUtil.toText("&7------------------------------------------ "));
		p.sendMessage(UCUtil.toText(UCLang.get("help.channels.available").replace("{channels}", channels.toString().substring(2))));
		p.sendMessage(UCUtil.toText(UCLang.get("help.channels.focus").replace("{channel}", channelFocus)));
		p.sendMessage(UCUtil.toText(UCLang.get("help.channels.key")));
		p.sendMessage(UCUtil.toText("&7------------------------------------------ "));
	}
}
