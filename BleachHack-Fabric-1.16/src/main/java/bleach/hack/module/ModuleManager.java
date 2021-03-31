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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventKeyPress;
import bleach.hack.module.mods.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList(
			new Ambience(),
			new AntiChunkBan(),
			new AntiHunger(),
			new ArrowJuke(),
			new AutoArmor(),
			new AutoBuild(),
			new AutoDonkeyDupe(),
			new AutoParkour(),
			new AutoReconnect(),
			new AutoRespawn(),
			new AutoSign(),
			new AutoSteal(),
			new AutoThrow(),
			new AutoTool(),
			new AutoTotem(),
			new AutoWalk(),
			new BetterPortal(),
			new BlockParty(),
			new BookCrash(),
			new BowBot(),
			new ClickGui(),
			new ClickTp(),
			new ColorSigns(),
			new Criticals(),
			new CrystalAura(),
			new CustomChat(),
			new DiscordRPCMod(),
			new Dispenser32k(),
			new ElytraFly(),
			new EntityControl(),
			new ElytraReplace(),
			new ESP(),
			new FakeLag(),
			new FastUse(),
			new Flight(),
			new Freecam(),
			new Fullbright(),
			new Ghosthand(),
			new HandProgress(),
			new Jesus(),
			new Killaura(),
			new MountBypass(),
			new MouseFriend(),
			new Nametags(),
			new Nofall(),
			new NoKeyBlock(),
			new NoRender(),
			new NoSlow(),
			new Notebot(),
			new NotebotStealer(),
			new NoVelocity(),
			new Nuker(),
			new OffhandCrash(),
			new PacketFly(),
			new Peek(),
			new PlayerCrash(),
			new RotationSnap(),
			new SafeWalk(),
			new Scaffold(),
			new ShaderRender(),
			new Spammer(),
			new Speed(),
			new SpeedMine(),
			new Sprint(),
			new StarGithub(),
			new Step(),
			new StorageESP(),
			new Surround(),
			new Timer(),
			new Tracers(),
			new Trail(),
			new Trajectories(),
			new UI(),
			new Xray(),
			new Zoom());

	public static List<Module> getModules() {
		return mods;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getModule(Class<T> clazz) {
		for (Module module : mods) {
			if (clazz.isInstance(module)) {
				return (T) module;
			}
		}

		return null;
	}

	public static Module getModuleByName(String name) {
		for (Module m : mods) {
			if (name.equalsIgnoreCase(m.getName()))
				return m;
		}
		return null;
	}

	public static List<Module> getModulesInCat(Category cat) {
		return mods.stream().filter(m -> m.getCategory().equals(cat)).collect(Collectors.toList());
	}

	@Subscribe
	public static void handleKeyPress(EventKeyPress eventKeyPress) {
		if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3))
			return;

		mods.stream().filter(m -> m.getKey() == eventKeyPress.getKey()).forEach(Module::toggle);
	}
}
