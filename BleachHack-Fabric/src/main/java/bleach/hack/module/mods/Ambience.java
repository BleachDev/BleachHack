package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventPreTick;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Weather"),
			new SettingToggle(false, "Time"),
			new SettingMode("Weather: ", "Clear", "Rain"),
			new SettingSlider(0, 2, 0, 2, "Rain:"),
			new SettingSlider(0, 24000, 12500, 0, "Time: "));
	
	public Ambience() {
		super("Ambience", -1, Category.WORLD, "Changes The World Time/Weather", settings);
	}
	
	@Subscribe
	public void onPreTick(EventPreTick event) {
		if(getSettings().get(0).toToggle().state) {
			if(getSettings().get(2).toMode().mode == 0) mc.world.setRainGradient(0f);
			else mc.world.setRainGradient((float) getSettings().get(3).toSlider().getValue());
		}
		if(getSettings().get(1).toToggle().state) {
			mc.world.setTime((long) getSettings().get(4).toSlider().getValue());
			mc.world.setTimeOfDay((long) getSettings().get(4).toSlider().getValue());
		}
	}
	
	@Subscribe
	public void readPacket(EventReadPacket event) {
		if(event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			event.setCancelled(true);
		}
	}

}
