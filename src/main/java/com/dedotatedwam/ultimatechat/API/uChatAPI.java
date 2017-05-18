package com.dedotatedwam.ultimatechat.API;

import com.dedotatedwam.ultimatechat.UCChannel;
import com.dedotatedwam.ultimatechat.config.UCConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class uChatAPI {

	/**Register a new tag and write on uChat configuration.
	 * @param tagName - {@code String} with tag name.
	 * @param format - {@code String} format to show on chat.
	 * @param clickCmd - {@code String} for click commands.
	 * @param hoverMessages - {@code List<String>} list with messages to show on mouse hover under tag. 
	 * @return {@code true} if sucess or {@code false} if already registred.
	 */
	public static boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){

		UCConfig config = UCConfig.getInstance();

		if (config.getString("tags",tagName,"format") == null) {

			config.setConfig("tags",tagName,"format", format);
			config.setConfig("tags",tagName,"click-cmd", clickCmd);
			config.setConfig("tags",tagName,"hover-messages", hoverMessages);
			config.save();
			return true;
		}
		return false;
	}

	// TODO Add the two new settings to this API method once they are fully implemented: overrideTagBuilder and customPrefix

	/**Register a new channel and save on channels folder.
	 * @param chName {@code String} - Channel name.
	 * @param chAlias {@code String} - Channel alias.
	 * @param crossWorlds {@code boolean} - Messages in this channel can go to multiple worlds.
	 * @param distance {@code int} - Distance the player will receive this channel messages.
	 * @param color {@code String} - Channel color.
	 * @param tagBuilder {@code String} - Tags names (set on main config) to show on chat.
	 * @param needFocus {@code boolean} - Need to use {@code /ch <alias>} to send messages or not.
	 * @param receiverMsg {@code boolean} - Send message if theres no player to receive the chat message.
	 * @param cost {@code double} - Cost to use this channel.
	 * @param leaveable (@code boolean) - Whether or not the player can ignore the channel, given they have the permission uchat.admin.ignoreoverride.[channel]
	 * @return {@code true} - If registered with sucess or {@code false} if channel alerady registered.
	 * @throws IOException - If can't save the channel file on channels folder.
	 */
	public static boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance,
											 String color, String tagBuilder, boolean needFocus, boolean receiverMsg,
											 double cost, boolean bungee, boolean leaveable) throws IOException{
		UCConfig config = UCConfig.getInstance();
		if (config.getChannel(chName) != null){
			return false;
		}
		// Channel aliases must be unique
		if (config.getChAliases().contains(chAlias)) {
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UCConfig.getInstance().getString("general","default-tag-builder");
		}
		UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg,
				cost, bungee, false, false, "player", "", new ArrayList<String>(),
				true, false, "", leaveable);
		config.addChannel(ch);
		return true;
	}

	/**Register a new channel and save on channels folder. This is used in cases where the other setting will be specified later.
	 * @param chName {@code String} - Channel name.
	 * @param chAlias {@code String} - Channel alias.
	 * @param color {@code String} - Channel color.
	 * @return {@code true} - If registered with sucess or {@code false} if something in the registration went wrong, such as an existing channel name or an invalid input.
	 * @throws IOException - If can't save the channel file on channels folder.
	 */
	public static boolean registerNewChannel(String chName, String chAlias, String color) throws IOException{
		UCConfig config = UCConfig.getInstance();
		if (config.getChannel(chName) != null){
			return false;
		}
		// Channel aliases must be unique
		if (config.getChAliases().contains(chAlias)) {
			return false;
		}
		UCChannel ch = new UCChannel(chName, chAlias, true, 0, color, "", false,
				false, 0, false, false, false, "player",
				"", new ArrayList<String>(), true, false, "", true);
		config.addChannel(ch);
		return true;
	}

	/**Gets an existing channel. Returns null if it cannot find a matching channel name or chanel alias.
	 * @param chName - Chanel name or alias.
	 * @return {@code UCChanel} - The channel.
	 */
	public static UCChannel getChannel(String chName){
		return UCConfig.getInstance().getChannel(chName);
	}
}
