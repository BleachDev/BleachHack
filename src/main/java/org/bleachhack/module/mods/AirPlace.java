/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.mixin.AccessorMinecraftClient;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class AirPlace extends Module {

	private boolean pressed;

	public AirPlace() {
		super("AirPlace", KEY_UNBOUND, ModuleCategory.WORLD, "Allows you to place blocks in thin air.",
				new SettingToggle("Highlight", true).withDesc("Renders an overlay where it will place the block.").withChildren(
						new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
						new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The width/thickness of the box lines."),
						new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
						new SettingColor("Color", 128, 128, 128).withDesc("The color of the highlight.")),
				new SettingMode("Mode", "Multi", "Single").withDesc("Whether to place a block once per click or multiple blocks if the button is held down."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		boolean isKeyUsePressed = mc.options.useKey.isPressed();

		if (!canPlaceAtCrosshair()) {
			return;
		}

		if (getSetting(1).asMode().getMode() == 0) {
			if (((AccessorMinecraftClient) mc).getItemUseCooldown() == 4 && isKeyUsePressed) {
				mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget, 0));
			}
		} else if (getSetting(1).asMode().getMode() == 1) {
			if (!pressed && isKeyUsePressed) {
				mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget, 0));
				pressed = true;
			} else if (!isKeyUsePressed) {
				pressed = false;
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (!canPlaceAtCrosshair()) {
			return;
		}

		BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

		int mode = getSetting(0).asToggle().getChild(0).asMode().getMode();
		int[] rgb = getSetting(0).asToggle().getChild(3).asColor().getRGBArray();

		if (mode == 0 || mode == 1) {
			float outlineWidth = getSetting(0).asToggle().getChild(1).asSlider().getValueFloat();
			Renderer.drawBoxOutline(pos, QuadColor.single(rgb[0], rgb[1], rgb[2], 255), outlineWidth);
		}

		if (mode == 0 || mode == 2) {
			int fillAlpha = (int) (getSetting(0).asToggle().getChild(2).asSlider().getValueFloat() * 255);
			Renderer.drawBoxFill(pos, QuadColor.single(rgb[0], rgb[1], rgb[2], fillAlpha));
		}

	}
	
	private boolean canPlaceAtCrosshair() {
		return mc.crosshairTarget instanceof BlockHitResult
				&& mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getMaterial().isReplaceable();
	}
}
