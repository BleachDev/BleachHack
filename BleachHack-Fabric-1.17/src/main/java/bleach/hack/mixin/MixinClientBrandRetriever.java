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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.gui.ProtocolScreen;
import net.minecraft.client.ClientBrandRetriever;

@Mixin(ClientBrandRetriever.class)
public class MixinClientBrandRetriever {

	@Inject(method = "getClientModName", at = @At("HEAD"), remap = false, cancellable = true)
	private static void getClientModName(CallbackInfoReturnable<String> callback) {
		if (ProtocolScreen.BRAND != null) {
			callback.setReturnValue(ProtocolScreen.BRAND);
		}
	}
}
