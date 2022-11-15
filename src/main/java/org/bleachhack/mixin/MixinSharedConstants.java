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
import org.bleachhack.module.mods.NoKeyBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.SharedConstants;

@Mixin(SharedConstants.class)
public class MixinSharedConstants {

	@Overwrite
	public static boolean isValidChar(char chr) {
		Module noKeyBlock = ModuleManager.getModule(NoKeyBlock.class);

		if (!noKeyBlock.isEnabled()) {
			return chr != 167 && chr >= ' ' && chr != 127;
		}

		return (noKeyBlock.getSetting(0).asToggle().getState() || chr != 167)
				&& (noKeyBlock.getSetting(1).asToggle().getState() || chr >= ' ')
				&& (noKeyBlock.getSetting(2).asToggle().getState() || chr != 127);
	}
}
