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
package bleach.hack.module;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	private static final Gson moduleGson = new Gson();

	private static final List<Module> modules = new ArrayList<>();

	public static Iterable<Module> getModules() {
		return modules;
	}

	public static void loadModules(InputStream jsonInputStream) {
		ModuleListJson json = moduleGson.fromJson(new InputStreamReader(jsonInputStream), ModuleListJson.class);

		for (String moduleString : json.getModules()) {
			try {
				Class<?> moduleClass = Class.forName(String.format("%s.%s", json.getPackage(), moduleString));

				if (Module.class.isAssignableFrom(moduleClass)) {
					try {
						Module module = (Module) moduleClass.getConstructor().newInstance();

						loadModule(module);
					} catch (Exception exception) {
						System.err.printf("Failed to load module %s: could not instantiate.%n", moduleClass);
						exception.printStackTrace();
					}
				} else {
					System.err.printf("Failed to load module %s: not a descendant of Module.%n", moduleClass);
				}
			} catch (Exception exception) {
				System.err.printf("Failed to load module %s.%n", moduleString);
				exception.printStackTrace();
			}
		}
	}

	public static void loadModule(Module module) {
		if (modules.contains(module)) {
			System.err.printf("Failed to load module %s: a module with this name is already loaded.%n", module.getName());
		} else {
			modules.add(module);
			// TODO: Setup init system for modules
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Module> T getModule(Class<T> clazz) {
		return (T) modules.stream().filter(clazz::isInstance).findFirst().orElse(null);
	}

	public static Module getModule(String name) {
		return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static List<Module> getModulesInCat(Category cat) {
		return modules.stream().filter(m -> m.getCategory().equals(cat)).collect(Collectors.toList());
	}

	// This is slightly improved, but still need to setup an input handler with a map of keys to modules/commands/whatever else
	public static void handleKeyPress(int key) {
		if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			modules.stream().filter(m -> m.getKey() == key).forEach(Module::toggle);
		}
	}
}
