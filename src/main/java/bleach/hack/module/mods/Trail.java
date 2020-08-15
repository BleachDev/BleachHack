/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.RenderUtils;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trail extends Module {

    private final List<List<Vec3d>> trails = new ArrayList<>();

    public Trail() {
        super("Trail", KEY_UNBOUND, Category.RENDER, "Shows a trail where you go",
                new SettingToggle("Trail", true),
                new SettingToggle("Keep Trail", false),
                new SettingMode("Color", "Red", "Green", "Blue", "B2G", "R2B"),
                new SettingSlider("Thick", 0.1, 10, 3, 1));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!getSetting(1).asToggle().state) trails.clear();
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (!getSetting(0).asToggle().state) return;

        if (trails.isEmpty()) trails.add(Arrays.asList(mc.player.getPos().add(0, 0.1, 0), mc.player.getPos()));
        else if (mc.player.getPos().add(0, 0.1, 0).distanceTo(Iterables.getLast(trails).get(1)) > 0.15) {
            trails.add(Arrays.asList(Iterables.getLast(trails).get(1), mc.player.getPos().add(0, 0.1, 0)));
        }
    }

    @Subscribe
    public void onRender(EventWorldRender event) {
        Color clr = Color.BLACK;
        if (getSetting(2).asMode().mode == 0) clr = new Color(200, 50, 50);
        else if (getSetting(2).asMode().mode == 1) clr = new Color(50, 200, 50);
        else if (getSetting(2).asMode().mode == 2) clr = new Color(50, 50, 200);

        int count = 250;
        boolean rev = false;
        for (List<Vec3d> e : trails) {
            if (getSetting(2).asMode().mode == 3) clr = new Color(50, 255 - count, count);
            else if (getSetting(2).asMode().mode == 4) clr = new Color(count, 50, 255 - count);
            RenderUtils.drawLine(e.get(0).x, e.get(0).y, e.get(0).z, e.get(1).x, e.get(1).y, e.get(1).z,
                    clr.getRed() / 255f, clr.getGreen() / 255f, clr.getBlue() / 255f,
                    (float) getSetting(3).asSlider().getValue());
            if (count < 5 || count > 250) rev = !rev;
            count += rev ? 3 : -3;
        }
    }

}
