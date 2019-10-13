package bleach.hack.module.mods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.Event3DRender;
import bleach.hack.event.events.EventTick;
import com.google.common.collect.Iterables;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.Vec3d;

public class Trail extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Trail"),
			new SettingToggle(false, "Keep Trail"),
			new SettingMode("Color: ", "Red", "Green", "Blue", "B2G", "R2B"),
			new SettingSlider(0.1, 10, 3, 1, "Thick: "));
	
	private List<List<Vec3d>> trails = new ArrayList<>();
	
	public Trail() {
		super("Trail", -1, Category.RENDER, "Shows a trail where you go", settings);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		if(!getSettings().get(1).toToggle().state) trails.clear();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(!getSettings().get(0).toToggle().state) return;
		
		if(trails.isEmpty()) trails.add(Arrays.asList(mc.player.getPos().add(0, 0.1, 0), mc.player.getPos()));
		else if(mc.player.getPos().add(0, 0.1, 0).distanceTo(Iterables.getLast(trails).get(1)) > 0.15) {
			trails.add(Arrays.asList(Iterables.getLast(trails).get(1), mc.player.getPos().add(0, 0.1, 0)));
		}
	}

	@Subscribe
	public void onRender(Event3DRender event) {
		Color clr = new Color(0, 0, 0);
		if(getSettings().get(2).toMode().mode == 0) clr = new Color(200, 50, 50);
		else if(getSettings().get(2).toMode().mode == 1) clr = new Color(50, 200, 50);
		else if(getSettings().get(2).toMode().mode == 2) clr = new Color(50, 50, 200);
		
		int count = 250;
		boolean rev = false;
		for(List<Vec3d> e: trails) {
			if(getSettings().get(2).toMode().mode == 3) clr = new Color(50, 255-count, count);
			else if(getSettings().get(2).toMode().mode == 4) clr = new Color(count, 50, 255-count);
			RenderUtils.drawLine(e.get(0).x, e.get(0).y, e.get(0).z, e.get(1).x, e.get(1).y, e.get(1).z,
					clr.getRed()/255f, clr.getGreen()/255f, clr.getBlue()/255f,
					(float) getSettings().get(3).toSlider().getValue());
			if(count < 5 || count > 250) rev = !rev;
			count += rev ? 3 : -3;
		}
	}

}
