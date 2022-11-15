/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import com.google.common.collect.Streams;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingRotate;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.DamageUtils;
import org.bleachhack.util.world.EntityUtils;
import org.bleachhack.util.world.WorldUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

// i am morbidly obese
public class CrystalAura extends Module {

	private BlockPos render = null;
	private int breakCooldown = 0;
	private int placeCooldown = 0;
	private Map<BlockPos, Integer> blacklist = new HashMap<>();

	public CrystalAura() {
		super("CrystalAura", KEY_UNBOUND, ModuleCategory.COMBAT, "Automatically does crystalpvp for you.",
				new SettingToggle("Players", true).withDesc("Targets players."),
				new SettingToggle("Mobs", false).withDesc("Targets mobs."),
				new SettingToggle("Animals", false).withDesc("Targets animals."),
				new SettingToggle("Explode", true).withDesc("Hits/explodes crystals.").withChildren(
						new SettingToggle("AntiWeakness", true).withDesc("Hits crystals with your strongest weapon when you have weakness."),
						new SettingToggle("AntiSuicide", true).withDesc("Prevents you from killing yourself with a crystal."),
						new SettingSlider("CPT", 1, 10, 2, 0).withDesc("How many crystals to hit per tick."),
						new SettingSlider("Cooldown", 0, 10, 0, 0).withDesc("How many ticks to wait before exploding the next batch of crystals."),
						new SettingSlider("MinHealth", 0, 20, 2, 0).withDesc("Wont explode the crystal if it makes you got below the specified health.")),
				new SettingToggle("Place", true).withDesc("Places crystals.").withChildren(
						new SettingToggle("AutoSwitch", true).withDesc("Automatically switches to crystal when in combat.").withChildren(
								new SettingToggle("SwitchBack", true).withDesc("Switches back to your previous item.")),
						new SettingToggle("1.12 Place", false).withDesc("Only places on blocks with 2 air blocks above instead of 1 because of an extra check in pre 1.13."),
						new SettingToggle("Blacklist", true).withDesc("Blacklists a crystal when it can't place so it doesn't spam packets."),
						new SettingToggle("Raycast", false).withDesc("Only places a crystal if you can see it."),
						new SettingSlider("MinDmg", 1, 20, 2, 0).withDesc("Minimum damage to the target to place crystals."),
						new SettingSlider("MinRatio", 0.5, 6, 2, 1).withDesc("Minimum damage ratio to place a crystal at (Target dmg/Player dmg)."),
						new SettingSlider("CPT", 1, 10, 2, 0).withDesc("How many crystals to place per tick."),
						new SettingSlider("Cooldown", 0, 10, 0, 0).withDesc("How many ticks to wait before placing the next batch of crystals."),
						new SettingColor("Place Color", 178, 178, 255).withDesc("The color of the block you're placing crystals on.")),
				new SettingToggle("SameTick", false).withDesc("Enables exploding and placing crystals at the same tick."),
				new SettingRotate(false).withDesc("Rotates to crystals."),
				new SettingSlider("Range", 0, 6, 4.5, 2).withDesc("Range to place and attack crystals."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		breakCooldown = Math.max(0, breakCooldown - 1);
		placeCooldown = Math.max(0, placeCooldown - 1);

		for (Entry<BlockPos, Integer> e : new HashMap<>(blacklist).entrySet()) {
			if (e.getValue() > 0) {
				blacklist.replace(e.getKey(), e.getValue() - 1);
			} else {
				blacklist.remove(e.getKey());
			}
		}

		if (mc.player.isUsingItem() && mc.player.getMainHandStack().isFood()) {
			return;
		}

		List<LivingEntity> targets = Streams.stream(mc.world.getEntities())
				.filter(e -> EntityUtils.isAttackable(e, true))
				.filter(e -> (getSetting(0).asToggle().getState() && EntityUtils.isPlayer(e))
						|| (getSetting(1).asToggle().getState() && EntityUtils.isMob(e))
						|| (getSetting(2).asToggle().getState() && EntityUtils.isAnimal(e)))
				.map(e -> (LivingEntity) e)
				.toList();

		if (targets.isEmpty()) {
			return;
		}

		// Explode
		SettingToggle explodeToggle = getSetting(3).asToggle();
		List<EndCrystalEntity> nearestCrystals = Streams.stream(mc.world.getEntities())
				.filter(e -> e instanceof EndCrystalEntity)
				.map(e -> (EndCrystalEntity) e)
				.sorted(Comparator.comparing(mc.player::distanceTo))
				.toList();

		int breaks = 0;
		if (explodeToggle.getState() && !nearestCrystals.isEmpty() && breakCooldown <= 0) {
			boolean end = false;
			for (EndCrystalEntity c : nearestCrystals) {
				if (mc.player.distanceTo(c) > getSetting(7).asSlider().getValue()
						|| mc.world.getOtherEntities(null, new Box(c.getPos(), c.getPos()).expand(7), targets::contains).isEmpty())
					continue;

				float damage = DamageUtils.getExplosionDamage(c.getPos(), 6f, mc.player);
				if (DamageUtils.willGoBelowHealth(mc.player, damage, explodeToggle.getChild(4).asSlider().getValueFloat()))
					continue;

				int oldSlot = mc.player.getInventory().selectedSlot;
				if (explodeToggle.getChild(0).asToggle().getState() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
					InventoryUtils.selectSlot(false, true, Comparator.comparing(i -> DamageUtils.getItemAttackDamage(mc.player.getInventory().getStack(i))));
				}

				if (getSetting(6).asRotate().getState()) {
					Vec3d eyeVec = mc.player.getEyePos();
					Vec3d v = new Vec3d(c.getX(), c.getY() + 0.5, c.getZ());
					for (Direction d : Direction.values()) {
						Vec3d vd = WorldUtils.getLegitLookPos(c.getBoundingBox(), d, true, 5, -0.001);
						if (vd != null && eyeVec.distanceTo(vd) <= eyeVec.distanceTo(v)) {
							v = vd;
						}
					}

					WorldUtils.facePosAuto(v.x, v.y, v.z, getSetting(6).asRotate());
				}

				mc.interactionManager.attackEntity(mc.player, c);
				mc.player.swingHand(Hand.MAIN_HAND);
				blacklist.remove(c.getBlockPos().down());

				InventoryUtils.selectSlot(oldSlot);

				end = true;
				breaks++;
				if (breaks >= explodeToggle.getChild(2).asSlider().getValue()) {
					break;
				}
			}

			breakCooldown = explodeToggle.getChild(3).asSlider().getValueInt() + 1;

			if (!getSetting(5).asToggle().getState() && end) {
				return;
			}
		}

		// Place
		SettingToggle placeToggle = getSetting(4).asToggle();
		if (placeToggle.getState() && placeCooldown <= 0) {
			int crystalSlot = !placeToggle.getChild(0).asToggle().getState()
					? (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? mc.player.getInventory().selectedSlot
							: mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? 40
									: -1)
							: InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL);

			if (crystalSlot == -1) {
				return;
			}

			Map<BlockPos, Float> placeBlocks = new LinkedHashMap<>();

			for (Vec3d v : getCrystalPoses()) {
				float playerDamg = DamageUtils.getExplosionDamage(v, 6f, mc.player);

				if (DamageUtils.willKill(mc.player, playerDamg))
					continue;

				for (LivingEntity e : targets) {
					float targetDamg = DamageUtils.getExplosionDamage(v, 6f, e);
					if (DamageUtils.willPop(mc.player, playerDamg) && !DamageUtils.willPopOrKill(e, targetDamg)) {
						continue;
					}

					if (targetDamg >= placeToggle.getChild(4).asSlider().getValue()) {
						float ratio = playerDamg == 0 ? targetDamg : targetDamg / playerDamg;

						if (ratio > placeToggle.getChild(5).asSlider().getValue()) {
							placeBlocks.put(new BlockPos(v).down(), ratio);
						}
					}
				}
			}

			placeBlocks = placeBlocks.entrySet().stream()
					.sorted((b1, b2) -> Float.compare(b2.getValue(), b1.getValue()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));

			int oldSlot = mc.player.getInventory().selectedSlot;
			int places = 0;
			for (Entry<BlockPos, Float> e : placeBlocks.entrySet()) {
				BlockPos block = e.getKey();

				Vec3d eyeVec = mc.player.getEyePos();

				Vec3d vec = Vec3d.ofCenter(block, 1);
				Direction dir = null;
				for (Direction d : Direction.values()) {
					Vec3d vd = WorldUtils.getLegitLookPos(block, d, true, 5);
					if (vd != null && eyeVec.distanceTo(vd) <= eyeVec.distanceTo(vec)) {
						vec = vd;
						dir = d;
					}
				}

				if (dir == null) {
					if (placeToggle.getChild(3).asToggle().getState())
						continue;

					dir = Direction.UP;
				}

				if (placeToggle.getChild(2).asToggle().getState())
					blacklist.put(block, 4);

				if (getSetting(6).asRotate().getState()) {
					WorldUtils.facePosAuto(vec.x, vec.y, vec.z, getSetting(6).asRotate());
				}

				Hand hand = InventoryUtils.selectSlot(crystalSlot);

				render = block;
				mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(vec, dir, block, false));

				places++;
				if (places >= placeToggle.getChild(6).asSlider().getValueInt()) {
					break;
				}
			}

			if (places > 0) {
				if (placeToggle.getChild(0).asToggle().getState()
						&& placeToggle.getChild(0).asToggle().getChild(0).asToggle().getState()) {
					InventoryUtils.selectSlot(oldSlot);
				}

				placeCooldown = placeToggle.getChild(7).asSlider().getValueInt() + 1;
			}
		}
	}

	@BleachSubscribe
	public void onRenderWorld(EventWorldRender.Post event) {
		if (this.render != null) {
			int[] col = getSetting(4).asToggle().getChild(8).asColor().getRGBArray();
			Renderer.drawBoxBoth(render, QuadColor.single(col[0], col[1], col[2], 100), 2.5f);
		}
	}

	public Set<Vec3d> getCrystalPoses() {
		Set<Vec3d> poses = new HashSet<>();

		int range = (int) Math.floor(getSetting(7).asSlider().getValue());
		for (int x = -range; x <= range; x++) {
			for (int y = -range; y <= range; y++) {
				for (int z = -range; z <= range; z++) {
					BlockPos basePos = new BlockPos(mc.player.getEyePos()).add(x, y, z);

					if (!canPlace(basePos) || (blacklist.containsKey(basePos) && getSetting(4).asToggle().getChild(2).asToggle().getState()))
						continue;

					if (getSetting(4).asToggle().getChild(3).asToggle().getState()) {
						boolean allBad = true;
						for (Direction d : Direction.values()) {
							if (WorldUtils.getLegitLookPos(basePos, d, true, 5) != null) {
								allBad = false;
								break;
							}
						}

						if (allBad) {
							continue;
						}
					}

					if (mc.player.getPos().distanceTo(Vec3d.of(basePos).add(0.5, 1, 0.5)) <= getSetting(7).asSlider().getValue() + 0.25)
						poses.add(Vec3d.of(basePos).add(0.5, 1, 0.5));
				}
			}
		}

		return poses;
	}

	private boolean canPlace(BlockPos basePos) {
		BlockState baseState = mc.world.getBlockState(basePos);

		if (baseState.getBlock() != Blocks.BEDROCK && baseState.getBlock() != Blocks.OBSIDIAN)
			return false;

		boolean oldPlace = getSetting(4).asToggle().getChild(1).asToggle().getState();
		BlockPos placePos = basePos.up();
		if (!mc.world.isAir(placePos) || (oldPlace && !mc.world.isAir(placePos.up())))
			return false;

		return mc.world.getOtherEntities(null, new Box(placePos, placePos.up(oldPlace ? 2 : 1))).isEmpty();
	}
}
