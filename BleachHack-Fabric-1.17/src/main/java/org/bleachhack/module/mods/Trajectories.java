/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingColor;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.LineColor;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.ProjectileSimulator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Trajectories extends Module {

	private List<Triple<List<Vec3d>, Entity, BlockPos>> poses = new ArrayList<>();

	public Trajectories() {
		super("Trajectories", KEY_UNBOUND, ModuleCategory.RENDER, "Shows the trajectories of projectiles.",
				new SettingMode("Draw", "Line", "Dots").withDesc("How to draw trajectories."),
				new SettingToggle("Throwables", true).withDesc("Shows snowballs/eggs/epearls."),
				new SettingToggle("XP Bottles", true).withDesc("Shows trajectories for XP bottles."),
				new SettingToggle("Potions", true).withDesc("Shows trajectories for splash/lingering potions."),
				new SettingToggle("Flying", true).withDesc("Shows trajectories for flying projectiles.").withChildren(
						new SettingToggle("Throwables", true).withDesc("Shows trajectories for flying snowballs/eggs/epearls."),
						new SettingToggle("XP Bottles", true).withDesc("Shows trajectories for flying XP bottles."),
						new SettingToggle("Potions", true).withDesc("Shows trajectories for flying splash/lingering potions.")),
				new SettingToggle("Other Players", false).withDesc("Show other players trajectories."),
				new SettingColor("Color", 1f, 0.3f, 1f, false).withDesc("The color of the trajectories."),

				new SettingSlider("Width", 0.1, 5, 2, 2).withDesc("Thickness of the trajectories."),
				new SettingSlider("Opacity", 0, 1, 0.7, 2).withDesc("Opacity of the trajectories."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		poses.clear();

		Entity entity = ProjectileSimulator.summonProjectile(
				mc.player, getSetting(1).asToggle().state, getSetting(2).asToggle().state, getSetting(3).asToggle().state);

		if (entity != null) {
			poses.add(ProjectileSimulator.simulate(entity));
		}

		if (getSetting(4).asToggle().state) {
			for (Entity e : mc.world.getEntities()) {
				if (e instanceof ThrownEntity || e instanceof PersistentProjectileEntity) {
					if (!getSetting(4).asToggle().getChild(0).asToggle().state
							&& (e instanceof SnowballEntity || e instanceof EggEntity || e instanceof EnderPearlEntity)) {
						continue;
					}

					if (!getSetting(4).asToggle().getChild(1).asToggle().state && e instanceof ExperienceBottleEntity) {
						continue;
					}

					if (!mc.world.getBlockCollisions(e, e.getBoundingBox()).allMatch(VoxelShape::isEmpty))
						continue;

					Triple<List<Vec3d>, Entity, BlockPos> p = ProjectileSimulator.simulate(e);

					if (p.getLeft().size() >= 2)
						poses.add(p);
				}
			}
		}

		if (getSetting(5).asToggle().state) {
			for (PlayerEntity e : mc.world.getPlayers()) {
				if (e == mc.player)
					continue;

				Entity proj = ProjectileSimulator.summonProjectile(
						e, getSetting(1).asToggle().state, getSetting(2).asToggle().state, getSetting(3).asToggle().state);

				if (proj != null) {
					poses.add(ProjectileSimulator.simulate(proj));
				}
			}

		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		float[] col = getSetting(6).asColor().getRGBFloat();

		for (Triple<List<Vec3d>, Entity, BlockPos> t : poses) {
			if (t.getLeft().size() >= 2) {
				if (getSetting(0).asMode().mode == 0) {
					for (int i = 1; i < t.getLeft().size(); i++) {
						Renderer.drawLine(
								t.getLeft().get(i - 1).x, t.getLeft().get(i - 1).y, t.getLeft().get(i - 1).z,
								t.getLeft().get(i).x, t.getLeft().get(i).y, t.getLeft().get(i).z,
								LineColor.single(col[0], col[1], col[2], getSetting(8).asSlider().getValueFloat()),
								getSetting(7).asSlider().getValueFloat());
					}
				} else {
					for (Vec3d v : t.getLeft()) {
						Renderer.drawBoxFill(new Box(v, v).expand(0.08), QuadColor.single(col[0], col[1], col[2], getSetting(8).asSlider().getValueFloat()));
					}
				}
			}

			VoxelShape hitbox = t.getMiddle() != null ? VoxelShapes.cuboid(t.getMiddle().getBoundingBox())
					: t.getRight() != null ? mc.world.getBlockState(t.getRight()).getCollisionShape(mc.world, t.getRight()).offset(t.getRight().getX(), t.getRight().getY(), t.getRight().getZ())
							: null;
			Vec3d lastVec = !t.getLeft().isEmpty() ? t.getLeft().get(t.getLeft().size() - 1)
					: mc.player.getEyePos();

			if (hitbox != null) {
				Renderer.drawLine(lastVec.x + 0.25, lastVec.y, lastVec.z, lastVec.x - 0.25, lastVec.y, lastVec.z, LineColor.single(col[0], col[1], col[2], 1f), 1.75f);
				Renderer.drawLine(lastVec.x, lastVec.y + 0.25, lastVec.z, lastVec.x, lastVec.y - 0.25, lastVec.z, LineColor.single(col[0], col[1], col[2], 1f), 1.75f);
				Renderer.drawLine(lastVec.x, lastVec.y, lastVec.z + 0.25, lastVec.x, lastVec.y, lastVec.z - 0.25, LineColor.single(col[0], col[1], col[2], 1f), 1.75f);

				for (Box box: hitbox.getBoundingBoxes()) {
					Renderer.drawBoxOutline(box, QuadColor.single(col[0], col[1], col[2], 0.75f), 1f);
				}
			}
		}
	}
}
