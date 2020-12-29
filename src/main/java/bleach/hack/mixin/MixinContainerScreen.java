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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawContainer;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.AutoDonkeyDupe;
import bleach.hack.module.mods.MountBypass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

@Mixin(HandledScreen.class)
public abstract class MixinContainerScreen extends Screen {

	@Shadow protected int backgroundWidth;
	@Shadow protected int backgroundHeight;

	protected MixinContainerScreen(Text title) {
		super(title);
	}

	@Inject(at = @At("RETURN"), method = "init()V")
	protected void init(CallbackInfo info) {
		if (client.player.getVehicle() instanceof AbstractDonkeyEntity) {
			AbstractDonkeyEntity entity = (AbstractDonkeyEntity) client.player.getVehicle();

			addButton(new ButtonWidget((width - backgroundWidth) / 2 + 82, (height - backgroundHeight) / 2 + 4, 44, 12, new LiteralText("AutoDupe"), button -> {
				ModuleManager.getModule(AutoDonkeyDupe.class).setToggled(true);
			}));

			addButton(new ButtonWidget((width - backgroundWidth) / 2 + 130, (height - backgroundHeight) / 2 + 4, 39, 12, new LiteralText("Dupe"), button -> {
				ModuleManager.getModule(MountBypass.class).dontCancel = true;

				MinecraftClient.getInstance().player.networkHandler.sendPacket(
						new PlayerInteractEntityC2SPacket(
								entity, Hand.MAIN_HAND, entity.getPos().add(entity.getWidth() / 2, entity.getHeight() / 2, entity.getWidth() / 2), false));

				ModuleManager.getModule(MountBypass.class).dontCancel = false;
			}));
		}
	}

	@Inject(at = @At("RETURN"), method = "render")
	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta, CallbackInfo info) {
		EventDrawContainer event = new EventDrawContainer(
				(HandledScreen<?>) client.currentScreen, mouseX, mouseY, matrix); // hmm // hmm?
		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();
	}
}