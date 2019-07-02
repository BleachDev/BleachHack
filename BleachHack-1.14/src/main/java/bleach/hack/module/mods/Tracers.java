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
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;

public class Tracers extends Module{

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Players"),
			new SettingToggle(true, "Mobs"),
			new SettingToggle(false, "Animals"),
			new SettingToggle(true, "Items"),
			new SettingToggle(true, "Crystals"),
			new SettingToggle(false, "Force Bob"),
			new SettingSlider(0.1, 5, 1.5, 1, "Thick: "));
	
	public Tracers() {
		super("Tracers", -1, Category.RENDER, "Shows lines to entities you select.", settings);
	}
	
	public void onEnable() { 
		if(!getSettings().get(5).toToggle().state) mc.gameSettings.viewBobbing = false;
	}
	
	public void onDisable() { mc.gameSettings.viewBobbing = true; }
	
	//TODO: fix bobbing, sneaking and pausing while moving
	public void onRender(){
		if(this.isToggled()) {
			final float thick = (float) getSettings().get(6).toSlider().getValue();
			
			for(Entity e: EntityUtils.getLoadedEntities()) {
				double[] xyz = interpolate(e);
				double[] xyz2 = interpolatePlayer(mc.player);
				
				if(e instanceof PlayerEntity && e != mc.player && getSettings().get(0).toToggle().state) {
					RenderUtils.drawLine(xyz2[0],xyz2[1],xyz2[2],xyz[0],xyz[1],xyz[2],1f,0f,0f,thick);
					RenderUtils.drawLine(xyz[0],xyz[1],xyz[2], xyz[0],xyz[1]+(e.getHeight()/1.1),xyz[2],1f,0f,0f,thick);
				}
				if(e instanceof IMob && getSettings().get(1).toToggle().state) {
					RenderUtils.drawLine(xyz2[0],xyz2[1],xyz2[2],xyz[0],xyz[1],xyz[2],0f,0f,0f,thick);
					RenderUtils.drawLine(xyz[0],xyz[1],xyz[2], xyz[0],xyz[1]+(e.getHeight()/1.1),xyz[2],0f,0f,0f,thick);
				}
				if(EntityUtils.isAnimal(e) && getSettings().get(2).toToggle().state) {
					RenderUtils.drawLine(xyz2[0],xyz2[1],xyz2[2],xyz[0],xyz[1],xyz[2],0f,1f,0f,thick);
					RenderUtils.drawLine(xyz[0],xyz[1],xyz[2], xyz[0],xyz[1]+(e.getHeight()/1.1),xyz[2],0f,1f,0f,thick);
				}
				if(e instanceof ItemEntity && getSettings().get(3).toToggle().state) {
					RenderUtils.drawLine(xyz2[0],xyz2[1],xyz2[2],xyz[0],xyz[1],xyz[2],1f,0.7f,0f,thick);
					RenderUtils.drawLine(xyz[0],xyz[1],xyz[2], xyz[0],xyz[1]+(e.getHeight()/1.1),xyz[2],1f,0.7f,0f,thick);
				}
				if(e instanceof EnderCrystalEntity && getSettings().get(4).toToggle().state) {
					RenderUtils.drawLine(xyz2[0],xyz2[1],xyz2[2],xyz[0],xyz[1],xyz[2],1f, 0f, 1f,thick);
					RenderUtils.drawLine(xyz[0],xyz[1],xyz[2], xyz[0],xyz[1]+(e.getHeight()/1.1),xyz[2],1f, 0f, 1f,thick);
				}
			}
		}
	}
	
	public double[] interpolate(Entity e) {
		double posX = (e.posX + (e.posX - e.prevPosX) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[0];
        double posY = (e.posY + (e.posY - e.prevPosY) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[1];
        double posZ = (e.posZ + (e.posZ - e.prevPosZ) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[2];
        return new double[] { posX, posY, posZ };
	}
	
	public double[] interpolatePlayer(Entity e) {
		double posX = (e.posX + (e.posX - e.prevPosX) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[0] - (e.posX - e.prevPosX);
        double posY = (e.posY + (e.posY - e.prevPosY) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[1] - (e.posY - e.prevPosY) + e.getEyeHeight();
        double posZ = (e.posZ + (e.posZ - e.prevPosZ) * mc.getRenderPartialTicks()) - RenderUtils.getRenderPos()[2] - (e.posZ - e.prevPosZ);
        return new double[] { posX, posY, posZ };
	}
}
