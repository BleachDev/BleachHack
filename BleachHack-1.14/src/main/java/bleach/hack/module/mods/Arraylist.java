package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.gui.IngameGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Arraylist extends Module {

	public Arraylist() {
		super("Arraylist", -1, Category.RENDER, "Shows a list of toggled modules onscreen.", null);
	}
	
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void drawOverlay(RenderGameOverlayEvent.Text event) {
		if(mc.gameSettings.showDebugInfo) return;
		List<String> lines = new ArrayList<>();
		
		for(Module m: ModuleManager.getModules()) if(m.isToggled()) lines.add(m.getName());
		
		lines.sort((a, b) -> Integer.compare(
				mc.fontRenderer.getStringWidth(b),mc.fontRenderer.getStringWidth(a)));
		
		int count = 0;
		int color = 0x40bbff;
		for(String s: lines) {
			IngameGui.fill(0, 1+(count*10), mc.fontRenderer.getStringWidth(s)+3, 11+(count*10), 0x70000000);
			mc.fontRenderer.drawStringWithShadow(s, 2, 2+(count*10), color);
			color -= 255/ModuleManager.getModules().size();
			count++;
		}
	}

}
