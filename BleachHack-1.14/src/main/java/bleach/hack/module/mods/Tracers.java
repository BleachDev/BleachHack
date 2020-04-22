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
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Players"),
			new SettingToggle(false, "Mobs"),
			new SettingToggle(false, "Animals"),
			new SettingToggle(false, "Items"),
			new SettingToggle(false, "Crystals"),
			new SettingToggle(false, "Vehicles"),
			new SettingToggle(false, "Force Bob"),
			new SettingSlider(0.1, 5, 1.5, 1, "Thick: "));
	
	public Tracers() {
		super("Tracers", -1, Category.RENDER, "Shows lines to entities you select.", settings);
	}
	
	public void onEnable() { 
		if(!getSettings().get(6).toToggle().state) mc.gameSettings.viewBobbing = false;
	}
	
	public void onDisable() { mc.gameSettings.viewBobbing = true; }
	
	//TODO: fix bobbing, sneaking and pausing while moving
	public void onRender(){
		if(this.isToggled()) {
			final float thick = (float) getSettings().get(7).toSlider().getValue();
			
			for(Entity e: EntityUtils.getLoadedEntities()) {
				Vec3d vec = e.getPositionVec();
				Vec3d vec2 = new Vec3d(0, 0, 0.25).rotatePitch(-(float) Math.toRadians(mc.player.rotationPitch))
						.rotateYaw(-(float) Math.toRadians(mc.player.rotationYaw))
						.add(mc.player.getEyePosition(mc.getRenderPartialTicks()));
				
				if(e instanceof PlayerEntity && e != mc.player && getSettings().get(0).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,1f,0f,0f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,1f,0f,0f,thick);
				}
				if(e instanceof IMob && getSettings().get(1).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,0f,0f,0f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,0f,0f,0f,thick);
				}
				if(EntityUtils.isAnimal(e) && getSettings().get(2).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,0f,1f,0f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,0f,1f,0f,thick);
				}
				if(e instanceof ItemEntity && getSettings().get(3).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,1f,0.7f,0f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,1f,0.7f,0f,thick);
				}
				if(e instanceof EnderCrystalEntity && getSettings().get(4).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,1f, 0f, 1f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,1f, 0f, 1f,thick);
				}
				if((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).toToggle().state) {
					RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z,0.5f, 0.5f, 0.5f,thick);
					RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z,0.5f, 0.5f, 0.5f,thick);
				}
			}
		}
	}
}
