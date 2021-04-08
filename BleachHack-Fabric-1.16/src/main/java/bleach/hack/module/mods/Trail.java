/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import net.minecraft.util.math.Vec3d;

public class Trail extends Module {

	private Map<Vec3d, Vec3d> trails = new LinkedHashMap<>();
	private Vec3d lastVec = null;

	public Trail() {
		super("Trail", KEY_UNBOUND, Category.RENDER, "Shows a trail where you go",
				new SettingToggle("Trail", true).withDesc("Enable trailing"),
				new SettingToggle("KeepTrail", false).withDesc("Keep the trail after turning the module off"),
				new SettingColor("Color", 0.8f, 0.2f, 0.2f, false).withDesc("Main trail color"),
				new SettingToggle("BlendColor", true).withDesc("Blends the main color with a second color").withChildren(
						new SettingColor("Second Color", 0.2f, 0.8f, 0.2f, false).withDesc("The second color")),

				new SettingSlider("Width", 0.1, 10, 3, 1).withDesc("Thickness of the trail"),
				new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the trail"));
	}

	@Override
	public void onDisable() {
		if (!getSetting(1).asToggle().state) {
			trails.clear();
		}

		lastVec = null;
		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!getSetting(0).asToggle().state) {
			return;
		}

		if (trails.isEmpty() || lastVec == null) {
			lastVec = mc.player.getPos().add(0, 0.1, 0);
			trails.put(mc.player.getPos(), lastVec);
		} else if (mc.player.getPos().add(0, 0.1, 0).distanceTo(lastVec) > 0.15) {
			trails.put(lastVec, mc.player.getPos().add(0, 0.1, 0));
			lastVec = mc.player.getPos().add(0, 0.1, 0);
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		int color = getSetting(2).asColor().getRGB();

		int count = 250;
		boolean rev = false;
		for (Entry<Vec3d, Vec3d> e : trails.entrySet()) {
			if (getSetting(3).asToggle().state) {
				color = blendColor(getSetting(2).asColor().getRGB(), getSetting(3).asToggle().getChild(0).asColor().getRGB(), count / 255f);
			}

			RenderUtils.drawLine(
					e.getKey().x, e.getKey().y, e.getKey().z,
					e.getValue().x, e.getValue().y, e.getValue().z,
					((color & 0xff0000) >> 16) / 255f, ((color & 0xff00) >> 8) / 255f, (color & 0xff) / 255f,
					(float) getSetting(5).asSlider().getValue(),
					(float) getSetting(4).asSlider().getValue());

			if (count < 5 || count > 250) {
				rev = !rev;
			}

			count += rev ? 3 : -3;
		}
	}

	/* https://stackoverflow.com/questions/19398238/how-to-mix-two-int-colors-correctly moment */
	private int blendColor(int color1, int color2, float ratio) {
		float iRatio = 1.0f - ratio;

		int a1 = (color1 >> 24 & 0xff);
		int r1 = ((color1 & 0xff0000) >> 16);
		int g1 = ((color1 & 0xff00) >> 8);
		int b1 = (color1 & 0xff);

		int a2 = (color2 >> 24 & 0xff);
		int r2 = ((color2 & 0xff0000) >> 16);
		int g2 = ((color2 & 0xff00) >> 8);
		int b2 = (color2 & 0xff);

		int a = (int) ((a1 * iRatio) + (a2 * ratio));
		int r = (int) ((r1 * iRatio) + (r2 * ratio));
		int g = (int) ((g1 * iRatio) + (g2 * ratio));
		int b = (int) ((b1 * iRatio) + (b2 * ratio));

		return a << 24 | r << 16 | g << 8 | b;
	}

}
