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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventKeyPress;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.Keyboard;

@Mixin(Keyboard.class)
public class MixinKeyboard {

	@Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
	private void onKeyEvent(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
		if (key >= 0) {
			EventKeyPress.Global event = new EventKeyPress.Global(key, scanCode, action, modifiers);
			BleachHack.eventBus.post(event);

			if (event.isCancelled()) {
				callbackInfo.cancel();
			}
		}
	}
	
	@Inject(method = "onKey", at = @At(value = "INVOKE", target = "net/minecraft/client/util/InputUtil.isKeyPressed(JI)Z", ordinal = 5), cancellable = true)
	private void onKeyEvent_1(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
		// TODO: bh setting 
		/*if (InputUtil.getKeycodeName(InputUtil.fromKeyCode(key, scanCode).getKeyCode()) != null &&
			InputUtil.getKeycodeName(InputUtil.fromKeyCode(key, canCode).getKeyCode()).equals(CommandManager.prefix)) {
			MinecraftClient.getInstance().setScreen(new ChatScreen(CommandManager.prefix));
		}*/

		ModuleManager.handleKeyPress(key);

		if (key >= 0) {
			EventKeyPress.InWorld event = new EventKeyPress.InWorld(key, scanCode);
			BleachHack.eventBus.post(event);

			if (event.isCancelled()) {
				callbackInfo.cancel();
			}
		}
	}
}
