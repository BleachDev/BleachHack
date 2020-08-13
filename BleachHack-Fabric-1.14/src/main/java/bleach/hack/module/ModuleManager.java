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
import bleach.hack.module.mods.Ambience;
import bleach.hack.module.mods.AntiChunkBan;
import bleach.hack.module.mods.AntiHunger;
import bleach.hack.module.mods.ArrowJuke;
import bleach.hack.module.mods.AutoDonkeyDupe;
import bleach.hack.module.mods.AutoReconnect;
import bleach.hack.module.mods.AutoRespawn;
import bleach.hack.module.mods.AutoSign;
import bleach.hack.module.mods.AutoTool;
import bleach.hack.module.mods.AutoTotem;
import bleach.hack.module.mods.AutoWalk;
import bleach.hack.module.mods.BetterPortal;
import bleach.hack.module.mods.BlockParty;
import bleach.hack.module.mods.BookCrash;
import bleach.hack.module.mods.BowBot;
import bleach.hack.module.mods.ChestESP;
import bleach.hack.module.mods.ChunkSize;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.module.mods.ColorSigns;
import bleach.hack.module.mods.Criticals;
import bleach.hack.module.mods.CrystalAura;
import bleach.hack.module.mods.CustomChat;
import bleach.hack.module.mods.DiscordRPCMod;
import bleach.hack.module.mods.Dispenser32k;
import bleach.hack.module.mods.ESP;
import bleach.hack.module.mods.ElytraFly;
import bleach.hack.module.mods.ElytraReplace;
import bleach.hack.module.mods.EntityControl;
import bleach.hack.module.mods.FakeLag;
import bleach.hack.module.mods.FastUse;
import bleach.hack.module.mods.Flight;
import bleach.hack.module.mods.Freecam;
import bleach.hack.module.mods.Fullbright;
import bleach.hack.module.mods.Ghosthand;
import bleach.hack.module.mods.HandProgress;
import bleach.hack.module.mods.Jesus;
import bleach.hack.module.mods.Killaura;
import bleach.hack.module.mods.MountBypass;
import bleach.hack.module.mods.MouseFriend;
import bleach.hack.module.mods.Nametags;
import bleach.hack.module.mods.NoKeyBlock;
import bleach.hack.module.mods.NoRender;
import bleach.hack.module.mods.NoSlow;
import bleach.hack.module.mods.NoVelocity;
import bleach.hack.module.mods.Nofall;
import bleach.hack.module.mods.Notebot;
import bleach.hack.module.mods.NotebotStealer;
import bleach.hack.module.mods.Nuker;
import bleach.hack.module.mods.OffhandCrash;
import bleach.hack.module.mods.PacketFly;
import bleach.hack.module.mods.Peek;
import bleach.hack.module.mods.PlayerCrash;
import bleach.hack.module.mods.SafeWalk;
import bleach.hack.module.mods.Scaffold;
import bleach.hack.module.mods.Spammer;
import bleach.hack.module.mods.SpeedHack;
import bleach.hack.module.mods.SpeedMine;
import bleach.hack.module.mods.Sprint;
import bleach.hack.module.mods.StarGithub;
import bleach.hack.module.mods.Step;
import bleach.hack.module.mods.Surround;
import bleach.hack.module.mods.Timer;
import bleach.hack.module.mods.Tracers;
import bleach.hack.module.mods.Trail;
import bleach.hack.module.mods.Trajectories;
import bleach.hack.module.mods.UI;
import bleach.hack.module.mods.Xray;
import bleach.hack.module.mods.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList(
			new Ambience(),
			new AntiChunkBan(),
			new AntiHunger(),
			new ArrowJuke(),
			new AutoDonkeyDupe(),
			new AutoReconnect(),
			new AutoRespawn(),
			new AutoSign(),
			new AutoTool(),
			new AutoTotem(),
			new AutoWalk(),
			new BetterPortal(),
			new BlockParty(),
			new BookCrash(),
			new BowBot(),
			new ChestESP(),
			new ChunkSize(),
			new ClickGui(),
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
			new SafeWalk(),
			new Scaffold(),
			new Spammer(),
			new SpeedHack(),
			new SpeedMine(),
			new Sprint(),
			new StarGithub(),
			new Step(),
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

	public static Module getModule(Class<? extends Module> clazz) {
		for (Module module : mods) {
			if (module.getClass().equals(clazz)) {
				return module;
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
		if (InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), GLFW.GLFW_KEY_F3))
			return;

		mods.stream().filter(m -> m.getKey() == eventKeyPress.getKey()).forEach(Module::toggle);
	}
}
