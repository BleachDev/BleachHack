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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import bleach.hack.module.mods.*;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	@SuppressWarnings("unchecked")
	private static final Set<Class<? extends Module>> storedModules = Sets.newHashSet(
			Ambience.class,
			AntiChunkBan.class,
			AntiHunger.class,
			ArrowJuke.class,
			AutoArmor.class,
			AutoBuild.class,
			AutoDonkeyDupe.class,
			AutoParkour.class,
			AutoReconnect.class,
			AutoRespawn.class,
			AutoSign.class,
			AutoSteal.class,
			AutoThrow.class,
			AutoTool.class,
			AutoTotem.class,
			AutoWalk.class,
			BetterPortal.class,
			BlockParty.class,
			BookCrash.class,
			BowBot.class,
			ClickGui.class,
			ClickTp.class,
			ColorSigns.class,
			Criticals.class,
			CrystalAura.class,
			CustomChat.class,
			DiscordRPCMod.class,
			Dispenser32k.class,
			ElytraFly.class,
			EntityControl.class,
			ElytraReplace.class,
			ESP.class,
			FakeLag.class,
			FastUse.class,
			Flight.class,
			Freecam.class,
			Fullbright.class,
			Ghosthand.class,
			HandProgress.class,
			HoleESP.class,
			Jesus.class,
			Killaura.class,
			MountBypass.class,
			MouseFriend.class,
			Nametags.class,
			Nofall.class,
			NoKeyBlock.class,
			NoRender.class,
			NoSlow.class,
			Notebot.class,
			NotebotStealer.class,
			NoVelocity.class,
			Nuker.class,
			OffhandCrash.class,
			PacketFly.class,
			Peek.class,
			PlayerCrash.class,
			RotationSnap.class,
			SafeWalk.class,
			Scaffold.class,
			Search.class,
			ShaderRender.class,
			Spammer.class,
			Speed.class,
			SpeedMine.class,
			Sprint.class,
			StarGithub.class,
			Step.class,
			StorageESP.class,
			Surround.class,
			Timer.class,
			Tracers.class,
			Trail.class,
			Trajectories.class,
			UI.class,
			Xray.class,
			Zoom.class);

	private static final Map<String, Module> modules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public static Iterable<Module> getModules() {
		return modules.values();
	}
	
	public static Iterable<Class<? extends Module>> getStoredModules() {
		return storedModules;
	}

	public static void loadStoredModules(boolean overwrite) {
		for (Class<? extends Module> moduleClass : getStoredModules()) {
			try {
				// Apache really got utils for every thing in the entire universe
				Module module = ConstructorUtils.invokeConstructor(moduleClass);

				loadModule(module, overwrite);
			} catch (Exception exception) {
				System.err.printf("Failed to load module %s: could not instantiate.%n", moduleClass);
				exception.printStackTrace();
			}
		}
	}
	
	public static void loadModule(Module module, boolean overwrite) {
		if (!overwrite && modules.containsKey(module.getName())) {
			System.err.printf("Failed to load module %s: a module with this name is already loaded.%n", module.getName());
		} else {
			modules.put(module.getName(), module);
			// TODO: Setup init system for modules
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

	// This is slightly improved, but still need to setup an input handler with a map of keys to modules/commands/whatever else
	public static void handleKeyPress(int key) {
		if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3)) {
			modules.values().stream().filter(m -> m.getKey() == key).forEach(Module::toggle);
		}
	}
}
