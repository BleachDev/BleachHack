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
package bleach.hack.command.commands;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.BleachQueue;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class CmdInvPeek extends Command {

	@Override
	public String getAlias() {
		return "invpeek";
	}

	@Override
	public String getDescription() {
		return "Shows the inventory of another player in your render distance";
	}

	@Override
	public String getSyntax() {
		return "invpeek <player>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		for (AbstractClientPlayerEntity e: mc.world.getPlayers()) {
			if (e.getDisplayName().getString().equalsIgnoreCase(args[0])) {
				BleachQueue.add(() -> {
					BleachLogger.infoMessage("Opened inventory for " + e.getDisplayName().getString());

					mc.openScreen(new InventoryScreen(e) {
						public boolean mouseClicked(double mouseX, double mouseY, int button) {
							return false;
						}

						protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
							RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
							this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
							int i = this.x;
							int j = this.y;
							this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
							drawEntity(i + 51, j + 75, 30, (float)(i + 51) - mouseX, (float)(j + 75 - 50) - mouseY, e);
						}
					});
				});

				return;
			}
		}

		BleachLogger.errorMessage("Player " + args[0] + " not found!");
	}

}
