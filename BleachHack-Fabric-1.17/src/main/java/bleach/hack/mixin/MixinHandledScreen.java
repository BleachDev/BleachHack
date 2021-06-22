/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.MountBypass;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen extends Screen {

	@Shadow protected int backgroundWidth;
	@Shadow protected int backgroundHeight;

	protected MixinHandledScreen(Text title) {
		super(title);
	}

	@Inject(method = "init()V", at = @At("RETURN"))
	protected void init(CallbackInfo info) {
		if (client.player.getVehicle() instanceof HorseBaseEntity) {
			int rightside = (width + backgroundWidth) / 2 + 2;
			int topside = (height - backgroundHeight) / 2;

			HorseBaseEntity entity = (HorseBaseEntity) client.player.getVehicle();

			if (client.player.getVehicle() instanceof AbstractDonkeyEntity) {
				addDrawable(new ButtonWidget(rightside, topside + 12, 50, 14, new LiteralText("AutoDupe"), button -> {
					ModuleManager.getModule("AutoIStackDupe").setEnabled(true);
				}));

				addDrawable(new ButtonWidget(rightside, topside + 28, 50, 14, new LiteralText("Dupe"), button -> {
					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = true;

					client.player.networkHandler.sendPacket(
							PlayerInteractEntityC2SPacket.interactAt(
									entity,
									false,
									Hand.MAIN_HAND,
									entity.getPos().add(entity.getWidth() / 2, entity.getHeight() / 2, entity.getWidth() / 2)));

					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = false;
				}));
			}
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		int rightside = (width + backgroundWidth) / 2 + 2;
		int topside = (height - backgroundHeight) / 2;
		if (client.player.getVehicle() instanceof AbstractDonkeyEntity) {
			textRenderer.drawWithShadow(matrices, "IS Dupe: \u00a79[?]", rightside, topside + 2, -1);

			if (mouseX >= rightside + textRenderer.getWidth("IS Dupe: ") && mouseX <= rightside + textRenderer.getWidth("IS Dupe: ") + 15
					&& mouseY >= topside + 1 && mouseY <= topside + 11) {
				renderTooltip(matrices, Arrays.asList(
						new LiteralText("\u00a79IllegalStack dupe/Old endcrystal.me dupe"),
						new LiteralText("\u00a79Only works on servers running IllegalStack <= 2.1.0")),
						mouseX, mouseY);
			}
		}
	}
}