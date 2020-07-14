package bleach.hack.utils;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;

public class DiscordRPCManager {
	
	public static void start(String id) {
		System.out.println("Initing Discord RPC...");
		
		DiscordRPC.discordInitialize(id, new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
    		System.out.println(user.username + "#" + user.discriminator + " is big gay");
    	}).build(), true);
	}
	
	public static void stop() {
		DiscordRPC.discordShutdown();
	}
}
