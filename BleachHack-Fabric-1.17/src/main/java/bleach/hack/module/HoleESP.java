package bleach.hack.module.mods;

import java.util.ArrayList;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.util.RenderUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class HoleESP extends Module {
	/**
	 * Author: Lasnik (https://github.com/lasnikprogram)
	 */
	private ArrayList<BlockPos> bedrockHoles = new ArrayList<BlockPos>();
	private ArrayList<BlockPos> mixedHoles = new ArrayList<BlockPos>();
	private ArrayList<BlockPos> obsidianHoles = new ArrayList<BlockPos>();

	public HoleESP() {
		super("HoleESP", KEY_UNBOUND, Category.RENDER, "Highlights save and not so save holes. Used for CrystalPvP",
				new SettingSlider("Radius", 1, 20, 10, 0).withDesc("Radius in which holes are getting searched"),
				new SettingColor("Bedrock", 0f, 1f, 0f, false).withDesc("All neighbours are bedrock"),
				new SettingColor("Mixed", 1f, 1f, 0f, false).withDesc("The neighbours are a mix out of obsidian and bedrock"),
				new SettingColor("Obsidian", 1f, 0f, 0f, false).withDesc("All neighbours are obsidian"),
				new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
				//TODO: Add shader and outline render setting. If this is this even possible?!
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"));
				new SettingMode("Location", "Under", "At").withDesc("The locations of the rendered blocks");
	}
	
	public void onDisable() {
		bedrockHoles.clear();
		mixedHoles.clear();
		obsidianHoles.clear();
	}

	@Subscribe
	public void onTick(EventTick event) {
		// Is this efficient? I don´t think so. So fix it if you know how
		bedrockHoles.clear();
		mixedHoles.clear();
		obsidianHoles.clear();
		
			
		int dist = (int) getSetting(0).asSlider().getValue();
		int renderMode = getSetting(7).asMode().mode;
		BlockPos player = mc.player.getBlockPos();
		Iterable<BlockPos> blocks = BlockPos.iterateOutwards(player, dist, dist, dist);

		allBlocksInDistance: 
		for (BlockPos pos : blocks) {
			for (int i = 0; i <= 2; i++)
				if (mc.world.getBlockState(pos.add(0, i, 0)).getBlock() != Blocks.AIR)
					continue allBlocksInDistance; // That´s only for expert programmers

			int bedrockCounter = 0;
			int obsidianCounter = 0;
			for (BlockPos pos1 : neighbours(pos)) {
				if (mc.world.getBlockState(pos1).getBlock() == Blocks.BEDROCK)
					bedrockCounter++;
				if (mc.world.getBlockState(pos1).getBlock() == Blocks.OBSIDIAN)
					obsidianCounter++;

				if (bedrockCounter == 5)
					bedrockHoles.add(renderMode == 0 ? pos1 : pos1.up());
				else if (obsidianCounter == 5)
					obsidianHoles.add(renderMode == 0 ? pos1 : pos1.up());
				else if (obsidianCounter > 0 && bedrockCounter > 0 
						&& bedrockCounter + obsidianCounter == 5)
					mixedHoles.add(renderMode == 0 ? pos1 : pos1.up());
			}
		}
	}

	private BlockPos[] neighbours(BlockPos pos) {
		return new BlockPos[] { 
				pos.add(1, 0, 0), 
				pos.add(-1, 0, 0), 
				pos.add(0, 0, 1), 
				pos.add(0, 0, -1),
				pos.add(0, -1, 0) };
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		int mode = getSetting(4).asMode().mode;
		if (mode == 0 || mode == 2) {
			for (BlockPos pos : bedrockHoles) {
				float[] rgb = getSetting(1).asColor().getRGBFloat();
				RenderUtils.drawFill(pos, rgb[0], rgb[1], rgb[2], 
						(float) getSetting(6).asSlider().getValue());
			}

			for (BlockPos pos : mixedHoles) {
				float[] rgb = getSetting(2).asColor().getRGBFloat();
				RenderUtils.drawFill(pos, rgb[0], rgb[1], rgb[2], 
						(float) getSetting(6).asSlider().getValue());
			}
			
			for (BlockPos pos : obsidianHoles) {
				float[] rgb = getSetting(3).asColor().getRGBFloat();
				RenderUtils.drawFill(pos, rgb[0], rgb[1], rgb[2], 
						(float) getSetting(6).asSlider().getValue());
			}
		}

		if (mode == 0 || mode == 1) {
			for (BlockPos pos : bedrockHoles) {
				float[] rgb = getSetting(1).asColor().getRGBFloat();
				RenderUtils.drawOutline(pos, rgb[0], rgb[1], rgb[2], 1f, 
						(float) getSetting(5).asSlider().getValue());
			}

			for (BlockPos pos : mixedHoles) {
				float[] rgb = getSetting(2).asColor().getRGBFloat();
				RenderUtils.drawOutline(pos, rgb[0], rgb[1], rgb[2], 1f, 
						(float) getSetting(5).asSlider().getValue());
			}
			
			for (BlockPos pos : obsidianHoles) {
				float[] rgb = getSetting(3).asColor().getRGBFloat();
				RenderUtils.drawOutline(pos, rgb[0], rgb[1], rgb[2], 1f, 
						(float) getSetting(5).asSlider().getValue());
			}
		}
	}
}

