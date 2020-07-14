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

import bleach.hack.event.events.EventTick;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.EntityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

public class CrystalAura extends Module {

	private HashMap<Entity, Float> damageCache = new HashMap<>();
	private int delay = 0;

	public CrystalAura() {
		super("CrystalAura", GLFW.GLFW_KEY_I, Category.COMBAT, "Automatically attacks crystals for you.",
				new SettingToggle("Attack", true),
				new SettingToggle("Rotate", false),
				new SettingToggle("Aimbot", false),
				new SettingToggle("Thru Walls", false),
				new SettingSlider("Range: ", 0, 6, 4.25, 2),
				new SettingSlider("CPS: ", 0, 20, 16, 0),
				new SettingToggle("Place", true),
				new SettingSlider("Max Ratio: ", 0, 2, 1, 2),
				new SettingToggle("AntiSuicide", true),
				new SettingMode("AntiPop: ", "Smart", "On", "Off"),
				new SettingToggle("AntiWeakness", true),
				new SettingToggle("Debug Message", false));
	}

	@Subscribe
	public void onTick(EventTick event) {
		damageCache.clear();
		
		// Crystals doesn't work in peaceful
		if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return;

		delay++;
		int reqDelay = (int) Math.round(20/getSettings().get(5).toSlider().getValue());

		if (delay > reqDelay || reqDelay == 0) {
			delay = 0;

			Optional<Entity> firstPlayer = Streams.stream(mc.world.getEntities())
					.filter(e -> e instanceof LivingEntity && mc.player.distanceTo(e) <= 10f && e != mc.player
						&& ((LivingEntity) e).getHealth() > 0f)
					.sorted((e1, e2) -> Float.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player)))
					.findFirst();

			if (!firstPlayer.isPresent()) return;

			Optional<Entity> firstCrystal = Streams.stream(mc.world.getEntities())
					.filter(e -> e instanceof EndCrystalEntity
							&& (!getSettings().get(3).toToggle().state || mc.player.canSee(e))
							&& mc.player.distanceTo(e) <= getSettings().get(4).toSlider().getValue()
							&& (getSettings().get(7).toSlider().getValue() >= getRatio(e, mc.player, (LivingEntity) firstPlayer.get()))
							&& (!getSettings().get(8).toToggle().state || !willKill(e, mc.player))
							&& (getSettings().get(9).toMode().mode != 0 || !(willPop(e, mc.player) && !willPop(e, (LivingEntity) firstPlayer.get()))
							&& (getSettings().get(9).toMode().mode != 1 || !willPop(e, mc.player))))
					.sorted((e1, e2) -> Float.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player)))
					.findFirst();

			if (firstCrystal.isPresent()) {
				if (mc.player.hasStatusEffect(StatusEffects.WEAKNESS) && getSettings().get(10).toToggle().state) {
					for (int i = 0; i < 9; i++) {
						if (mc.player.inventory.getStack(i).getItem() instanceof SwordItem) {
							mc.player.inventory.selectedSlot = i;
							break;
						}
					}
				}

				if (getSettings().get(2).toToggle().state) {
					EntityUtils.facePos(firstCrystal.get().getX(), firstCrystal.get().getY() + firstCrystal.get().getHeight() / 2, firstCrystal.get().getZ());
				} else if (getSettings().get(1).toToggle().state) {
					EntityUtils.facePosPacket(firstCrystal.get().getX(), firstCrystal.get().getY() + firstCrystal.get().getHeight() / 2, firstCrystal.get().getZ());
				}
	
				if (getSettings().get(11).toToggle().state) {
					BleachLogger.infoMessage("Predicted: " + getExplosionDamage(firstCrystal.get().getBlockPos().down(), mc.player)
					+ " vs " + getExplosionDamage(firstCrystal.get().getBlockPos().down(), (LivingEntity) firstPlayer.get()) + " Damage \u00a7a[x"
					+ getRatio(firstCrystal.get(), mc.player, (LivingEntity) firstPlayer.get()) + "]");
				}
				
				mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(firstCrystal.get(), false));
				mc.player.attack(firstCrystal.get());
				mc.player.swingHand(Hand.MAIN_HAND);
				return;
			}
			
			int crystalSlot = -1;
			for (int i = 0; i < 9; i++) {
				if (mc.player.inventory.getStack(i).getItem() == Items.END_CRYSTAL) {
					crystalSlot = i;
					break;
				}
			}
			
			if (crystalSlot == -1) return;
			
			BlockPos bestPos = null;
			float bestRatio = -1f;
			int range = (int) getSettings().get(4).toSlider().getValue() + 1;
			for (int x = -range; x < range + 1; x++) {
				for (int y = -range; y < range; y++) {
					for (int z = -range; z < range + 1; z++) {
						BlockPos basePos = mc.player.getBlockPos().add(x, y, z);
						
						if (!canPlace(basePos)) continue;
						
						if (mc.player.getPos().distanceTo(Vec3d.of(basePos).add(0.5, 1, 0.5))
								> getSettings().get(4).toSlider().getValue() + 0.25) continue;
						
						if (getSettings().get(7).toSlider().getValue() >= getRatio(basePos, mc.player, (LivingEntity) firstPlayer.get())
							&& (!getSettings().get(8).toToggle().state || !willKill(basePos, mc.player))
							&& (getSettings().get(9).toMode().mode != 0 || !(willPop(basePos, mc.player) && !willPop(basePos, (LivingEntity) firstPlayer.get()))
							&& (getSettings().get(9).toMode().mode != 1 || !willPop(basePos, mc.player)))) {
							
							float ratio = getRatio(basePos, mc.player, (LivingEntity) firstPlayer.get());
							if (bestPos == null || ratio < bestRatio) {
								bestPos = new BlockPos(basePos);
								bestRatio = ratio;
							}
						}
					}
				}
			}

			if (bestPos != null) {
				mc.player.inventory.selectedSlot = crystalSlot;
				if (getSettings().get(2).toToggle().state) {
					EntityUtils.facePos(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5);
				} else if (getSettings().get(1).toToggle().state) {
					EntityUtils.facePosPacket(bestPos.getX() + 0.5, bestPos.getY() + 1, bestPos.getZ() + 0.5);
				}

				mc.interactionManager.interactBlock(
						mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(bestPos), Direction.UP, bestPos, false));
			}
		}
	}

	private boolean canPlace(BlockPos basePos) {
		BlockState baseState = mc.world.getBlockState(basePos);
	
		if (baseState.getBlock() != Blocks.BEDROCK && baseState.getBlock() != Blocks.OBSIDIAN) return false;
	
		BlockPos placePos = basePos.up();
		if (!mc.world.isAir(placePos)) return false;
	
		return mc.world.getEntities(null, new Box(placePos).stretch(0, 1, 0)).isEmpty();
	}
	
	private boolean willPop(Entity crystal, LivingEntity target) {
		return willPop(crystal.getBlockPos().down(), target);
	}
	
	private boolean willPop(BlockPos basePos, LivingEntity target) {
		if (target.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING && target.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
			return false;
		}
	
		return getExplosionDamage(basePos, target) >= target.getHealth() + target.getAbsorptionAmount();
	}
	
	private boolean willKill(Entity crystal, LivingEntity target) {
		return willKill(crystal.getBlockPos().down(), target);
	}
	
	private boolean willKill(BlockPos basePos, LivingEntity target) {
		if (target.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || target.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
			return false;
		}
	
		return getExplosionDamage(basePos, target) >= target.getHealth() + target.getAbsorptionAmount();
	}
	
	private float getRatio(Entity crystal, LivingEntity you, LivingEntity target) {
		return getExplosionDamage(crystal, you) / getExplosionDamage(crystal, target);
	}
	
	private float getRatio(BlockPos basePos, LivingEntity you, LivingEntity target) {
		return getExplosionDamage(basePos, you) / getExplosionDamage(basePos, target);
	}
	
	private float getExplosionDamage(Entity crystal, LivingEntity target) {
		return getExplosionDamage(crystal.getBlockPos().down(), target);
	}
	
	private float getExplosionDamage(BlockPos basePos, LivingEntity target) {
		if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0;
		
		if (damageCache.containsKey(target)) return damageCache.get(target);
	
		Vec3d crystalPos = Vec3d.of(basePos).add(0.5, 1, 0.5);
	
		Explosion explosion = new Explosion(mc.world, null, crystalPos.x, crystalPos.y, crystalPos.z, 6f, false, Explosion.DestructionType.DESTROY);
	
		double power = 12;
		int int_3 = MathHelper.floor(crystalPos.x - power - 1.0);
		int int_4 = MathHelper.floor(crystalPos.x + power + 1.0);
		int int_7 = MathHelper.floor(crystalPos.y - power - 1.0);
		int int_8 = MathHelper.floor(crystalPos.y + power + 1.0);
		int int_9 = MathHelper.floor(crystalPos.z - power - 1.0);
		int int_10 = MathHelper.floor(crystalPos.z + power + 1.0);
		List<Entity> list_1 = mc.world.getEntities(null, new Box(int_3, int_7, int_9, int_4, int_8, int_10));
	
		for (int int_11 = 0; int_11 < list_1.size(); ++int_11) {
			if (!list_1.get(int_11).equals(target)) continue;
			LivingEntity entity = (LivingEntity) list_1.get(int_11);
	
			if (!entity.isImmuneToExplosion()) {
				double double_8 = MathHelper.sqrt(entity.squaredDistanceTo(crystalPos)) / power;
				if (double_8 <= 1.0D) {
					double double_9 = entity.getX() - crystalPos.x;
					double double_10 = entity.getEyeY() - crystalPos.y;
					double double_11 = entity.getZ() - crystalPos.z;
					double double_12 = MathHelper.sqrt(double_9 * double_9 + double_10 * double_10 + double_11 * double_11);
					if (double_12 != 0.0D) {
						double_9 /= double_12;
						double_10 /= double_12;
						double_11 /= double_12;
						double double_13 = Explosion.getExposure(crystalPos, entity);
						double double_14 = (1.0D - double_8) * double_13;
	
						//entity_1.damage(explosion.getDamageSource(), (float)((int)((double_14 * double_14 + double_14) / 2.0D * 7.0D * power + 1.0D)));
						float toDamage = (float) Math.floor((double_14 * double_14 + double_14) / 2.0D * 7.0D * power + 1.0D);
	
						if (entity instanceof PlayerEntity) {
							if (mc.world.getDifficulty() == Difficulty.EASY) toDamage = Math.min(toDamage / 2.0F + 1.0F, toDamage);
							else if (mc.world.getDifficulty() == Difficulty.HARD) toDamage = toDamage * 3.0F / 2.0F;
						}
	
						// Armor
						toDamage = DamageUtil.getDamageLeft(toDamage, (float) entity.getArmor(),
								(float) entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());
	
						// Enchantments
						if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
							int resistance = (entity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
							int int_2 = 25 - resistance;
							float resistance_1 = toDamage * (float) int_2;
							toDamage = Math.max(resistance_1 / 25.0F, 0.0F);
						}
	
						if (toDamage <= 0.0F) {
							toDamage = 0.0F;
						} else {
							int protAmount = EnchantmentHelper.getProtectionAmount(entity.getArmorItems(), explosion.getDamageSource());
							if (protAmount > 0) {
								toDamage = DamageUtil.getInflictedDamage(toDamage, (float)int_3);
							}
						}
	
						damageCache.put(entity, toDamage);
						return toDamage;
					}
				}
			}
		}
	
		return 0;
	}

}
