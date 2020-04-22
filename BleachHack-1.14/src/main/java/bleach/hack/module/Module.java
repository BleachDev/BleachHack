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

import java.util.ArrayList;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Module {

	protected Minecraft mc = Minecraft.getInstance();
	private String name;
	private KeyBinding key;
	private boolean toggled;
	private Category category;
	private String desc;
	private List<SettingBase> settings = new ArrayList<>();
	
	public Module(String nm, int k, Category c, String d, List<SettingBase> s) {
		name = nm;
		registerBind(nm, k);
		category = c;
		desc = d;
		if(s != null) settings = s;
		toggled = false;
	}
	
	
	public void toggle() {
		toggled = !toggled;
		if(toggled) {
			onEnable();
		}else {
			onDisable();
		}
	}
	
	public void onEnable() {}
	public void onDisable() {}
	public void onUpdate() {}
	public void onRender() {}

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

	public KeyBinding getKey() {
		return key;
	}
	
	public List<? extends SettingBase> getSettings() {
		return settings;
	}

	public void setKey(KeyBinding key) {
		this.key = key;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}
	
	/* Ripped from rusherhack because keybindings were the only way i got input to work */
	public void registerBind(String name, int keycode) {
		this.key = new KeyBinding(name, keycode, "BleachHack");
		ClientRegistry.registerKeyBinding(this.key);
	}
	
}
