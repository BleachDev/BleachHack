/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.SpeedMine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

	@Shadow private PlayerInventory inventory;

	private MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
	private void getBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> ci) {
		Module speedMine = ModuleManager.getModule(SpeedMine.class);

		if (speedMine.isEnabled()) {
			float breakingSpeed = inventory.getBlockBreakingSpeed(block);
			if (breakingSpeed > 1.0F) {
				int eff = EnchantmentHelper.getEfficiency(this);
				ItemStack itemStack_1 = this.getMainHandStack();
				if (eff > 0 && !itemStack_1.isEmpty()) {
					breakingSpeed += eff * eff + 1;
				}
			}

			if (StatusEffectUtil.hasHaste(this)) {
				breakingSpeed *= 1.0F + (StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
			}

			if (!speedMine.getSetting(4).asToggle().getState()) {
				if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
					float fatigueMult;
					switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
						case 0:
							fatigueMult = 0.3F;
							break;
						case 1:
							fatigueMult = 0.09F;
							break;
						case 2:
							fatigueMult = 0.0027F;
							break;
						case 3:
						default:
							fatigueMult = 8.1E-4F;
					}

					breakingSpeed *= fatigueMult;
				}
			}

			if (!speedMine.getSetting(5).asToggle().getState()) {
				if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
					breakingSpeed /= 5.0F;
				}

				if (!this.onGround) {
					breakingSpeed /= 5.0F;
				}
			}

			if (speedMine.getSetting(0).asMode().getMode() == 1)
				breakingSpeed *= speedMine.getSetting(3).asSlider().getValueFloat();

			ci.setReturnValue(breakingSpeed);
		}
	}
}
