package bleach.hack.util.rpc;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Structure;

/**
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 * <p>
 * Object containing references to all event handlers registered. No callbacks are necessary,
 * every event handler is optional. Non-assigned handlers are being ignored.
 */
public class DiscordEventHandlers extends Structure {
	
	/** Callback called when Discord-RPC was initialized successfully. **/
	public DiscordCallback ready;
	/** Callback called when the Discord connection was disconnected. **/
	public DiscordCallback disconnected;
	/** Callback called when a Discord error occurred. **/
	public DiscordCallback errored;
	/** Callback called when the player joins the game. **/
	public DiscordCallback joinGame;
	/** Callback called when the player spectates a game. **/
	public DiscordCallback spectateGame;
	/** Callback called when a join request is received. **/
	public DiscordCallback joinRequest;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
	}

	public static class Builder {

		DiscordEventHandlers h;

		public Builder() {
			h = new DiscordEventHandlers();
		}

		public Builder withReadyEventHandler(DiscordCallback r) {
			h.ready = r;
			return this;
		}

		public Builder withDisconnectedEventHandler(DiscordCallback d) {
			h.disconnected = d;
			return this;
		}

		public Builder withErroredEventHandler(DiscordCallback e) {
			h.errored = e;
			return this;
		}

		public Builder withJoinGameEventHandler(DiscordCallback j) {
			h.joinGame = j;
			return this;
		}

		public Builder withSpectateGameEventHandler(DiscordCallback s) {
			h.spectateGame = s;
			return this;
		}

		public Builder withJoinRequestEventHandler(DiscordCallback j) {
			h.joinRequest = j;
			return this;
		}

		public DiscordEventHandlers build() {
			return h;
		}
	}
	
	public static interface DiscordCallback extends Callback {
		void apply(DiscordUser request);
	}
	
	public static class DiscordUser extends Structure {

		/** The userId of the player asking to join. **/
		public String userId;
		/** The username of the player asking to join. **/
		public String username;
		/** The discriminator of the player asking to join. **/
		public String discriminator;
		/**
		 * The avatar hash of the player asking to join.
		 *
		 * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting">Image Formatting</a>
		 */
		public String avatar;

		@Override
		public List<String> getFieldOrder() {
			return Arrays.asList("userId", "username", "discriminator", "avatar");
		}
	}

}
