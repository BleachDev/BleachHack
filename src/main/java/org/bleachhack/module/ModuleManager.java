/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.bleachhack.util.BleachLogger;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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
		InputStreamReader inputReader = new InputStreamReader(jsonInputStream, StandardCharsets.UTF_8);

		try {
			ModuleListJson json = moduleGson.fromJson(inputReader, ModuleListJson.class);

			for (String moduleString : json.getModules()) {
				try {
					Class<?> moduleClass = Class.forName(String.format("%s.%s", json.getPackage(), moduleString));

					if (Module.class.isAssignableFrom(moduleClass)) {
						try {
							Module module = (Module) moduleClass.getConstructor().newInstance();

							loadModule(module);
						} catch (Exception exception) {
							BleachLogger.logger.error("Failed to load module %s: could not instantiate.", moduleClass);
							exception.printStackTrace();
						}
					} else {
						BleachLogger.logger.error("Failed to load module %s: not a descendant of Module.", moduleClass);
					}
				} catch (Exception exception) {
					BleachLogger.logger.error("Failed to load module %s.", moduleString);
					exception.printStackTrace();
				}
			}
		} finally {
			IOUtils.closeQuietly(inputReader);
		}
	}

	public static void loadModule(Module module) {
		if (modules.containsValue(module)) {
			BleachLogger.logger.error("Failed to load module %s: a module with this name is already loaded.", module.getName());
		} else {
			modules.put(module.getName(), module);
			// TODO: Setup init system for modules
		}
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

	private class ModuleListJson {

		@SerializedName("package")
		private String packageName;

		@SerializedName("modules")
		private List<String> modules;

		public String getPackage() {
			return this.packageName;
		}

		public List<String> getModules() {
			return this.modules;
		}
	}
}
