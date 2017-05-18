package com.dedotatedwam.ultimatechat;

import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UCChannel implements MutableMessageChannel {

	private String name;
	private String alias;
	private boolean worlds = true;	// Corresponds to across-worlds in the channel config
	private int dist = 0;
	private String color = "&a";
	private String builder = "";
	private boolean focus = false;
	private boolean receiversMsg = false;
	private List<String> ignoring = new ArrayList<String>();
	private List<String> mutes = new ArrayList<String>();
	private double cost = 0.0;
	private boolean bungee = false;
	private boolean ownBuilder = false;
	private boolean isAlias = false;
	private String aliasSender = "";
	private String aliasCmd = "";
	private List<String> availableWorlds = new ArrayList<String>();
	private boolean canLock = true;
	private boolean overrideTagBuilder = false;				// Overrides the tag builder when set to true
	private String customPrefix = ""; 						// Custom prefix before message, used when overrideTagBuilder is set to true
	private boolean leaveable = true;						// Whether or not a player can ignore the channel by doing /chat ignore [channel]
	private List<MessageReceiver> members = new ArrayList<>();		// Used to store the players who are currently joined in on the chat.	// TODO store this in a database for channel persistence

	public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder,
					 boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder,
					 boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, boolean lock,
					 boolean overrideTagBuilder, String customPrefix, boolean leaveable) {
		this.name = name;
		this.alias = alias;
		this.worlds = worlds;
		this.dist = dist;
		this.color = color;
		this.builder = builder;
		this.focus = focus;
		this.receiversMsg = receiversMsg;
		this.cost = cost;
		this.bungee = isbungee;
		this.ownBuilder  = ownBuilder;
		this.isAlias = isAlias;
		this.aliasCmd  = aliasCmd;
		this.aliasSender = aliasSender;
		this.availableWorlds = availableWorlds;	
		this.canLock = lock;
		this.overrideTagBuilder = overrideTagBuilder;
		this.customPrefix = customPrefix;
		this.leaveable = leaveable;
	}
			
	UCChannel(String name) {
		this.name = name;
		this.alias = name.substring(0, 1).toLowerCase();
	}
	
	public boolean canLock(){
		return this.canLock;
	}
	
	boolean availableInWorld(World w){
		return this.availableWorlds.contains(w.getName());
	}
	
	public List<String> availableWorlds(){
		return this.availableWorlds;
	}
	
	public String getAliasCmd(){
		return this.aliasCmd;
	}
	
	public String getAliasSender(){		
		return this.aliasSender;
	}
	
	public boolean isCmdAlias(){
		return this.isAlias;
	}

	// If this returns true, the channel has no custom builder and must use the one specified in the config
	public boolean useOwnBuilder(){
		return this.ownBuilder;
	}
	
	public double getCost(){
		return this.cost;
	}
	
	public void setCost(double cost){
		this.cost = cost;
	}
	
	public void setReceiversMsg(boolean show){
		this.receiversMsg = show;
	}
	
	public boolean getReceiversMsg(){
		return this.receiversMsg;
	}
	
	public void muteThis(String player){
		if (!this.mutes.contains(player)){
			this.mutes.add(player);
		}		
	}
	
	public void unMuteThis(String player){
		if (this.mutes.contains(player)){
			this.mutes.remove(player);
		}		
	}
	
	public boolean isMuted(String player){
		return this.mutes.contains(player);
	}
	
	void ignoreThis(String player){
		if (!this.ignoring.contains(player)){
			this.ignoring.add(player);
		}		
	}
	
	void unIgnoreThis(String player){
		if (this.ignoring.contains(player)){
			this.ignoring.remove(player);
		}		
	}
	
	boolean isIgnoring(String player){
		return this.ignoring.contains(player);
	}
	
	public String[] getBuilder(){
		return this.builder.split(",");
	}
	
	public String getRawBuilder(){
		return this.builder;
	}
	
	public boolean crossWorlds(){
		return this.worlds;
	}
	
	public int getDistance(){
		return this.dist;
	}
	
	public String getColor(){
		return this.color;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getAlias(){
		return this.alias;
	}

	public boolean neeFocus() {
		return this.focus;
	}
	
	public boolean matchChannel(String aliasOrName){
		return this.alias.equalsIgnoreCase(aliasOrName) || this.name.equalsIgnoreCase(aliasOrName);
	}
		
	public boolean isBungee() {		
		return this.bungee ;
	}

	public boolean canOverrideTagBuilder() {
		return overrideTagBuilder;
	}

	public String getCustomPrefix() {
		return customPrefix;
	}

	public boolean isLeaveable() {
		return leaveable;
	}

	@Override
	public Collection<MessageReceiver> getMembers() {
		return Collections.unmodifiableList(this.members);			// So no funny business happens with the members data container
	}

	@Override
	public boolean addMember(MessageReceiver member) {
		return this.members.add(member);
	}

	@Override
	public boolean removeMember(MessageReceiver member) {
		return this.members.remove(member);
	}

	@Override
	public void clearMembers() {
		this.members.clear();
	}

	//TODO In the future all message transformation needs to happen here
	//TODO Manage members of each channel here, then have the config class store every UCChanel instance
	/*public Optional<Text> transformMessage(Object sender, MessageReceiver recipient, Text original) {


	}*/
}
