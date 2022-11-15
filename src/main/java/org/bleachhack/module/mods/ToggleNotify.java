/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.ArrayList;
import java.util.List;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;

import net.minecraft.text.Text;



public class ToggleNotify extends Module {
	
	private List<Module> enabledModules = new ArrayList<>();

	public ToggleNotify() {
		super("ToggleNotify", KEY_UNBOUND, ModuleCategory.MISC, "Notifies you in chat when you toggle a module.",
				new SettingToggle("OnEnable", true).withDesc("Notifies you when a module has been enabled"),
				new SettingToggle("OnDisable", true).withDesc("Notifies you when a module has been enabled"),
				new SettingToggle("NotifySelf", false).withDesc("Notifies when ToggleNotify is toggled."));
	}
	
	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);
		
		for (Module m: ModuleManager.getModules()) {
			if (m.isEnabled()) {
				enabledModules.add(m);
			}
		}
		
		if (getSetting(2).asToggle().getState())
			notify(this);
	}
	
	@Override
	public void onDisable(boolean inWorld) {
		enabledModules.clear();
		
		if (getSetting(2).asToggle().getState())
			notify(this);

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		for (int i = 0; i < enabledModules.size(); i++) {
			Module m = enabledModules.get(i);
			if (!m.isEnabled()) {
				if (!(m instanceof ClickGui) && getSetting(1).asToggle().getState())
					notify(m);

				enabledModules.remove(m);
				i--;
			}
		}
		
		for (Module m: ModuleManager.getModules()) {
			if (m.isEnabled() && !enabledModules.contains(m)) {
				if (!(m instanceof ClickGui) && getSetting(0).asToggle().getState())
					notify(m);

				enabledModules.add(m);
			}
		}
	}
	
	private void notify(Module module) {
		BleachLogger.info(Text.literal(
				module.getName() + ": " + (module.isEnabled() ? "\u00a7aEnabled" : "\u00a7cDisabled")));
	}
}
