package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;

public class ChestESP extends Module{

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Chests"),
			new SettingToggle(true, "EndChests"),
			new SettingToggle(true, "Furnaces"),
			new SettingToggle(true, "Dispensers"),
			new SettingToggle(true, "Shulkers"),
			new SettingToggle(true, "BrewStands"));
	
	public ChestESP() {
		super("ChestESP", 0, Category.RENDER, "Draws a box around storage containers.", settings);
	}
	
	public void onRender() {
		if(this.isToggled()) {
			for(TileEntity e: mc.world.loadedTileEntityList) {
				if(e instanceof ChestTileEntity && getSettings().get(0).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 1.9F, 1.5F, 0.3F, 0.7F);}
				if(e instanceof EnderChestTileEntity && getSettings().get(1).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 1F, 0.05F, 1F, 0.7F);}
				if(e instanceof FurnaceTileEntity && getSettings().get(2).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.5F, 0.5F, 0.7F);}
				if(e instanceof DispenserTileEntity && getSettings().get(3).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.55F, 0.55F, 0.7F, 0.7F);}
				if(e instanceof ShulkerBoxTileEntity && getSettings().get(4).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.2F, 1F, 0.7F);}
				if(e instanceof BrewingStandTileEntity && getSettings().get(5).toSettingToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.4F, 0.2F, 0.7F);}
			}
		}
	}

}
