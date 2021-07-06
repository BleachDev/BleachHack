/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.render.RenderUtils;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.world.EntityUtils;
import bleach.hack.util.world.DamageUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

// i am morbidly obese
public class CrystalAura extends Module {

	private BlockPos render = null;
	private int breakCooldown = 0;
	private int placeCooldown = 0;
	private HashMap<BlockPos, Integer> blacklist = new HashMap<>();

	public CrystalAura() {
		super("CrystalAura", KEY_UNBOUND, ModuleCategory.COMBAT, "Automatically attacks crystals for you.",
				new SettingToggle("Players", true).withDesc("Target players"),
				new SettingToggle("Mobs", false).withDesc("Target mobs"),
				new SettingToggle("Animals", false).withDesc("Target animals"),
				new SettingToggle("Explode", true).withDesc("Hit/explode crystals").withChildren(
						new SettingToggle("AntiWeakness", true).withDesc("Hit with sword when you have weakness"),
						new SettingToggle("AntiSuicide", true).withDesc("Prevents you from killing yourself with a crystal"),
						new SettingSlider("CPT", 1, 10, 2, 0).withDesc("How many crystals to hit per tick"),
						new SettingSlider("Cooldown", 0, 10, 0, 0).withDesc("How many ticks to wait before exploding the next batch of crystals"),
						new SettingSlider("MinHealth", 0, 20, 2, 0).withDesc("Wont explode the crystal if it makes you got below the specified health")),
				new SettingToggle("Place", true).withDesc("Place crystals").withChildren(
						new SettingToggle("AutoSwitch", true).withDesc("Automatically switches to crystal when in combat").withChildren(
								new SettingToggle("SwitchBack", true).withDesc("Switches back to your previous item")),
						new SettingToggle("1.12 Place", false).withDesc("Only places on blocks with 2 air blocks above instead of 1 because of an extra check in 1.12"),
						new SettingToggle("Blacklist", true).withDesc("Blacklists a crystal when it can't place so it doesn't spam packets"),
						new SettingToggle("Force Legit", false).withDesc("Only places a crystal if you can see it"),
						new SettingSlider("MinDmg", 1, 20, 2, 0).withDesc("Minimum damage to the target to place crystals"),
						new SettingSlider("MinRatio", 0.5, 6, 2, 1).withDesc("Minimum damage ratio to place a crystal at (Target dmg/Player dmg)"),
						new SettingSlider("CPT", 1, 10, 2, 0).withDesc("How many crystals to place per tick"),
						new SettingSlider("Cooldown", 0, 10, 0, 0).withDesc("How many ticks to wait before placing the next batch of crystals"),
						new SettingColor("Place Color", 0.7f, 0.7f, 1f, false)),
				new SettingToggle("SameTick", false).withDesc("Enables exploding and placing crystals at the same tick"),
				new SettingRotate(false).withDesc("Rotates to crystals"),
				new SettingSlider("Range", 0, 6, 4.5, 2).withDesc("Range to place and attack crystals"));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		breakCooldown = Math.max(0, breakCooldown - 1);
		placeCooldown = Math.max(0, placeCooldown - 1);

		for (Entry<BlockPos, Integer> e: new HashMap<>(blacklist).entrySet()) {
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
				.filter(e -> !(e instanceof PlayerEntity && BleachHack.friendMang.has(e.getName().getString()))
						&& e.isAlive()
						&& !e.getEntityName().equals(mc.getSession().getUsername())
						&& e != mc.player.getVehicle())
				.filter(e -> (e instanceof PlayerEntity && getSetting(0).asToggle().state)
						|| (e instanceof Monster && getSetting(1).asToggle().state)
						|| (EntityUtils.isAnimal(e) && getSetting(2).asToggle().state))
				.map(e -> (LivingEntity) e)
				.collect(Collectors.toList());

		// Explode
		List<EndCrystalEntity> nearestCrystals = Streams.stream(mc.world.getEntities())
				.filter(e -> (e instanceof EndCrystalEntity))
				.map(e -> {
					blacklist.remove(e.getBlockPos().down());
					return (EndCrystalEntity) e;
				})
				.sorted(Comparator.comparing(c -> mc.player.distanceTo(c)))
				.collect(Collectors.toList());

		int breaks = 0;
		if (getSetting(3).asToggle().state && !nearestCrystals.isEmpty() && breakCooldown <= 0) {
			boolean end = false;
			for (EndCrystalEntity c: nearestCrystals) {
				if (mc.player.distanceTo(c) > getSetting(7).asSlider().getValue()
						|| DamageUtils.willExplosionKill(c.getPos(), 6f, mc.player)
						|| (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - DamageUtils.getExplosionDamage(c.getPos(), 6f, mc.player) 
						< getSetting(3).asToggle().getChild(4).asSlider().getValue()
						|| mc.world.getOtherEntities(null, new Box(c.getPos(), c.getPos()).expand(7), targets::contains).isEmpty()) {
					continue;
				}

				int oldSlot = mc.player.inventory.selectedSlot;
				if (getSetting(3).asToggle().getChild(0).asToggle().state && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
					InventoryUtils.selectSlot(false, true, Comparator.comparing(i -> DamageUtils.getItemAttackDamage(mc.player.inventory.getStack(i))));
				}

				if (getSetting(6).asRotate().state) {
					Vec3d eyeVec = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
					Vec3d v = new Vec3d(c.getX(), c.getY() + 0.5, c.getZ());
					for (Direction d: Direction.values()) {
						Vec3d vd = WorldUtils.getLegitLookPos(c.getBoundingBox(), d, true, 5, -0.001);
						if (vd != null && eyeVec.distanceTo(vd) <= eyeVec.distanceTo(v)) {
							v = vd;
						}
					}

					WorldUtils.facePosAuto(v.x, v.y, v.z, getSetting(6).asRotate());
				}

				mc.interactionManager.attackEntity(mc.player, c);
				mc.player.swingHand(Hand.MAIN_HAND);

				mc.player.inventory.selectedSlot = oldSlot;

				end = true;
				breaks++;
				if (breaks >= getSetting(3).asToggle().getChild(2).asSlider().getValue()) {
					break;
				}
			}

			breakCooldown = getSetting(3).asToggle().getChild(3).asSlider().getValueInt() + 1;

			if (!getSetting(5).asToggle().state && end) {
				return;
			}
		}

		// Place
		if (getSetting(4).asToggle().state && placeCooldown <= 0) {
			int crystalSlot = !getSetting(4).asToggle().getChild(0).asToggle().state
					? (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? mc.player.inventory.selectedSlot
							: mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? 40
									: -1)
							: InventoryUtils.getSlot(true, i -> mc.player.inventory.getStack(i).getItem() == Items.END_CRYSTAL);

			if (crystalSlot == -1) {
				return;
			}

			Map<BlockPos, Float> placeBlocks = new LinkedHashMap<>();

			for (Vec3d v: getCrystalPoses()) {
				float playerDamg = DamageUtils.getExplosionDamage(v, 6f, mc.player);

				if (DamageUtils.willExplosionKill(v, 6f, mc.player)) {
					continue;
				}

				for (LivingEntity e: targets) {
					if (DamageUtils.willExplosionPop(v, 6f, mc.player) && !DamageUtils.willExplosionPopOrKill(v, 6f, e)) {
						continue;
					}

					float targetDamg = DamageUtils.getExplosionDamage(v, 6f, e);

					if (targetDamg >= getSetting(4).asToggle().getChild(4).asSlider().getValue()) {
						float ratio = playerDamg == 0 ? targetDamg : targetDamg / playerDamg;

						if (ratio > getSetting(4).asToggle().getChild(5).asSlider().getValue()) {
							placeBlocks.put(new BlockPos(v).down(), ratio);
						}
					}
				}
			}

			placeBlocks = placeBlocks.entrySet().stream()
					.sorted((b1, b2) -> Float.compare(b2.getValue(), b1.getValue()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

			int oldSlot = mc.player.inventory.selectedSlot;
			int places = 0;
			for (Entry<BlockPos, Float> e: placeBlocks.entrySet()) {
				BlockPos block = e.getKey();

				Vec3d eyeVec = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());

				Vec3d vec = new Vec3d(block.getX(), block.getY() + 0.5, block.getZ());
				Direction dir = Direction.UP;
				for (Direction d: Direction.values()) {
					Vec3d vd = WorldUtils.getLegitLookPos(block, d, true, 5);
					if (vd != null && eyeVec.distanceTo(vd) <= eyeVec.distanceTo(vec)) {
						vec = vd;
					}
				}

				if (getSetting(4).asToggle().getChild(3).asToggle().state
						&& vec.equals(new Vec3d(block.getX(), block.getY() + 0.5, block.getZ()))) {
					return;
				}

				if (getSetting(6).asRotate().state) {
					WorldUtils.facePosAuto(vec.x, vec.y, vec.z, getSetting(6).asRotate());
				}

				Hand hand = InventoryUtils.selectSlot(crystalSlot);

				render = block;
				mc.interactionManager.interactBlock(mc.player, mc.world, hand, new BlockHitResult(vec, dir, block, false));

				places++;
				if (places >= getSetting(4).asToggle().getChild(6).asSlider().getValueInt()) {
					break;
				}
			}

			if (places > 0) {
				if (getSetting(4).asToggle().getChild(0).asToggle().state
						&& getSetting(4).asToggle().getChild(0).asToggle().getChild(0).asToggle().state) {
					mc.player.inventory.selectedSlot = oldSlot;
				}

				placeCooldown = getSetting(4).asToggle().getChild(7).asSlider().getValueInt() + 1;
			}
		}
	}

	@BleachSubscribe
	public void onRenderWorld(EventWorldRender.Post event) {
		if (this.render != null) {
			float[] col = getSetting(4).asToggle().getChild(8).asColor().getRGBFloat();
			RenderUtils.drawBoxBoth(render, QuadColor.single(col[0], col[1], col[2], 0.4f), 2.5f);
		}
	}

	public Set<Vec3d> getCrystalPoses() {
		Set<Vec3d> poses = new HashSet<>();

		int range = (int) Math.floor(getSetting(7).asSlider().getValue());
		for (int x = -range; x <= range; x++) {
			for (int y = -range; y <= range; y++) {
				for (int z = -range; z <= range; z++) {
					BlockPos basePos = mc.player.getBlockPos().add(x, y, z);

					if (!canPlace(basePos) || (blacklist.containsKey(basePos) && getSetting(4).asToggle().getChild(2).asToggle().state))
						continue;

					if (getSetting(4).asToggle().getChild(3).asToggle().state) {
						boolean allBad = true;
						for (Direction d: Direction.values()) {
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

		boolean oldPlace = getSetting(4).asToggle().getChild(1).asToggle().state;
		BlockPos placePos = basePos.up();
		if (!mc.world.isAir(placePos) || (oldPlace && !mc.world.isAir(placePos.up())))
			return false;

		return mc.world.getOtherEntities((Entity) null, new Box(placePos).stretch(0, oldPlace ? 2 : 1, 0)).isEmpty();
	}
}
