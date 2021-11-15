/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bleachhack.BleachHack;
import org.bleachhack.module.setting.base.SettingBase;
import org.bleachhack.module.setting.base.SettingBind;
import org.bleachhack.util.io.BleachFileHelper;

import net.minecraft.client.MinecraftClient;

public class Module {

	public static final int KEY_UNBOUND = -1481058891;

	protected static final MinecraftClient mc = MinecraftClient.getInstance();
	private String name;
	private int key;
	private int defaultKey;
	
	private boolean enabled;
	private final boolean defaultEnabled;
	private boolean subscribed;
	
	private ModuleCategory category;
	private String desc;
	private List<SettingBase> settings = new ArrayList<>();

	public Module(String nm, int k, ModuleCategory c, String d, SettingBase... s) {
		this(nm, k, c, false, d, s);
	}

	public Module(String nm, int k, ModuleCategory c, boolean enabled, String d, SettingBase... s) {
		name = nm;
		setKey(k);
		defaultKey = getKey();
		category = c;
		desc = d;
		settings = new ArrayList<>(Arrays.asList(s));
		settings.add(new SettingBind(this));
		
		defaultEnabled = enabled;
		if (enabled) {
			setEnabled(true);
		}
	}

	public void onEnable(boolean inWorld) {
		BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);

		subscribed = BleachHack.eventBus.subscribe(this);
	}

	public void onDisable(boolean inWorld) {
		BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);

		if (subscribed) {
			BleachHack.eventBus.unsubscribe(this);
		}
	}

	public String getName() {
		return name;
	}

	public ModuleCategory getCategory() {
		return category;
	}

	public String getDesc() {
		return desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getKey() {
		return key;
	}

	public int getDefaultKey() {
		return defaultKey;
	}

	public List<SettingBase> getSettings() {
		return settings;
	}

	public SettingBase getSetting(int s) {
		return settings.get(s);
	}

	public void setKey(int key) {
		BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
		this.key = key;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled)
			toggle();
	}

	public boolean isDefaultEnabled() {
		return defaultEnabled;
	}

	public void toggle() {
		enabled = !enabled;
		if (enabled) {
			onEnable(mc.world != null);
		} else {
			onDisable(mc.world != null);
		}
	}
}
