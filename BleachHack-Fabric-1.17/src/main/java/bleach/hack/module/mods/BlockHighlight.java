package bleach.hack.module.mods;

import bleach.hack.event.events.EventWorldRender;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingColor;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.QuadColor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class BlockHighlight extends Module {
    public BlockHighlight() {
        super("BlockHighlight", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights blocks that ur looking at.",
                new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
                new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The width/thickness of the box lines."),
                new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
                new SettingColor("Color", 0.0f, 0.5f, 0.5f, false).withDesc("The color of the highlight."));
    }

    @BleachSubscribe
    public void onWorldRender(EventWorldRender.Post event) {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult)) return;

        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        int mode = getSetting(0).asMode().mode;
        float[] rgb = (this.getSetting(3).asColor().getRGBFloat());

        BlockPos blockpos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        BlockState blockState = mc.world.getBlockState(blockpos);

        if (blockState.getMaterial() != Material.AIR && blockState.getMaterial() != Material.WATER && blockState.getMaterial() != Material.LAVA && mc.world.getWorldBorder().contains(blockpos)) {
            if (mode == 0 || mode == 1) {
                float outlineWidth = getSetting(1).asSlider().getValueFloat();
                Renderer.drawBoxOutline(blockpos, QuadColor.single(rgb[0], rgb[1], rgb[2], 1f), outlineWidth);
            }
            if (mode == 0 || mode == 2) {
                float fillAlpha = getSetting(2).asSlider().getValueFloat();
                Renderer.drawBoxFill(blockpos, QuadColor.single(rgb[0], rgb[1], rgb[2], fillAlpha));
            }
        }
    }
}