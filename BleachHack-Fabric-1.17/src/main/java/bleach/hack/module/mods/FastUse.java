/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Set;

import com.google.common.collect.Sets;
import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.FabricReflect;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class FastUse extends Module {

	private static final Set<Item> THROWABLE = Sets.newHashSet(
			Items.SNOWBALL, Items.EGG, Items.EXPERIENCE_BOTTLE,
			Items.ENDER_EYE, Items.ENDER_PEARL, Items.SPLASH_POTION, Items.LINGERING_POTION);

	public FastUse() {
		super("FastUse", KEY_UNBOUND, ModuleCategory.PLAYER, "Allows you to use items faster.",
				new SettingMode("Mode", "Single", "Multi").withDesc("Whether to throw once per tick or multiple times."),
				new SettingSlider("Multi", 1, 100, 20, 0).withDesc("How many items to use per tick if on multi mode."),
				new SettingToggle("Throwables Only", true).withDesc("Only uses throwables.").withChildren(
						new SettingToggle("XP Only", false).withDesc("Only uses XP bottles.")));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (getSetting(2).asToggle().state) {
			if (!(THROWABLE.contains(mc.player.getMainHandStack().getItem())
					&& (!getSetting(2).asToggle().getChild(0).asToggle().state 
						|| mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE))) {
				return;
			}
		}

		/* set rightClickDelay to 0 */
		FabricReflect.writeField(mc, 0, "field_1752", "itemUseCooldown");

		/* call rightClickMouse */
		if (getSetting(0).asMode().mode == 1 && mc.options.keyUse.isPressed()) {
			for (int i = 0; i < getSetting(1).asSlider().getValueInt(); i++) {
				FabricReflect.invokeMethod(mc, "method_1583", "doItemUse");
			}
		}
	}
}
