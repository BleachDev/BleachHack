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
package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventBlockRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Xray extends Module {

	private Set<Block> visibleBlocks = new HashSet<>();
	private double gamma;

	public Xray() {
		super("Xray", GLFW.GLFW_KEY_X, Category.RENDER, "Baritone is for zoomers",
				new SettingToggle("Fluids", false));
	}

	public boolean isVisible(Block block) {
		return !this.isToggled() || this.visibleBlocks.contains(block);
	}

	public void setVisible(Block... blocks) {
		Collections.addAll(this.visibleBlocks, blocks);
	}

	public void setInvisible(Block... blocks) {
		this.visibleBlocks.removeAll(Arrays.asList(blocks));
	}

	public Set<Block> getVisibleBlocks() {
		return visibleBlocks;
	}

	@Override
	public void onEnable() {
		visibleBlocks.clear();

		for (String s : BleachFileMang.readFileLines("xrayblocks.txt")) {
			setVisible(Registry.BLOCK.get(new Identifier(s)));
		}

		mc.worldRenderer.reload();

		gamma = mc.options.gamma;

		super.onEnable();
	}

	@Override
	public void onDisable() {
		if (mc.world != null)
			mc.worldRenderer.setWorld(mc.world);

		/* for (int i = 0; i <= 15; ++i) { float float_2 = 1.0F - (float) i / 15.0F;
		 * mc.world.dimension.getLightLevelToBrightness()[i] = (1.0F - float_2) /
		 * (float_2 * 3.0F + 1.0F) * 1.0F + 0.0F; } */
		mc.options.gamma = gamma;

		mc.worldRenderer.reload();

		super.onDisable();
	}

	@Subscribe
	public void blockRender(EventBlockRender eventBlockRender) {
		if (this.isVisible(eventBlockRender.getBlockState().getBlock())) {
			eventBlockRender.setCancelled(true);
		}
	}

	@Subscribe
	public void onTick(EventTick eventPreUpdate) {
		mc.options.gamma = 69.420;
	}
}
