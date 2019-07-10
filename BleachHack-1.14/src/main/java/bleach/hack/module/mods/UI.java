package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.IngameOverlay;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UI extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Arraylist"),
			new SettingToggle(false, "FPS"),
			new SettingToggle(false, "Ping"));
	
	private IngameOverlay gui = new IngameOverlay();
	
	public UI() {
		super("UI", -1, Category.RENDER, "Shows stuff onscreen.", settings);
	}
	
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void drawOverlay(RenderGameOverlayEvent.Text event) {
		gui.bottomLeftList.clear();
		if(getSettings().get(0).toToggle().state) gui.drawArrayList();
		if(getSettings().get(1).toToggle().state) gui.addFPS();
		try{ if(getSettings().get(2).toToggle().state) gui.addPing(); }catch(Exception e) {}
		gui.drawBottomLeft();
	}

}
