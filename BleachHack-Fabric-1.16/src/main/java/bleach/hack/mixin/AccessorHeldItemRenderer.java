/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public interface AccessorHeldItemRenderer {
	@Accessor("mainHand")
	void setItemStackMainHand(ItemStack value);

	@Accessor("offHand")
	void setItemStackOffHand(ItemStack value);

	@Accessor("equipProgressMainHand")
	void setEquippedProgressMainHand(float value);

	@Accessor("prevEquipProgressMainHand")
	void setPrevEquippedProgressMainHand(float value);

	@Accessor("equipProgressOffHand")
	void setEquippedProgressOffHand(float value);

	@Accessor("prevEquipProgressOffHand")
	void setPrevEquippedProgressOffHand(float value);
}
