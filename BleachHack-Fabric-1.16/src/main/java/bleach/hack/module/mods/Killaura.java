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
package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.world.EntityUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class Killaura extends Module {

	private int delay = 0;

	public Killaura() {
		super("Killaura", GLFW.GLFW_KEY_K, Category.COMBAT, "Automatically attacks entities",
				new SettingToggle("TriggerBot", false).withDesc("Only attack entities in front of you"),
				new SettingToggle("Players", true).withDesc("Attack players"),
				new SettingToggle("Mobs", true).withDesc("Attack mobs"),
				new SettingToggle("Animals", false).withDesc("Attack animals"),
				new SettingToggle("Armor Stands", false).withDesc("Attack armor stands"),
				new SettingRotate(true),
				new SettingToggle("Thru Walls", false).withDesc("Attack through walls"),
				new SettingToggle("1.9 Delay", false).withDesc("Uses the 1.9+ delay between hits"),
				new SettingSlider("Range", 0, 6, 4.25, 2).withDesc("Attack range"),
				new SettingSlider("CPS", 0, 20, 8, 0).withDesc("Attack CPS"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!mc.player.isAlive()) {
			return;
		}

		delay++;
		int reqDelay = (int) Math.round(20 / getSetting(9).asSlider().getValue());
		
		ArrayList<Entity> arrayListWithOneEntry = new ArrayList<Entity>();
		if(mc.crosshairTarget != null && mc.crosshairTarget instanceof EntityHitResult) 
			arrayListWithOneEntry.add(((EntityHitResult)mc.crosshairTarget).getEntity());
		
		Optional<Entity> target = Streams.stream(getSetting(0).asToggle().state ? arrayListWithOneEntry : mc.world.getEntities())
				.filter(e -> !(e instanceof PlayerEntity && BleachHack.friendMang.has(e.getName().getString()))
						&& mc.player.distanceTo(e) <= getSetting(8).asSlider().getValue()
						&& e.isAlive() 
						&& !e.getEntityName().equals(mc.getSession().getUsername())
						&& e != mc.player.getVehicle()
						&& (mc.player.canSee(e) || getSetting(6).asToggle().state))
				.filter(e -> (e instanceof PlayerEntity && getSetting(1).asToggle().state)
						|| (e instanceof Monster && getSetting(2).asToggle().state)
						|| (EntityUtils.isAnimal(e) && getSetting(3).asToggle().state)
						|| (e instanceof ArmorStandEntity && getSetting(4).asToggle().state))
				.sorted((a, b) -> Float.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
				.findFirst();

		if (target.isPresent()) {
			Entity e = target.get();
			
			if (getSetting(5).asRotate().state && !getSetting(0).asToggle().state) {

				WorldUtils.facePosAuto(e.getX(), e.getY() + e.getHeight() / 2, e.getZ(), getSetting(5).asRotate());
			}

			if (((delay > reqDelay || reqDelay == 0) && !getSetting(7).asToggle().state) ||
					(mc.player.getAttackCooldownProgress(mc.getTickDelta()) == 1.0f && getSetting(7).asToggle().state)) {
				boolean wasSprinting = mc.player.isSprinting();

				if (wasSprinting)
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));

				mc.interactionManager.attackEntity(mc.player, e);
				mc.player.swingHand(Hand.MAIN_HAND);

				if (wasSprinting)
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));

				delay = 0;
			}
		}
	}
}
