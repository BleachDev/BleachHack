package bleach.hack.util.rpc;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 */
public class DiscordRichPresence extends Structure {

	/** State of the player's current party. **/
	public String state;
	/** Details to the current game-session of the player. **/
	public String details;
	/** Unix timestamp for the start of the game **/
	public long startTimestamp;
	/** Unix timestamp for when the game will end **/
	public long endTimestamp;
	/** Name of the uploaded image for the large profile artwork. **/
	public String largeImageKey;
	/** Tooltip for the largeImageKey **/
	public String largeImageText;
	/** Name of the uploaded image for the small profile artwork. **/
	public String smallImageKey;
	/** Tooltip for the smallImageKey **/
	public String smallImageText;
	/** Id of the player's party, lobby, or group. **/
	public String partyId;
	/** Current size of the player's party, lobby, or group. **/
	public int partySize;
	/** Maximum size of the player's party, lobby, or group. **/
	public int partyMax;
	/** Unused. **/
	@Deprecated
	public String matchSecret;
	/** Unique hashed string for Spectate button. **/
	public String spectateSecret;
	/** Unique hashed string for chat invitations and Ask to Join. **/
	public String joinSecret;
	/** Unused. **/
	@Deprecated
	public int instance;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("state", "details", "startTimestamp", "endTimestamp", "largeImageKey", "largeImageText", "smallImageKey", "smallImageText", "partyId", "partySize", "partyMax", "matchSecret", "joinSecret", "spectateSecret", "instance");
	}

	/*+
	 * Builder object provided to easily assemble DiscordRichPresence objects without having to add a huge assignment Block.
	 * No method is essential, not called methods/unassigned fields are simply ignored and not applied in the final DiscordRichPresence
	 * seen inside the Discord client.
	 */
	public static class Builder {

		private DiscordRichPresence p;

		/**
		 * Initiates a new instance of the Presence builder.
		 *
		 * @param state String representing the player's current state.
		 * @see DiscordRichPresence
		 */
		public Builder(String state) {
			p = new DiscordRichPresence();
			p.state = state;
		}

		/**
		 * Sets the details field of the DiscordRichPresence object.
		 *
		 * @param details String representing details to the player's current state.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setDetails(String details) {
			p.details = details;
			return this;
		}

		/**
		 * Sets the starting timestamps of the DiscordRichPresence object, to activate the timer display.
		 *
		 * @param start Long Unix Timestamp representing the starting point of the timer.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setStartTimestamps(long start) {
			p.startTimestamp = start;
			return this;
		}

		/**
		 * Sets the ending timestamps of the DiscordRichPresence object, to activate the timer display.
		 *
		 * @param end Long Unix Timestamp representing the ending point of the timer.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setEndTimestamp(long end) {
			p.endTimestamp = end;
			return this;
		}

		/**
		 * Sets the large image fields of the DiscordRichPresence object. key cannot be null when text is not null.
		 *
		 * @param key  String key assigned to the image asset inside of the Discord Application.
		 * @param text String text shown as hover text when hovering over the image of the presence.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setBigImage(String key, String text) {
			if ((text != null && !text.equalsIgnoreCase("")) && key == null)
				throw new IllegalArgumentException("Image key must not be null when assigning a hover text.");

			p.largeImageKey = key;
			p.largeImageText = text;
			return this;
		}

		/**
		 * Sets the small image fields of the DiscordRichPresence object. key cannot be null when text is not null.
		 *
		 * @param key  String key assigned to the image asset inside of the Discord Application.
		 * @param text String text shown as hover text when hovering over the image of the presence.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setSmallImage(String key, String text) {
			if ((text != null && !text.equalsIgnoreCase("")) && key == null)
				throw new IllegalArgumentException("Image key must not be null when assigning a hover text.");

			p.smallImageKey = key;
			p.smallImageText = text;
			return this;
		}

		/**
		 * Sets the party information for the "Party" section of the user's presence.
		 *
		 * @param party Unique String given to the party as identifier.
		 * @param size  Integer representing the current size of the user's party.
		 * @param max   Integer representing the maximal size of the user's party.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setParty(String party, int size, int max) {
			p.partyId = party;
			p.partySize = size;
			p.partyMax = max;
			return this;
		}

		/**
		 * Unused.
		 */
		@Deprecated
		public Builder setSecrets(String match, String join, String spectate) {
			p.matchSecret = match;
			p.joinSecret = join;
			p.spectateSecret = spectate;
			return this;
		}

		/**
		 * Sets the secret fields of the DiscordRichPresence object.
		 *
		 * @param join     Unique String containing necessary information passed to the joining player.
		 * @param spectate Unique String containing necessary information passed to the spectating player.
		 * @return Current Builder object.
		 * @see DiscordRichPresence
		 */
		public Builder setSecrets(String join, String spectate) {
			p.joinSecret = join;
			p.spectateSecret = spectate;
			return this;
		}

		/**
		 * Unused.
		 */
		@Deprecated
		public Builder setInstance(boolean i) {
			p.instance = i ? 1 : 0;
			return this;
		}

		/**
		 * Returns the fully finished DiscordRichPresence object. Non-assigned fields are being ignored.
		 *
		 * @return The build DiscordRichPresence object.
		 * @see DiscordRichPresence
		 */
		public DiscordRichPresence build() {
			return p;
		}
	}
}