package bleach.hack.module.mods;

import org.apache.commons.lang3.RandomUtils;

import com.google.common.eventbus.Subscribe;

import bleach.hack.command.commands.*;
import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.DiscordRPCManager;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DiscordRPCMod extends Module {

	public static String customText1 = "top text";
	public static String customText2 = "bottom text";
	
	private int tick = 0;
	
	private boolean silent;
	
	public DiscordRPCMod() {
		super("DiscordRPC", KEY_UNBOUND, Category.MISC, "Dicord RPC, use " + "Command.PREFIX" + "rpc to set a custom status",
				new SettingMode("Text 1: ", "Playing %server%", "%server%", "%type%", "%username% ontop", "Minecraft %mcver%", "%username%", "<- bad client", "%custom%"),
				new SettingMode("Text 2: ", "%hp% hp - Holding %item%", "%username% - %hp% hp", "Holding %item%", "%hp% hp - At %coords%", "At %coords%", "%custom%"),
				new SettingMode("Elapsed: ", "Normal", "Random", "Backwards", "None"),
				new SettingToggle("Silent", false));
	}
	
	public void onEnable() {
		silent = getSettings().get(3).toToggle().state;
		
		tick = 0;
		DiscordRPCManager.start(silent ? "727434331089272903" : "725237549563379724");
		
		super.onEnable();
	}
	
	public void onDisable() {
		DiscordRPCManager.stop();
		
		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (silent != getSettings().get(3).toToggle().state) {
			onDisable();
			onEnable();
		}
		
		if (tick % 40 == 0) {
			String text1 = customText1;
			String text2 = customText2;
			long start = 0;
			
			switch (getSettings().get(0).toMode().mode) {
				case 0:
					if (mc.getCurrentServerEntry() != null) text1 = "Playing " + mc.getCurrentServerEntry().address;
					else text1 = "Playing Singleplayer";
					
					break;
				case 1:
					if (mc.getCurrentServerEntry() != null) text1 = mc.getCurrentServerEntry().address;
					else text1 = "Singleplayer";
					
					break;
				case 2:
					if (mc.getCurrentServerEntry() != null) text1 = "Multiplayer";
					else text1 = "Singleplayer";
					
					break;
				case 3:
					text1 = mc.player.getEntityName() + " Ontop!";
					break;
				case 4:
					text1 = "Minecraft " + SharedConstants.getGameVersion().getName();
					break;
				case 5:
					text1 = mc.player.getEntityName();
					break;
				case 6:
					text1 = "<- bad client";
					break;
			}
			
			ItemStack currentItem = mc.player.inventory.getMainHandStack();
			String itemName = currentItem.getItem() == Items.AIR ? "Nothing" :
				(currentItem.getCount() > 1 ? currentItem.getCount() + " " : "") + currentItem.getItem().getName().getString();
			
			switch (getSettings().get(1).toMode().mode) {
				case 0:
					text2 = (int) mc.player.getHealth() + " hp - Holding " + itemName;
					break;
				case 1:
					text2 = mc.player.getEntityName() + " - " + (int) mc.player.getHealth() + " hp";
					break;
				case 2:
					text2 = "Holding " + itemName;
					break;
				case 3:
					text2 = (int) mc.player.getHealth() + " hp - At " + mc.player.getBlockPos().toShortString();
					break;
				case 4:
					text2 = "At " + mc.player.getBlockPos().toShortString();
					break;
			}
			
			switch (getSettings().get(2).toMode().mode) {
				case 0:
					start = System.currentTimeMillis() - tick * 50;
					break;
				case 1:
					start = System.currentTimeMillis() - RandomUtils.nextInt(0, 86400000);
					break;
				case 2:
					start = 1590000000000l + tick * 100;
					break;
			}
			
			DiscordRPC.discordUpdatePresence(
					new DiscordRichPresence.Builder(text2)
					.setBigImage("bleachhack", silent ? "Minecraft " + SharedConstants.getGameVersion().getName() : "BleachHack " + BleachHack.VERSION)
					.setDetails(text1).setStartTimestamps(start).build());
		}
		
		if (tick % 200 == 0) {
			DiscordRPC.discordRunCallbacks();
		}
		
		tick++;
	}
}
