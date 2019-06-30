package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.item.minecart.HopperMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;

public class ChestESP extends Module{

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Chests"),
			new SettingToggle(true, "EndChests"),
			new SettingToggle(true, "Furnaces"),
			new SettingToggle(true, "Dispensers"),
			new SettingToggle(true, "Hoppers"),
			new SettingToggle(true, "Shulkers"),
			new SettingToggle(true, "BrewStands"),
			new SettingToggle(true, "ChestCarts"),
			new SettingToggle(true, "FurnaceCarts"),
			new SettingToggle(true, "HopperCarts"),
			new SettingToggle(true, "ItemFrames"),
			new SettingToggle(true, "ArmorStands"));
	
	public ChestESP() {
		super("ChestESP", 0, Category.RENDER, "Draws a box around storage containers.", settings);
	}
	
	public void onRender() {
		if(this.isToggled()) {
			for(TileEntity e: mc.world.loadedTileEntityList) {
				if(e instanceof ChestTileEntity && getSettings().get(0).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 1.9F, 1.5F, 0.3F, 0.7F);}
				if(e instanceof EnderChestTileEntity && getSettings().get(1).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 1F, 0.05F, 1F, 0.7F);}
				if(e instanceof FurnaceTileEntity && getSettings().get(2).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.5F, 0.5F, 0.7F);}
				if(e instanceof DispenserTileEntity && getSettings().get(3).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.55F, 0.55F, 0.7F, 0.7F);}
				if(e instanceof HopperTileEntity && getSettings().get(4).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.45F, 0.45F, 0.6F, 0.7F);}
				if(e instanceof ShulkerBoxTileEntity && getSettings().get(5).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.2F, 1F, 0.7F);}
				if(e instanceof BrewingStandTileEntity && getSettings().get(6).toToggle().state) {
					RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.4F, 0.2F, 0.7F);}
			}
			
			for(Entity e: EntityUtils.getLoadedEntities()) {
				if(e instanceof ChestMinecartEntity && getSettings().get(7).toToggle().state){
					RenderUtils.drawFilledBox(e.getBoundingBox(), 1.9F, 1.5F, 0.3F, 0.7F);}
				if(e instanceof FurnaceMinecartEntity && getSettings().get(8).toToggle().state){
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.5F, 0.5F, 0.7F);}
				if(e instanceof HopperMinecartEntity && getSettings().get(9).toToggle().state){
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.45F, 0.6F, 0.7F);}
				
				if(e instanceof ItemFrameEntity && getSettings().get(10).toToggle().state){
					if(((ItemFrameEntity) e).getDisplayedItem().getItem() == Items.AIR) {
						RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.1F, 0.1F, 0.7F);
					}else if(((ItemFrameEntity) e).getDisplayedItem().getItem() == Items.FILLED_MAP){
						RenderUtils.drawFilledBox(e.getBoundingBox(), 0.1F, 0.1F, 0.5F, 0.7F);
					}else {
						RenderUtils.drawFilledBox(e.getBoundingBox(), 0.1F, 0.45F, 0.1F, 0.7F);
					}
				}
				
				if(e instanceof ArmorStandEntity && getSettings().get(11).toToggle().state){
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.4F, 0.1F, 0.7F);}
			}
		}
	}

}
