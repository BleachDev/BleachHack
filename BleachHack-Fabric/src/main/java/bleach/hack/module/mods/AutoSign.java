package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.server.network.packet.UpdateSignC2SPacket;
import net.minecraft.text.LiteralText;

public class AutoSign extends Module {

	private String[] text = new String[] {};
	
	public AutoSign() {
		super("AutoSign", -1, Category.PLAYER, "Automatically writes on signs", null);
	}
	
	public void onDisable() {
		text = new String[] {};
	}
	
	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if(event.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
			text = ((UpdateSignC2SPacket) event.getPacket()).getText();
		}
	}
	
	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if(text == null) return;
		
		if(event.getScreen() instanceof SignEditScreen) {
			SignEditScreen screen = (SignEditScreen) event.getScreen();
			SignBlockEntity sign = (SignBlockEntity) FabricReflect.getFieldValue(screen, "field_3031", "sign");
			
			mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), 
					new LiteralText(text[0]), new LiteralText(text[1]), new LiteralText(text[2]), new LiteralText(text[3])));
			event.setCancelled(true);
		}
	}
}
