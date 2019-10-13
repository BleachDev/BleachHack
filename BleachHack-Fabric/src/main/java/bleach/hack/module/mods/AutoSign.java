package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.server.network.packet.UpdateSignC2SPacket;
import net.minecraft.text.LiteralText;

public class AutoSign extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(false, "Random"));
	
	public String[] text = new String[] {};
	
	public AutoSign() {
		super("AutoSign", -1, Category.PLAYER, "Automatically writes on signs", settings);
	}
	
	public void onDisable() {
		text = new String[] {};
		super.onDisable();
	}
	
	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if(event.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
			text = ((UpdateSignC2SPacket) event.getPacket()).getText();
		}
	}
	
	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if(text.length < 3) return;
		
		if(event.getScreen() instanceof SignEditScreen) {
			event.setCancelled(true);
			
			if(getSettings().get(0).toToggle().state) {
				text =  new String[] {};
				while(text.length < 4) {
					IntStream chars = new Random().ints(0, 0x10FFFF);
					text = chars.limit(1000).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining()).split("(?<=\\G.{250})");
				}
			}
			
			SignEditScreen screen = (SignEditScreen) event.getScreen();
			SignBlockEntity sign = (SignBlockEntity) FabricReflect.getFieldValue(screen, "field_3031", "sign");
			
			mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), 
					new LiteralText(text[0]), new LiteralText(text[1]), new LiteralText(text[2]), new LiteralText(text[3])));
		}
	}
}
