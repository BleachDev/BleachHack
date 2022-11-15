/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.event.events.EventInteract;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.operation.Operation;
import org.bleachhack.util.operation.OperationBlueprint;
import org.bleachhack.util.operation.OperationList;
import org.bleachhack.util.operation.PlaceDirOperation;
import org.bleachhack.util.operation.PlaceOperation;
import org.bleachhack.util.operation.RemoveOperation;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class AutoBuild extends Module {

	private static final List<List<OperationBlueprint>> BLUEPRINTS = Arrays.asList(
			Arrays.asList( // Wither
					PlaceOperation.blueprint(0, 0, 0, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(0, 1, 0, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(0, 1, -1, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(0, 1, 1, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(0, 2, -1, Items.WITHER_SKELETON_SKULL),
					PlaceOperation.blueprint(0, 2, 0, Items.WITHER_SKELETON_SKULL),
					PlaceOperation.blueprint(0, 2, 1, Items.WITHER_SKELETON_SKULL)),
			Arrays.asList( // WitherH
					PlaceOperation.blueprint(0, 0, 0, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(1, 0, 0, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(1, 0, -1, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(1, 0, 1, Items.SOUL_SAND, Items.SOUL_SOIL),
					PlaceOperation.blueprint(2, 0, -1, Items.WITHER_SKELETON_SKULL),
					PlaceOperation.blueprint(2, 0, 0, Items.WITHER_SKELETON_SKULL),
					PlaceOperation.blueprint(2, 0, 1, Items.WITHER_SKELETON_SKULL)),
			Arrays.asList( // Iron Golem
					PlaceOperation.blueprint(0, 0, 0, Items.IRON_BLOCK),
					PlaceOperation.blueprint(0, 1, 0, Items.IRON_BLOCK),
					PlaceOperation.blueprint(0, 1, -1, Items.IRON_BLOCK),
					PlaceOperation.blueprint(0, 1, 1, Items.IRON_BLOCK),
					PlaceOperation.blueprint(0, 2, 0, Items.CARVED_PUMPKIN)),
			Arrays.asList( // Snow Golem
					PlaceOperation.blueprint(0, 0, 0, Items.SNOW_BLOCK),
					PlaceOperation.blueprint(0, 1, 0, Items.SNOW_BLOCK),
					PlaceOperation.blueprint(0, 2, 0, Items.CARVED_PUMPKIN)),
			Arrays.asList( // Nomad Hut
					PlaceOperation.blueprint(-2, 0, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 0, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 0, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 0, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 0, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 0, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 0, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 0, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 0, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 0, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-2, 0, 1, Items.OBSIDIAN),
					
					PlaceOperation.blueprint(-2, 1, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 1, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 1, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 1, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 1, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 1, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 1, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-2, 1, 1, Items.OBSIDIAN),
					
					PlaceOperation.blueprint(-2, 2, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 2, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 2, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 2, -2, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 2, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 2, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 2, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 2, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 2, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 2, 2, Items.OBSIDIAN),
					PlaceOperation.blueprint(-2, 2, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(-2, 2, 0, Items.OBSIDIAN),
					
					PlaceOperation.blueprint(-2, 3, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 3, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 3, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(-1, 3, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 3, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 3, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(0, 3, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 3, 0, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 3, -1, Items.OBSIDIAN),
					PlaceOperation.blueprint(1, 3, 1, Items.OBSIDIAN),
					PlaceOperation.blueprint(2, 3, 0, Items.OBSIDIAN)),
			Arrays.asList( // Bomber Mid
					PlaceOperation.blueprint(0, 0, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(0, -1, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(1, -1, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(1, 0, 0, Items.DETECTOR_RAIL),
					PlaceOperation.blueprint(1, -1, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(2, -1, 1, Items.SLIME_BLOCK),
					RemoveOperation.blueprint(1, -1, 1),
					PlaceOperation.blueprint(2, -2, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(3, -2, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					PlaceOperation.blueprint(2, -2, 0, Items.TNT),
					PlaceOperation.blueprint(3, -1, 0, Items.SLIME_BLOCK),
					PlaceDirOperation.blueprint(3, 0, 0, Direction.WEST, Items.OBSERVER),
					PlaceDirOperation.blueprint(4, 0, 0, Direction.WEST, Items.PISTON)),
			Arrays.asList( // Bomber End
					PlaceOperation.blueprint(0, 0, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(0, -1, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(1, -1, 0, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(1, 0, 0, Items.DETECTOR_RAIL),
					PlaceOperation.blueprint(1, -1, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(2, -1, 1, Items.SLIME_BLOCK),
					RemoveOperation.blueprint(1, -1, 1),
					PlaceOperation.blueprint(2, -2, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(3, -2, 1, Items.SLIME_BLOCK),
					PlaceOperation.blueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					PlaceOperation.blueprint(2, -2, 0, Items.TNT),
					PlaceOperation.blueprint(3, -1, 0, Items.SANDSTONE_WALL)));

	private OperationList current = null;
	private BlockHitResult ray = null;
	private boolean active = false;

	public AutoBuild() {
		super("AutoBuild", KEY_UNBOUND, ModuleCategory.WORLD, "Auto builds structures.",
				new SettingMode("Build", "Wither", "WitherH", "IronGolem", "SnowGolem", "NomadHut", "Bomber-Mid", "Bomber-End").withDesc("What to build"),
				new SettingToggle("Repeat", false).withDesc("Lets you build multiple things without having to re-enable the module."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		current = null;
		ray = null;
		active = false;

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!active) {
			ray = (BlockHitResult) mc.player.raycast(40, mc.getTickDelta(), false);
			Direction dir = ray.getSide().getAxis() == Axis.Y ? Direction.fromRotation(mc.player.getYaw()) : ray.getSide();

			current = OperationList.create(BLUEPRINTS.get(getSetting(0).asMode().getMode()), ray.getBlockPos().offset(ray.getSide()), dir);

			if (mc.mouse.wasLeftButtonClicked() || mc.mouse.wasRightButtonClicked()) {
				active = true;
			}
		} else {
			if (current.executeNext() && current.isDone()) {
				setEnabled(false);

				if (getSetting(1).asToggle().getState()) {
					setEnabled(true);
				}
			}
		}
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		if (current != null) {
			//RenderUtils.drawOutlineBox(current.getBox(), 1f, 1f, 0f, 0.5f);

			for (Operation o: current.getRemainingOps()) {
				o.render();
			}

			Renderer.drawBoxOutline(new Box(current.getNext().pos).contract(0.01), QuadColor.single(1f, 1f, 0f, 0.5f), 3f);
		}

		if (ray != null && !active) {
			BlockPos pos = ray.getBlockPos();

			Renderer.drawBoxFill(pos, QuadColor.single(1f, 1f, 0f, 0.3f), ArrayUtils.remove(Direction.values(), ray.getSide().ordinal()));
		}
	}

	@BleachSubscribe
	public void onInteract(EventInteract.InteractBlock event) {
		if (ray != null && !active) {
			event.setCancelled(true);
		}
	}
}