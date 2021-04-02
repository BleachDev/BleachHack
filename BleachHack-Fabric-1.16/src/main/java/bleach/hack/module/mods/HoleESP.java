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
import bleach.hack.util.RenderUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class HoleESP extends Module {

	private Set<BlockPos> bedrockHoles = new HashSet<>();
	private Set<BlockPos> mixedHoles = new HashSet<>();
	private Set<BlockPos> obsidianHoles = new HashSet<>();

	public HoleESP() {
		super("HoleESP", KEY_UNBOUND, Category.RENDER, "Highlights save and not so save holes. Used for CrystalPvP",
				new SettingSlider("Radius", 1, 20, 10, 0).withDesc("Radius in which holes are getting searched"),
				new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),
				new SettingToggle("Bedrock", true).withDesc("Shows holes with full bedrock").withChildren(
						new SettingColor("Color", 0f, 1f, 0f, false).withDesc("Color for bedrock holes")),
				new SettingToggle("Mixed", true).withDesc("Shows holes with a mix of obsidian and bedrock").withChildren(
						new SettingColor("Mixed", 1f, 1f, 0f, false).withDesc("Color formixed holes")),
				new SettingToggle("Obsidian", true).withDesc("Shows holes with a mix of obsidian and bedrock").withChildren(
						new SettingColor("Obsidian", 1f, 0f, 0f, false).withDesc("Color for obsidian holes")),
				new SettingMode("Location", "Under", "At", "Flat").withDesc("Where and how the box is rendered"));
	}

	@Override
	public void onDisable() {
		bedrockHoles.clear();
		mixedHoles.clear();
		obsidianHoles.clear();

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.age % 16 == 0) {
			bedrockHoles.clear();
			mixedHoles.clear();
			obsidianHoles.clear();
		}

		int dist = (int) getSetting(0).asSlider().getValue();

		for (BlockPos pos : BlockPos.iterateOutwards(mc.player.getBlockPos(), dist, dist, dist)) {
			if (!World.isInBuildLimit(pos.down())
					|| (mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK
					&& mc.world.getBlockState(pos.down()).getBlock() != Blocks.OBSIDIAN)
					|| !mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()
					|| !mc.world.getBlockState(pos.up(1)).getCollisionShape(mc.world, pos.up(1)).isEmpty()
					|| !mc.world.getBlockState(pos.up(2)).getCollisionShape(mc.world, pos.up(2)).isEmpty()) {
				continue;
			}

			int bedrockCounter = 0;
			int obsidianCounter = 0;
			for (BlockPos pos1 : neighbours(pos)) {
				if (mc.world.getBlockState(pos1).getBlock() == Blocks.BEDROCK) {
					bedrockCounter++;
				} else if (mc.world.getBlockState(pos1).getBlock() == Blocks.OBSIDIAN) {
					obsidianCounter++;
				} else {
					break;
				}
			}
			
			if (bedrockCounter == 5 && getSetting(4).asToggle().state) {
				bedrockHoles.add(pos.toImmutable());
			} else if (obsidianCounter == 5 && getSetting(6).asToggle().state) {
				obsidianHoles.add(pos.toImmutable());
			} else if (bedrockCounter >= 1 && obsidianCounter >= 1 
					&& bedrockCounter + obsidianCounter == 5 && getSetting(5).asToggle().state) {
				mixedHoles.add(pos.toImmutable());
			}
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		int mode = getSetting(1).asMode().mode;

		if (mode == 0 || mode == 2) {
			float fillAlpha = (float) getSetting(3).asSlider().getValue();
			
			for (BlockPos pos : bedrockHoles) {
				float[] rgb = getSetting(4).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawFill(boxAt(pos), rgb[0], rgb[1], rgb[2], fillAlpha);
			}

			for (BlockPos pos : mixedHoles) {
				float[] rgb = getSetting(5).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawFill(boxAt(pos), rgb[0], rgb[1], rgb[2], fillAlpha);
			}

			for (BlockPos pos : obsidianHoles) {
				float[] rgb = getSetting(6).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawFill(boxAt(pos), rgb[0], rgb[1], rgb[2], fillAlpha);
			}
		}

		if (mode == 0 || mode == 1) {
			float outlineWidth = (float) getSetting(2).asSlider().getValue();
			
			for (BlockPos pos : bedrockHoles) {
				float[] rgb = getSetting(4).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawOutline(boxAt(pos), rgb[0], rgb[1], rgb[2], 1f, outlineWidth);
			}

			for (BlockPos pos : mixedHoles) {
				float[] rgb = getSetting(5).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawOutline(boxAt(pos), rgb[0], rgb[1], rgb[2], 1f, outlineWidth);
			}

			for (BlockPos pos : obsidianHoles) {
				float[] rgb = getSetting(6).asToggle().getChild(0).asColor().getRGBFloat();
				RenderUtils.drawOutline(boxAt(pos), rgb[0], rgb[1], rgb[2], 1f, outlineWidth);
			}
		}
	}
	
	private BlockPos[] neighbours(BlockPos pos) {
		return new BlockPos[] {
				pos.west(), pos.east(), pos.south(), pos.north(), pos.down()
		};
	}
	
	private Box boxAt (BlockPos pos) {
		int renderLocation = getSetting(7).asMode().mode;
		Box box = new Box(renderLocation == 0 ? pos.down() : pos);
		
		if (renderLocation == 2)
			return box.expand(0, -0.5, 0).offset(0, -0.5, 0);
		
		return box;
	}
}
