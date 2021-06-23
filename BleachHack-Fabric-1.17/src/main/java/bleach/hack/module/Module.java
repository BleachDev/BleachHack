/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingBind;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.MinecraftClient;

public class Module {

	public static final int KEY_UNBOUND = -1481058891;

	protected final MinecraftClient mc = MinecraftClient.getInstance();
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
		
		defaultEnabled = enabled;
		if (enabled) {
			setEnabled(true);
		}

		settings.add(new SettingBind(this));
	}

	public void toggle() {
		enabled = !enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void onEnable() {
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

		subscribed = BleachHack.eventBus.subscribe(this);
	}

	public void onDisable() {
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

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
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
		this.key = key;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean toggled) {
		this.enabled = toggled;
		if (toggled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public boolean isDefaultEnabled() {
		return defaultEnabled;
	}

}
