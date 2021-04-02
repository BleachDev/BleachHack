package bleach.hack.module.mods;

import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class Search extends Module{

	private Set<BlockPos> foundBlocks = new HashSet<>();
	
	public Search() {
		super("Search", KEY_UNBOUND, Category.RENDER, "Highlights specified Blocks",
				new SettingSlider("Radius", 1, 40, 20, 0).withDesc("Radius in which blocks are getting searched"),
				SettingLists.newBlockList("Edit Blocks", "Edit Search Blocks",
						Blocks.IRON_ORE,
						Blocks.GOLD_ORE,
						Blocks.LAPIS_ORE,
						Blocks.REDSTONE_ORE,
						Blocks.DIAMOND_ORE,
						Blocks.EMERALD_ORE,
						Blocks.GOLD_BLOCK,
						Blocks.LAPIS_BLOCK,
						Blocks.REDSTONE_BLOCK,
						Blocks.DIAMOND_BLOCK,
						Blocks.EMERALD_BLOCK,
						Blocks.NETHER_GOLD_ORE,
						Blocks.ANCIENT_DEBRIS).withDesc("Edit the Search blocks"),
				new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),
				new SettingToggle("Tracers", true).withDesc("Renders a line from the player to all found blocks").withChildren(
						new SettingSlider("Width", 0.1, 5, 1.5, 1).withDesc("Thickness of the tracers"),
						new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the tracers")),
				new SettingColor("Color", 240, 211, 165, false)); // I like this color
	}
	
	@Override
	public void onDisable() {
		foundBlocks.clear();
		super.onDisable();
	}
	
	@Subscribe
	public void onTick (EventTick event) {
		if (mc.player.age % 16 == 0) {
			foundBlocks.clear();
		}
		
		int dist = (int) getSetting(0).asSlider().getValue();
		for (BlockPos pos : BlockPos.iterateOutwards(mc.player.getBlockPos(), dist, dist, dist))
			if (isAnyListBlockThere(pos))
				foundBlocks.add(pos.toImmutable());
	}
	
	private boolean isAnyListBlockThere (BlockPos pos) {
		return getSetting(1).asList(Block.class).contains(mc.world.getBlockState(pos).getBlock());
	}
	
	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		int mode = getSetting(2).asMode().mode;
		float[] rgb = getSetting(6).asColor().getRGBFloat();

		if (mode == 0 || mode == 1) {
			float outlineWidth = (float) getSetting(3).asSlider().getValue();
			for (BlockPos pos : foundBlocks) {
				RenderUtils.drawOutline(pos, rgb[0], rgb[1], rgb[2], 1f, outlineWidth);
			}
		}
		
		if (mode == 0 || mode == 2) {
			float fillAlpha = (float) getSetting(4).asSlider().getValue();
			for (BlockPos pos : foundBlocks) {
				RenderUtils.drawFill(pos, rgb[0], rgb[1], rgb[2], fillAlpha);
			}
		}
		
		SettingToggle tracers = getSetting(5).asToggle();
		if (tracers.state) {
			// This is bad when bobbing is enabled!
			Vec3d vec2 = new Vec3d(0, 0, 75).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
					.rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
					.add(mc.cameraEntity.getPos().add(0, mc.cameraEntity.getEyeHeight(mc.cameraEntity.getPose()), 0));
			
			for (BlockPos pos : foundBlocks)
				RenderUtils.drawLine(vec2.x, vec2.y, vec2.z, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
						rgb[0], rgb[1], rgb[2], (float) tracers.getChild(1).asSlider().getValue(), (float) tracers.getChild(0).asSlider().getValue());
		}
	}
}
