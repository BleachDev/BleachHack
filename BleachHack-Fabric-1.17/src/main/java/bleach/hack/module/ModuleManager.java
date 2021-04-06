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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	private static final Gson gson = new Gson();

	private static final Map<String, Module> modules = new HashMap<>();

	public static Iterable<Module> getModules() {
		return modules.values();
	}

	public static void loadModules(InputStream jsonFileStream) {
		ModulesJson json = gson.fromJson(new InputStreamReader(jsonFileStream), ModulesJson.class);
		for (String moduleClass : json.getModules()) {
			try {
				Class<?> clazz = Class.forName(String.format("%s.%s", json.getPackage(), moduleClass));

				if (Module.class.isAssignableFrom(clazz)) {
					try {
						Module module = (Module) clazz.getConstructors()[0].newInstance();
						if (modules.containsKey(module.getName())) {
							System.err.printf("Failed to load module %s: a module with this name is already loaded.%n", moduleClass);
						} else {
							modules.put(module.getName(), module);
							//TODO Setup init system for modules
						}
					} catch (Exception exception) {
						System.err.printf("Failed to load module %s: could not instantiate.%n", moduleClass);
						exception.printStackTrace();
					}
				} else {
					System.err.printf("Failed to load module %s: not a descendant of Module.%n", moduleClass);
				}
			} catch (Exception exception) {
				System.err.printf("Failed to load module %s.%n", moduleClass);
				exception.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Module> T getModule(Class<T> clazz) {
		return (T) modules.values().stream().filter(clazz::isInstance).findFirst().orElse(null);
	}

	public static Module getModule(String name) {
		return modules.get(name);
	}

	public static List<Module> getModulesInCat(Category cat) {
		return modules.values().stream().filter(m -> m.getCategory().equals(cat)).collect(Collectors.toList());
	}

	//This is slightly improved, but still need to setup an input handler with a map of keys to modules/commands/whatever else
	public static void handleKeyPress(int key) {
		if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			modules.values().stream().filter(m -> m.getKey() == key).forEach(Module::toggle);
		}
	}
}
