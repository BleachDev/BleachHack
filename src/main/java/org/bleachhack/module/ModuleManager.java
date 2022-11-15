/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module;

import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.io.IOUtils;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.collections.NameableStorage;
import org.bleachhack.util.io.BleachJsonHelper;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

	private static final NameableStorage<Module> MODULES = new NameableStorage<>(Module::getName);

	public static void loadModules(InputStream jsonInputStream) {
		InputStreamReader inputReader = new InputStreamReader(jsonInputStream, StandardCharsets.UTF_8);

		try {
			ModuleListJson json = BleachJsonHelper.GSON.fromJson(inputReader, ModuleListJson.class);

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
		if (!MODULES.add(module))
			BleachLogger.logger.error("Failed to load module %s: a module with this name is already loaded.", module.getName());
	}

	public static Iterable<Module> getModules() {
		return MODULES.values();
	}

	public static Module getModule(String name) {
		return MODULES.get(name);
	}
	
	public static <M extends Module> M getModule(Class<M> class_) {
		return MODULES.get(class_);
	}

	public static List<Module> getModulesInCat(ModuleCategory cat) {
		return MODULES.stream().filter(m -> m.getCategory().equals(cat)).collect(Collectors.toList());
	}

	public static void handleKey(int key) {
		if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			for (Module m: getModules()) {
				if (m.getKey() == key) {
					m.toggle();
				}
			}
		}
	}

	private static class ModuleListJson {

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
