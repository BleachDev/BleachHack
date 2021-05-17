/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	private static final Gson moduleGson = new Gson();

	private static final Map<String, Module> modules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public static Map<String, Module> getModuleMap() {
	    return modules;
    }

	public static Iterable<Module> getModules() {
		return modules.values();
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
		if (modules.containsValue(module)) {
			System.err.printf("Failed to load module %s: a module with this name is already loaded.%n", module.getName());
		} else {
			modules.put(module.getName(), module);
			// TODO: Setup init system for modules
		}
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T extends Module> T getModuleByClass(Class<T> clazz) {
		return (T) modules.values().stream().filter(clazz::isInstance).findFirst().orElse(null);
	}

	public static Module getModule(String name) {
		return modules.get(name);
	}

	public static List<Module> getModulesInCat(ModuleCategory cat) {
		return modules.values().stream().filter(m -> m.getCategory().equals(cat)).collect(Collectors.toList());
	}

	// This is slightly improved, but still need to setup an input handler with a map of keys to modules/commands/whatever else
	public static void handleKeyPress(int key) {
		if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			modules.values().stream().filter(m -> m.getKey() == key).forEach(Module::toggle);
		}
	}
}