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
import java.util.Arrays;
import java.util.List;

import bleach.hack.module.mods.*;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList( // new ArrayList<Module>();
			new AutoTotem(),
			new ChestESP(),
			new ClickGui(),
			new CrystalAura(),
			new ElytraFly(),
			new EntitySpeed(),
			new ESP(),
			new FastUse(),
			new Flight(),
			new Freecam(),
			new Fullbright(),
			new Jesus(),
			new Killaura(),
			new Nametags(),
			new Nofall(),
			new NoSlow(),
			new OffhandCrash(),
			new PacketFly(),
			new Peek(),
			new Scaffold(),
			new Spammer(),
			new Speed(),
			new SpeedMine(),
			new Sprint(),
			new StarGithubPls(),
			new Step(),
			new Tracers(),
			new UI());
	
	public static List<Module> getModules() {
		return mods;
	}
	
	public static Module getModuleByName(String name) {
	    for (Module m: mods) {
	        if (name.equals(m.getName())) return m;
	    }
	    return null;
	}
	
	public static List<Module> getModulesInCat(Category cat) {
		List<Module> mds = new ArrayList<>();
	    for (Module m: mods) {
	        if (m.getCategory().equals(cat)) mds.add(m);
	    }
	    return mds;
	}
	
	public static void onUpdate() {
		for (Module m: mods) {
			m.onUpdate();
		}
	}
	
	public static void onRender() {
		for (Module m: mods) {
			m.onRender();
		}
	}
	
	public static void updateKeys() {
		for (Module m: mods) {
			if (m.getKey().isPressed()) {
				m.toggle();
			}
		}
	}
}
