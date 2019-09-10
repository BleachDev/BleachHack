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

	private String[] text;
	
	public AutoSign() {
		super("AutoSign", -1, Category.PLAYER, "Automatically writes on signs", null);
	}
	
	public void onDisable() {
		text = new String[] {};
	}
	
	@Subscribe
	public void sendPacket(EventSendPacket eventSendPacket) {
		if(eventSendPacket.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
			text = ((UpdateSignC2SPacket) eventSendPacket.getPacket()).getText();
		}
	}
	
	@Subscribe
	public void onOpenScreen(EventOpenScreen eventOpenScreen) {
		if(text == null) return;
		
		if(eventOpenScreen.getScreen() instanceof SignEditScreen) {
			SignEditScreen screen = (SignEditScreen) eventOpenScreen.getScreen();
			SignBlockEntity sign = (SignBlockEntity) FabricReflect.getFieldValue(screen, "field_3031", "sign");
			
			mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), 
					new LiteralText(text[0]), new LiteralText(text[1]), new LiteralText(text[2]), new LiteralText(text[3])));
			eventOpenScreen.setCancelled(true);
		}
	}
}
