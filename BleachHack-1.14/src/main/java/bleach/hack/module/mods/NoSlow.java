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
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Slowness"),
			new SettingToggle(true, "Soul Sand"),
			new SettingToggle(true, "Slime Blocks"),
			new SettingToggle(true, "Webs"));
	
	private Vec3d addMotion = Vec3d.ZERO;
	
	public NoSlow() {
		super("NoSlow", -1, Category.MOVEMENT, "Disables Stuff From Slowing You Down", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			
			/* Slowness */
			if(getSettings().get(0).toToggle().state && (mc.player.getActivePotionEffect(Effects.SLOWNESS) != null || mc.player.getActivePotionEffect(Effects.BLINDNESS) != null)) {
				if(mc.gameSettings.keyBindForward.isKeyDown() 
						&& mc.player.getMotion().x > -0.15 && mc.player.getMotion().x < 0.15
						&& mc.player.getMotion().z > -0.15 && mc.player.getMotion().z < 0.15) {
					mc.player.setMotion(mc.player.getMotion().add(addMotion));
					addMotion = addMotion.add(new Vec3d(0, 0, 0.05).rotateYaw(-(float)Math.toRadians(mc.player.rotationYaw)));
				}else addMotion = addMotion.scale(0.75);
			}
			
			/* Soul Sand */
			if(getSettings().get(1).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox(), Blocks.SOUL_SAND)) {
				Vec3d m = new Vec3d(0, 0, 0.125).rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				if(!mc.player.abilities.isFlying && mc.gameSettings.keyBindForward.isKeyDown()) {
					mc.player.setMotion(mc.player.getMotion().add(m));
				}
			}
			
			/* Slime Block */
			if(getSettings().get(2).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox().offset(0,-0.02,0), Blocks.SLIME_BLOCK)) {
				Vec3d m1 = new Vec3d(0, 0, 0.1).rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				if(!mc.player.abilities.isFlying && mc.gameSettings.keyBindForward.isKeyDown()) {
					mc.player.setMotion(mc.player.getMotion().add(m1));
				}
			}
			
			/* Web */
			if(getSettings().get(3).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB)) {
				Vec3d m2 = new Vec3d(0, -1, 0.9).rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw));
				if(!mc.player.abilities.isFlying && mc.gameSettings.keyBindForward.isKeyDown()) {
					mc.player.setMotion(mc.player.getMotion().add(m2));
				}
			}
		}
	}
}
