/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import bleach.hack.module.ModuleManager;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

	@Shadow private int blockBreakingCooldown;
	
	private int getCooldown() {
		return (ModuleManager.getModule("Nuker").isEnabled()
				? (int) ModuleManager.getModule("Nuker").getSetting(3).asSlider().getValue()
						: ModuleManager.getModule("SpeedMine").isEnabled()
						&& ModuleManager.getModule("SpeedMine").getSetting(0).asMode().mode == 1
						? (int) ModuleManager.getModule("SpeedMine").getSetting(2).asSlider().getValue()
								: 5);
	}

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 3),
			require = 0 /* TODO: meteor */)
	public void updateBlockBreakingProgress(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
		this.blockBreakingCooldown = getCooldown();
	}

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 4),
			require = 0 /* TODO: meteor */)
	public void updateBlockBreakingProgress2(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
		this.blockBreakingCooldown = getCooldown();
	}

	@Redirect(method = "attackBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I"),
			require = 0 /* TODO: meteor */)
	public void attackBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
		this.blockBreakingCooldown = getCooldown();
	}
}
