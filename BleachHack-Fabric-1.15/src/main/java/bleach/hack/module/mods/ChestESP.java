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

import bleach.hack.event.events.Event3DRender;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Items;

public class ChestESP extends Module {
	
	public ChestESP() {
		super("ChestESP", KEY_UNBOUND, Category.RENDER, "Draws a box around storage containers.",
				new SettingToggle("Chests", true),
				new SettingToggle("EndChests", true),
				new SettingToggle("Furnaces", true),
				new SettingToggle("Dispensers", true),
				new SettingToggle("Hoppers", true),
				new SettingToggle("Shulkers", true),
				new SettingToggle("BrewStands", true),
				new SettingToggle("ChestCarts", true),
				new SettingToggle("FurnaceCarts", true),
				new SettingToggle("HopperCarts", true),
				new SettingToggle("ItemFrames", true),
				new SettingToggle("ArmorStands", true));
	}

	@Subscribe
	public void onRender(Event3DRender event) {
		for (BlockEntity e: mc.world.blockEntities) {
			if ((e instanceof ChestBlockEntity || e instanceof BarrelBlockEntity)
					&& getSettings().get(0).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 1.9F, 1.5F, 0.3F, 0.7F);}
			if (e instanceof EnderChestBlockEntity && getSettings().get(1).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 1F, 0.05F, 1F, 0.7F);}
			if ((e instanceof FurnaceBlockEntity || e instanceof SmokerBlockEntity ||
					e instanceof BlastFurnaceBlockEntity) && getSettings().get(2).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.5F, 0.5F, 0.7F);}
			if (e instanceof DispenserBlockEntity && getSettings().get(3).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.55F, 0.55F, 0.7F, 0.7F);}
			if (e instanceof HopperBlockEntity && getSettings().get(4).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.45F, 0.45F, 0.6F, 0.7F);}
			if (e instanceof ShulkerBoxBlockEntity && getSettings().get(5).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.2F, 1F, 0.7F);}
			if (e instanceof BrewingStandBlockEntity && getSettings().get(6).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.4F, 0.2F, 0.7F);}
		}
		
		for (Entity e: mc.world.getEntities()) {
			if (e instanceof ChestMinecartEntity && getSettings().get(7).toToggle().state) {
				RenderUtils.drawFilledBox(e.getBoundingBox(), 1.9F, 1.5F, 0.3F, 0.7F);}
			if (e instanceof FurnaceMinecartEntity && getSettings().get(8).toToggle().state) {
				RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.5F, 0.5F, 0.7F);}
			if (e instanceof HopperMinecartEntity && getSettings().get(9).toToggle().state) {
				RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.45F, 0.6F, 0.7F);}
			
			if (e instanceof ItemFrameEntity && getSettings().get(10).toToggle().state) {
				if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.AIR) {
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.1F, 0.1F, 0.7F);
				} else if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP) {
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.1F, 0.1F, 0.5F, 0.7F);
				} else {
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.1F, 0.45F, 0.1F, 0.7F);
				}
			}
			
			if (e instanceof ArmorStandEntity && getSettings().get(11).toToggle().state) {
				RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.4F, 0.1F, 0.7F);}
		}
	}

}
