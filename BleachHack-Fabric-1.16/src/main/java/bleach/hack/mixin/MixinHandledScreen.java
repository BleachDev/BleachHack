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

import java.util.Arrays;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawContainer;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.MountBypass;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
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
				addButton(new ButtonWidget(rightside, topside + 12, 50, 14, new LiteralText("AutoDupe"), button -> {
					ModuleManager.getModule("AutoDonkeyDupe").setEnabled(true);
				}));

				addButton(new ButtonWidget(rightside, topside + 28, 50, 14, new LiteralText("Dupe"), button -> {
					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = true;

					client.player.networkHandler.sendPacket(
							new PlayerInteractEntityC2SPacket(
									entity, Hand.MAIN_HAND, entity.getPos().add(entity.getWidth() / 2, entity.getHeight() / 2, entity.getWidth() / 2), false));

					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = false;
				}));

				addButton(new ButtonWidget(rightside, topside + 66, 50, 14, new LiteralText("Dupe"), button -> {
					double start = client.player.getY();
					for (int i = 0; i < 1000; i++) {
						entity.setPos(client.player.getX(), start + i / 5d, client.player.getZ());
						client.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
						//client.player.getVehicle().setPos(
						//		client.player.getX() - 5, client.player.getY(), client.player.getZ() - 5);
						//client.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(e));
					}

					//client.player.networkHandler.onDisconnected(new LiteralText("aaaaaaaaaaaaaaaaaaaaa"));
				}));
			}
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta, CallbackInfo info) {
		EventDrawContainer event = new EventDrawContainer(
				(HandledScreen<?>) client.currentScreen, mouseX, mouseY, matrix); // hmm // hmm?

		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();

		int rightside = (width + backgroundWidth) / 2 + 2;
		int topside = (height - backgroundHeight) / 2;
		if (client.player.getVehicle() instanceof AbstractDonkeyEntity) {
			textRenderer.drawWithShadow(matrix, "IS Dupe: \u00a79[?]", rightside, topside + 2, -1);
			textRenderer.drawWithShadow(matrix, "ec.me Dupe: \u00a79[?]", rightside, topside + 56, -1);

			if (mouseX >= rightside + textRenderer.getWidth("IS Dupe: ") && mouseX <= rightside + textRenderer.getWidth("IS Dupe: ") + 15
					&& mouseY >= topside + 1 && mouseY <= topside + 11) {
				renderTooltip(matrix, Arrays.asList(
						new LiteralText("\u00a79IllegalStack dupe/Old endcrystal.me dupe"),
						new LiteralText("\u00a79Only works on servers running IllegalStack <= 2.1.0")),
						mouseX, mouseY);
			}

			if (mouseX >= rightside + textRenderer.getWidth("ec.me Dupe: ") && mouseX <= rightside + textRenderer.getWidth("ec.me Dupe: ") + 15
					&& mouseY >= topside + 55 && mouseY <= topside + 65) {
				renderTooltip(matrix, Arrays.asList(
						new LiteralText("\u00a79Endcrystal.me dupe"),
						new LiteralText("\u00a79Endcrystal donkey dc dupe that was active for 2 days, patched 23/02/21"),
						new LiteralText("\u00a79there has to be no blocks above you up to world height for it to work")),
						mouseX, mouseY);
			}
		}
	}
}