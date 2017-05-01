package com.dedotatedwam.ultimatechat;

import com.dedotatedwam.ultimatechat.config.UCConfig;
import com.dedotatedwam.ultimatechat.config.UCLang;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UCListener {
	
	private void sendTell(CommandSource p, Optional<Player> tellreceiver2, String msg){
			
		if (!tellreceiver2.isPresent() || !tellreceiver2.get().isOnline() || p instanceof Player && !((Player)p).canSee(tellreceiver2.get())){
			UCLang.sendMessage(p, "listener.invalidplayer");
			return;
		}		
		Player tellreceiver = tellreceiver2.get();
		UltimateChat.respondTell.put(tellreceiver.getName(),p.getName());
		UCMessages.sendFancyMessage(new String[0], msg, null, p, tellreceiver);			
	}
	
	@Listener
	public void onChat(MessageChannelEvent.Chat e, @First Player p){
		if (UltimateChat.tellPlayers.containsKey(p.getName())){
			Optional<Player> tellreceiver = Sponge.getServer().getPlayer(UltimateChat.tellPlayers.get(p.getName()));
			sendTell(p, tellreceiver, e.getRawMessage().toPlain());
			e.setMessageCancelled(true);
		} else {
			UCChannel ch = UCConfig.getInstance().getChannel(UltimateChat.pChannels.get(p.getName()));
			if (UltimateChat.tempChannels.containsKey(p.getName()) && !UltimateChat.tempChannels.get(p.getName()).equals(ch.getAlias())){
				ch = UCConfig.getInstance().getChannel(UltimateChat.tempChannels.get(p.getName()));
				UltimateChat.tempChannels.remove(p.getName());
			}
			
			if (UltimateChat.mutes.contains(p.getName()) || ch.isMuted(p.getName())){
				UCLang.sendMessage(p, "channel.muted");
				e.setMessageCancelled(true);
				return;
			}			
			
			if (ch.isCmdAlias()){
				String start = ch.getAliasCmd();
				if (start.startsWith("/")){
					start = start.substring(1);
				}
				if (ch.getAliasSender().equalsIgnoreCase("console")){
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(),start+" "+e.getRawMessage());
				} else {
					Sponge.getCommandManager().process(p, start+" "+e.getRawMessage());
				}				
				e.setMessageCancelled(true);
			} else {
				Object[] args = UCMessages.sendFancyMessage(new String[]{
						TextSerializers.FORMATTING_CODE.serialize(e.getFormatter().getHeader().format()),
						TextSerializers.FORMATTING_CODE.serialize(e.getFormatter().getBody().format()),
						TextSerializers.FORMATTING_CODE.serialize(e.getFormatter().getFooter().format())
						}, TextSerializers.FORMATTING_CODE.serialize(e.getRawMessage()), ch, p, null);	
				if (args == null){					
					e.setMessageCancelled(true);
				} else {
					MutableMessageChannel msgCh = (MutableMessageChannel) args[0];	
					msgCh.addMember(Sponge.getServer().getConsole());
					
					e.setChannel(msgCh);
					e.setMessage((Text)args[1], (Text)args[2], (Text)args[3]);
				}										
			}
		}				
	}
	
	@Listener
	public void onJoin(ClientConnectionEvent.Join e){
		Player p = e.getTargetEntity();		
		UltimateChat.pChannels.put(p.getName(), UCConfig.getInstance().getDefChannel().getAlias());
	}
		
	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect e){
		Player p = e.getTargetEntity();	
		List<String> toRemove = new ArrayList<String>();
		for (String play:UltimateChat.tellPlayers.keySet()){
			if (play.equals(p.getName()) || UltimateChat.tellPlayers.get(play).equals(p.getName())){
				toRemove.add(play);				
			}
		}	
		for (String remove:toRemove){
			UltimateChat.tellPlayers.remove(remove);
		}
		List<String> toRemove2 = new ArrayList<String>();
		for (String play:UltimateChat.respondTell.keySet()){
			if (play.equals(p.getName()) || UltimateChat.respondTell.get(play).equals(p.getName())){
				toRemove2.add(play);				
			}
		}	
		for (String remove:toRemove2){
			UltimateChat.respondTell.remove(remove);
		}
		if (UltimateChat.pChannels.containsKey(p.getName())){
			UltimateChat.pChannels.remove(p.getName());
		}
		if (UltimateChat.tempChannels.containsKey(p.getName())){
			UltimateChat.tempChannels.remove(p.getName());
		}
	}
			
}
