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

import bleach.hack.event.events.EventKeyPress;
import bleach.hack.module.mods.*;
import com.google.common.eventbus.Subscribe;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList(
			new Ambience(),
			new AntiChunkBan(),
			new ArrowJuke(),
			new AutoReconnect(),
			new AutoSign(),
			new AutoTotem(),
			new BlockParty(),
			new BookCrash(),
			new BowBot(),
			new ChestESP(),
			new ChunkSize(),
			new ClickGui(),
			new Criticals(),
			new CrystalAura(),
			new ElytraFly(),
			new EntityControl(),
			new ESP(),
			new FakeLag(),
			new FastUse(),
			new Flight(),
			new Freecam(),
			new Fullbright(),
			new Ghosthand(),
			new Jesus(),
			new Killaura(),
			new Nametags(),
			new Nofall(),
			new NoSlow(),
			new Notebot(),
			new NotebotStealer(),
			new NoVelocity(),
			new Nuker(),
			new OffhandCrash(),
			new PacketFly(),
			new Peek(),
			new PlayerCrash(),
			new Scaffold(),
			new Spammer(),
			new Speed(),
			new SpeedMine(),
			new Sprint(),
			new StarGithub(),
			new Step(),
			new Teleport(),
			new Tracers(),
			new Trail(),
			new UI(),
			new Xray());
	
	public static List<Module> getModules() {
		return mods;
	}

	public static Module getModule(Class<? extends Module> clazz) {
		for(Module module : mods) {
			if(module.getClass().equals(clazz)) {
				return module;
			}
		}

		return null;
	}

	public static Module getModuleByName(String name) {
	    for (Module m: mods) {
	        if (name.equalsIgnoreCase(m.getName())) return m;
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

	@Subscribe
	public static void handleKeyPress(EventKeyPress eventKeyPress) {
		for (Module m : mods) {
			if (m.getKey() == eventKeyPress.getKey()) {
				m.toggle();
			}
		}
	}
}
