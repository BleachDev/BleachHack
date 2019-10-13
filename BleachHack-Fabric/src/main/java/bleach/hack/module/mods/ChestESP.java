package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.Event3DRender;
import bleach.hack.gui.clickgui.SettingBase;
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
		super("ChestESP", -1, Category.RENDER, "Draws a box around storage containers.", settings);
	}

	@Subscribe
	public void onRender(Event3DRender event) {
		for(BlockEntity e: mc.world.blockEntities) {
			if((e instanceof ChestBlockEntity || e instanceof BarrelBlockEntity)
					&& getSettings().get(0).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 1.9F, 1.5F, 0.3F, 0.7F);}
			if(e instanceof EnderChestBlockEntity && getSettings().get(1).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 1F, 0.05F, 1F, 0.7F);}
			if((e instanceof FurnaceBlockEntity || e instanceof SmokerBlockEntity ||
					e instanceof BlastFurnaceBlockEntity) && getSettings().get(2).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.5F, 0.5F, 0.7F);}
			if(e instanceof DispenserBlockEntity && getSettings().get(3).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.55F, 0.55F, 0.7F, 0.7F);}
			if(e instanceof HopperBlockEntity && getSettings().get(4).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.45F, 0.45F, 0.6F, 0.7F);}
			if(e instanceof ShulkerBoxBlockEntity && getSettings().get(5).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.2F, 1F, 0.7F);}
			if(e instanceof BrewingStandBlockEntity && getSettings().get(6).toToggle().state) {
				RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.4F, 0.2F, 0.7F);}
		}
		
		for(Entity e: mc.world.getEntities()) {
			if(e instanceof ChestMinecartEntity && getSettings().get(7).toToggle().state){
				RenderUtils.drawFilledBox(e.getBoundingBox(), 1.9F, 1.5F, 0.3F, 0.7F);}
			if(e instanceof FurnaceMinecartEntity && getSettings().get(8).toToggle().state){
				RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.5F, 0.5F, 0.7F);}
			if(e instanceof HopperMinecartEntity && getSettings().get(9).toToggle().state){
				RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.45F, 0.6F, 0.7F);}
			
			if(e instanceof ItemFrameEntity && getSettings().get(10).toToggle().state){
				if(((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.AIR) {
					RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.1F, 0.1F, 0.7F);
				}else if(((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP){
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
