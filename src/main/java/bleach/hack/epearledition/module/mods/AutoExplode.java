//package bleach.hack.module.mods;
//
//import bleach.hack.event.events.EventTick;
//import bleach.hack.event.events.EventWorldRender;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.setting.base.SettingSlider;
//import bleach.hack.utils.RenderUtils;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.item.Items;
//import net.minecraft.util.Hand;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.Vec3d;
//import org.lwjgl.opengl.GL11;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AutoExplode extends Module
//{
//    private final List<BlockPos> poses = new ArrayList<>();
//    public Vec3d prevPos;
//    private double[] rPos;
//    private int lastSlot = -1;
//
//    public AutoExplode()
//    {
//        super("AutoExplode", KEY_UNBOUND, Category.PLAYER, "Automatically explodes respawn anchors",
//                new SettingSlider("Range", 0.1, 5, 4.5, 1),
//                new SettingSlider("R: ", 0.0D, 255.0D, 148.0D, 0),
//                new SettingSlider("G: ", 0.0D, 255.0D, 0.0D, 0),
//                new SettingSlider("B: ", 0.0D, 255.0D, 201.0D, 0),
//                new SettingSlider("Tick Delay: ", 0.0D, 40.0D, 10.0D, 0).withDesc("Ticks per block place to avoid kick for packet spam"));
//    }
//    @Subscribe
//    public void onTick(EventTick event)
//    {
//        if (this.isToggled() && !mc.world.getRegistryKey().getValue().getPath().equalsIgnoreCase("the_nether"))
//        {
//            this.update((int) this.getSettings().get(0).asSlider().getValue());
//        }
//    }
//
//    public void update(int range)
//    {
//        this.poses.clear();
//        BlockPos player = mc.player.getBlockPos();
//        this.prevPos = mc.player.getPos();
//
//        for (int y = -Math.min(range, player.getY()); y < Math.min(range, 255 - player.getY()); ++y)
//        {
//            for (int x = -range; x < range; ++x)
//            {
//                for (int z = -range; z < range; ++z)
//                {
//                    BlockPos pos = player.add(x, y, z);
//                    assert this.mc.world != null;
//                    if (this.mc.world.getBlockState(pos).getBlock().getDefaultState().toString().equals("Block{minecraft:respawn_anchor}[charges=0]"))
//                    {
//                        this.poses.add(pos);
//                    }
//                }
//            }
//        }
//        for (BlockPos p : this.poses)
//        {
//            //this.drawFilledBlockBox(p, red, 0.7F, blue, 0.25F);
//            lastSlot = mc.player.inventory.selectedSlot;
//            for (int i = 0; i < 9; i++) {
//                if (mc.player.inventory.getStack(i).getItem() == Items.GLOWSTONE) {
//                    mc.player.inventory.selectedSlot = i;
//                }
//            }
//            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(p), Direction.UP, p, false));
//            if (lastSlot != -1) {
//                mc.player.inventory.selectedSlot = lastSlot;
//                lastSlot = -1;
//            }
//            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(p), Direction.UP, p, false));
//        }
//    }
//    @Subscribe
//    public void onRender(EventWorldRender event) {
//
//        GL11.glPushMatrix();
//        GL11.glBlendFunc(770, 771);
//        GL11.glEnable(3042);
//        GL11.glDisable(3553);
//        GL11.glDisable(2929);
//        GL11.glDepthMask(false);
//        GL11.glLineWidth(2.0F);
//
//        float blue = (float) (System.currentTimeMillis() / 10L % 512L) / 255.0F;
//        float red = (float) (System.currentTimeMillis() / 16L % 512L) / 255.0F;
//
//        if (blue > 1.0F)
//        {
//            blue = 1.0F - blue;
//        }
//
//        if (red > 1.0F)
//        {
//            red = 1.0F - red;
//        }
//
//        GL11.glEnable(2929);
//        GL11.glEnable(3553);
//        GL11.glDepthMask(true);
//        GL11.glDisable(3042);
//        GL11.glPopMatrix();
//    }
//
//    public void drawFilledBlockBox(BlockPos blockPos, float r, float g, float b, float a)
//    {
//        double x = blockPos.getX();
//        double y = blockPos.getY();
//        double z = blockPos.getZ();
//
//        float or = (float) (this.getSettings().get(1).asSlider().getValue() / 255.0D);
//        float og = (float) (this.getSettings().get(2).asSlider().getValue() / 255.0D);
//        float ob = (float) (this.getSettings().get(3).asSlider().getValue() / 255.0D);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y + 1.0D, z), or, og, ob, a * 1.5F);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
//        RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x + 1.0D, y, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
//        RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x, y, z + 1.0D, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x, y, z, x + 1.0D, y, z + 1.0D), or, og, ob, a * 1.5F);
//        RenderUtils.drawFilledBox(new Box(x, y + 1.0D, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a);
//        RenderUtils.drawFilledBox(new Box(x, y + 1.0D, z, x + 1.0D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
//    }
//    public void onDisable () {
//        this.poses.clear();
//    }
//}