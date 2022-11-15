/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

public class DamageUtils {

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	public static float getItemAttackDamage(ItemStack stack) {
		float damage = 1f
				+ (float) stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
				.stream()
				.mapToDouble(e -> e.getValue())
				.findFirst().orElse(0);

		return damage + EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
	}

	public static float getAttackDamage(PlayerEntity attacker, Entity target) {
		float cooldown = attacker.getAttackCooldownProgress(0.5F);

		float damage = (float) attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
				+ (float) attacker.getMainHandStack().getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE)
				.stream()
				.mapToDouble(e -> e.getValue())
				.findFirst().orElse(0);

		damage *= 0.2f + cooldown * cooldown * 0.8f;
		float enchDamage = EnchantmentHelper.getAttackDamage(
				attacker.getMainHandStack(), target instanceof LivingEntity ? ((LivingEntity) target).getGroup() : EntityGroup.DEFAULT) * cooldown;

		if (damage <= 0f && enchDamage <= 0f) {
			return 0f;
		}

		// Crits
		if (cooldown > 0.9
				&& attacker.fallDistance > 0.0F
				&& !attacker.isOnGround()
				&& !attacker.isClimbing()
				&& !attacker.isTouchingWater()
				&& !attacker.hasStatusEffect(StatusEffects.BLINDNESS)
				&& !attacker.hasVehicle()
				&& !attacker.isSprinting()
				&& target instanceof LivingEntity) {
			damage *= 1.5f;
		}

		damage += enchDamage;

		if (target instanceof LivingEntity) {
			LivingEntity livingTarget = (LivingEntity) target;
			// Armor
			damage = DamageUtil.getDamageLeft(damage, livingTarget.getArmor(),
					(float) livingTarget.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

			// Enchantments
			if (livingTarget.hasStatusEffect(StatusEffects.RESISTANCE)) {
				int resistance = 25 - (livingTarget.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
				float resistance_1 = damage * resistance;
				damage = Math.max(resistance_1 / 25f, 0f);
			}
		}

		if (damage <= 0f) {
			damage = 0f;
		} else {
			int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), DamageSource.player(attacker));
			if (protAmount > 0) {
				damage = DamageUtil.getInflictedDamage(damage, protAmount);
			}
		}

		return damage;
	}

	public static float getExplosionDamage(Vec3d explosionPos, float power, LivingEntity target) {
		if (mc.world.getDifficulty() == Difficulty.PEACEFUL)
			return 0f;

		double maxDist = power * 2;
		if (!mc.world.getOtherEntities(null, new Box(
				MathHelper.floor(explosionPos.x - maxDist - 1.0),
				MathHelper.floor(explosionPos.y - maxDist - 1.0),
				MathHelper.floor(explosionPos.z - maxDist - 1.0),
				MathHelper.floor(explosionPos.x + maxDist + 1.0),
				MathHelper.floor(explosionPos.y + maxDist + 1.0),
				MathHelper.floor(explosionPos.z + maxDist + 1.0))).contains(target)) {
			return 0f;
		}

		if (!target.isImmuneToExplosion() && !target.isInvulnerable()) {
			double distExposure = Math.sqrt(target.squaredDistanceTo(explosionPos)) / maxDist;
			if (distExposure <= 1.0) {
				double xDiff = target.getX() - explosionPos.x;
				double yDiff = target.getEyeY() - explosionPos.y;
				double zDiff = target.getZ() - explosionPos.z;
				double diff = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
				if (diff != 0.0) {
					double exposure = Explosion.getExposure(explosionPos, target);
					double finalExposure = (1.0 - distExposure) * exposure;

					float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

					if (target instanceof PlayerEntity) {
						if (mc.world.getDifficulty() == Difficulty.EASY) {
							toDamage = Math.min(toDamage / 2f + 1f, toDamage);
						} else if (mc.world.getDifficulty() == Difficulty.HARD) {
							toDamage = toDamage * 3f / 2f;
						}
					}

					// Armor
					toDamage = DamageUtil.getDamageLeft(toDamage, target.getArmor(),
							(float) target.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

					// Enchantments
					if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
						int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
						float resistance_1 = toDamage * resistance;
						toDamage = Math.max(resistance_1 / 25f, 0f);
					}

					if (toDamage <= 0f) {
						toDamage = 0f;
					} else {
						int protAmount = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), DamageSource.explosion((LivingEntity) null));
						if (protAmount > 0) {
							toDamage = DamageUtil.getInflictedDamage(toDamage, protAmount);
						}
					}

					return toDamage;
				}
			}
		}

		return 0f;
	}

	public static boolean willKill(LivingEntity target, float damage) {
		if (target.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || target.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
			return false;
		}

		return damage >= target.getHealth() + target.getAbsorptionAmount();
	}

	public static boolean willPop(LivingEntity target, float damage) {
		if (target.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING && target.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
			return false;
		}

		return damage >= target.getHealth() + target.getAbsorptionAmount();
	}

	public static boolean willPopOrKill(LivingEntity target, float damage) {
		return damage >= target.getHealth() + target.getAbsorptionAmount();
	}
	
	public static boolean willGoBelowHealth(LivingEntity target, float damage, float minHealth) {
		return target.getHealth() + target.getAbsorptionAmount() - damage < minHealth;
	}
}
