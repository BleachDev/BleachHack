package bleach.hack.module.mods;

import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module
{
    private final List<BlockPos> poses = new ArrayList<>();
    public Vec3d prevPos;
    private double[] rPos;

    public HoleESP()
    {
        super("HoleESP", KEY_UNBOUND, Category.RENDER, "ESP for safe crystal holes (WIP)",
                new SettingSlider("Range: ", 5.0D, 25.0D, 10.0D, 0),
                new SettingMode("Draw: ", "Full", "Flat"),
                new SettingToggle("Rainbow", true),
                new SettingSlider("Obsidian-R: ", 0.0D, 255.0D, 100.0D, 0),
                new SettingSlider("Obsidian-G: ", 0.0D, 255.0D, 255.0D, 0),
                new SettingSlider("Obsidian-B: ", 0.0D, 255.0D, 100.0D, 0),
                new SettingSlider("Bedrock-R: ", 0.0D, 255.0D, 100.0D, 0),
                new SettingSlider("Bedrock-G: ", 0.0D, 255.0D, 100.0D, 0),
                new SettingSlider("Bedrock-B: ", 0.0D, 255.0D, 255.0D, 0));
        this.prevPos = Vec3d.ZERO;
    }

    public void onUpdate()
    {
        assert this.mc.player != null;
        if (mc.player.age % 20 == 0 || this.mc.player.getPos().distanceTo(this.prevPos) > 5.0D && mc.player.age % 10 == 0)
        {
            this.update((int) this.getSettings().get(0).asSlider().getValue());
        }
    }

    public void update(int range)
    {
        this.poses.clear();
        BlockPos player = mc.player.getBlockPos();
        this.prevPos = mc.player.getPos();

        for (int y = -Math.min(range, player.getY()); y < Math.min(range, 255 - player.getY()); ++y)
        {
            for (int x = -range; x < range; ++x)
            {
                for (int z = -range; z < range; ++z)
                {
                    BlockPos pos = player.add(x, y, z);
                    if ((this.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) && this.mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR && (this.mc.world.getBlockState(pos.up().east()).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(pos.up().east()).getBlock() == Blocks.OBSIDIAN) && (this.mc.world.getBlockState(pos.up().west()).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(pos.up().west()).getBlock() == Blocks.OBSIDIAN) && (this.mc.world.getBlockState(pos.up().north()).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(pos.up().north()).getBlock() == Blocks.OBSIDIAN) && (this.mc.world.getBlockState(pos.up().south()).getBlock() == Blocks.BEDROCK || this.mc.world.getBlockState(pos.up().south()).getBlock() == Blocks.OBSIDIAN) && this.mc.world.getBlockState(pos.up(2)).getBlock() == Blocks.AIR && this.mc.world.getBlockState(pos.up(3)).getBlock() == Blocks.AIR)
                    {
                        this.poses.add(pos.up());
                    }
                }
            }
        }
    }

    @Subscribe
    public void onRender(EventWorldRender event) {
        try
        {
            this.rPos = new double[] {
                    //(Double) Objects.requireNonNull(ReflectUtil(RenderManager.class, "renderPosX", "renderPosX")).get(mc.getBlockRenderManager()),
                    //(Double) Objects.requireNonNull(ReflectUtil(RenderManager.class, "renderPosY", "renderPosY")).get(this.mc.getBlockRenderManager()),
                    //(Double) Objects.requireNonNull(ReflectUtil(RenderManager.class, "renderPosZ", "renderPosZ")).get(this.mc.getBlockRenderManager())
            };
        } catch (Exception e)
        {
            this.rPos = new double[] {0.0D, 0.0D, 0.0D};
        }

        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);

        float blue = (float) (System.currentTimeMillis() / 10L % 512L) / 255.0F;
        float red = (float) (System.currentTimeMillis() / 16L % 512L) / 255.0F;

        if (blue > 1.0F)
        {
            blue = 1.0F - blue;
        }

        if (red > 1.0F)
        {
            red = 1.0F - red;
        }

        for (BlockPos p : this.poses)
        {
            this.drawFilledBlockBox(p, red, 0.7F, blue, 0.25F);
        }

        GL11.glEnable(2929);
        GL11.glEnable(3553);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public void drawFilledBlockBox(BlockPos blockPos, float r, float g, float b, float a)
    {
        try
        {
            double x = (double) blockPos.getX() - this.rPos[0];
            double y = (double) blockPos.getY() - this.rPos[1];
            double z = (double) blockPos.getZ() - this.rPos[2];

            float or = (float) (this.getSettings().get(3).asSlider().getValue() / 255.0D);
            float og = (float) (this.getSettings().get(4).asSlider().getValue() / 255.0D);
            float ob = (float) (this.getSettings().get(5).asSlider().getValue() / 255.0D);
            float br = (float) (this.getSettings().get(6).asSlider().getValue() / 255.0D);
            float bg = (float) (this.getSettings().get(7).asSlider().getValue() / 255.0D);
            float bb = (float) (this.getSettings().get(8).asSlider().getValue() / 255.0D);

            if (this.getSettings().get(2).asToggle().state)
            {
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), r, g, b, a);
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), r, g, b, a * 1.5F);
            } else if (this.mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.OBSIDIAN)
            {
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), or, og, ob, a);
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), or, og, ob, a * 1.5F);
            } else
            {
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), br, bg, bb, a);
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), br, bg, bb, a * 1.5F);
            }

            if (this.getSettings().get(1).asMode().mode == 1)
            {
                return;
            }

            if (this.getSettings().get(2).asToggle().state)
            {
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), r, g, b, a);
                RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), r, g, b, a * 1.5F);
                RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), r, g, b, a);
                RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), r, g, b, a * 1.5F);
                RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), r, g, b, a);
                RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), r, g, b, a * 1.5F);
                RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), r, g, b, a);
                RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), r, g, b, a * 1.5F);
            } else
            {
                if (this.mc.world.getBlockState(blockPos.north()).getBlock() == Blocks.OBSIDIAN)
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), or, og, ob, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), or, og, ob, a * 1.5F);
                } else
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), br, bg, bb, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), br, bg, bb, a * 1.5F);
                }

                if (this.mc.world.getBlockState(blockPos.west()).getBlock() == Blocks.OBSIDIAN)
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), or, og, ob, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
                } else
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), br, bg, bb, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), br, bg, bb, a * 1.5F);
                }

                if (this.mc.world.getBlockState(blockPos.east()).getBlock() == Blocks.OBSIDIAN)
                {
                    RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a);
                    RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
                } else
                {
                    RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), br, bg, bb, a);
                    RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), br, bg, bb, a * 1.5F);
                }

                if (this.mc.world.getBlockState(blockPos.south()).getBlock() == Blocks.OBSIDIAN)
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
                } else
                {
                    RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), br, bg, bb, a);
                    RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), br, bg, bb, a * 1.5F);
                }
            }
        } catch (Exception ignored)
        {
        }
    }
}