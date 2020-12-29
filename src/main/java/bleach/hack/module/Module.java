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
package bleach.hack.module;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingBind;
import bleach.hack.utils.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;

public class Module {

	public final static int KEY_UNBOUND = -2;

	protected MinecraftClient mc = MinecraftClient.getInstance();
	private String name;
	private int key;
	private int defaultKey;
	private boolean toggled = false;
	private Category category;
	private String desc;
	private List<SettingBase> settings = new ArrayList<>();

	public Module(String nm, int k, Category c, String d, SettingBase... s) {
		name = nm;
		setKey(k);
		defaultKey = getKey();
		category = c;
		desc = d;
		settings = new ArrayList<>(Arrays.asList(s));

		settings.add(new SettingBind(this));
	}

	public void toggle() {
		toggled = !toggled;
		if (toggled)
			onEnable();
		else
			onDisable();
	}

	public void onEnable() {
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

		for (Method method : getClass().getMethods()) {
			if (method.isAnnotationPresent(Subscribe.class)) {
				BleachHack.eventBus.register(this);
				break;
			}
		}
	}

	public void onDisable() {
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;

		try {
			for (Method method : getClass().getMethods()) {
				if (method.isAnnotationPresent(Subscribe.class)) {
					BleachHack.eventBus.unregister(this);
					break;
				}
			}
		} catch (Exception this_didnt_get_registered_hmm_weird) {
			this_didnt_get_registered_hmm_weird.printStackTrace();
		}
	}

	public void init() {
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
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

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
		if (toggled)
			onEnable();
		else
			onDisable();
	}

}
