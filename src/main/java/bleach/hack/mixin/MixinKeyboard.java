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
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventKeyPress;
import net.minecraft.client.Keyboard;

@Mixin(Keyboard.class)
public class MixinKeyboard {
	@Inject(method = "onKey", at = @At(value = "INVOKE", target = "net/minecraft/client/util/InputUtil.isKeyPressed(JI)Z", ordinal = 5), cancellable = true)
	private void onKeyEvent(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callbackInfo) {
		// if (InputUtil.getKeycodeName(InputUtil.fromKeyCode(key,
		// scanCode).getKeyCode()) != null &&
		// InputUtil.getKeycodeName(InputUtil.fromKeyCode(key,
		// scanCode).getKeyCode()).equals(CommandManager.prefix)) {
		// MinecraftClient.getInstance().openScreen(new
		// ChatScreen(CommandManager.prefix));
		// }

		if (key != -1) {
			EventKeyPress event = new EventKeyPress(key, scanCode);
			BleachHack.eventBus.post(event);
			if (event.isCancelled())
				callbackInfo.cancel();
		}
	}
}
