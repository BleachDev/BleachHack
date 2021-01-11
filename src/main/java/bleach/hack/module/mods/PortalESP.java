package bleach.hack.module.mods;

import bleach.hack.event.events.*;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class PortalESP extends Module
{
    private final HashMap<DimensionType, ArrayBlockingQueue<BlockPos>> portals = new HashMap<DimensionType, ArrayBlockingQueue<BlockPos>>();
    // Man, race condition is killing me wtf
    public Vec3d prevPos;
    private double[] rPos;
    private Stack<WorldChunk> chunkStack = new Stack<>(); // Lol stacks is so cool
    private boolean running;

    public PortalESP()
    {
        super("PortalESP", KEY_UNBOUND, Category.RENDER, "ESP for portals (laggy with high range)",
                new SettingSlider("Range", 0, 125, 75, 0),
                new SettingSlider("R: ", 0.0D, 255.0D, 115.0D, 0),
                new SettingSlider("G: ", 0.0D, 255.0D, 0.0D, 0),
                new SettingSlider("B: ", 0.0D, 255.0D, 255.0D, 0),
                new SettingSlider("Tick Delay", 1, 20, 10, 0),
                new SettingToggle("Debug", false)
        );
        new Thread(this::chunkyBoi).start();
    }

    private boolean shown(BlockPos pos, DimensionType d) {
        ArrayBlockingQueue<BlockPos> booga = portals.get(d);
        if (booga == null) {
            portals.put(d, new ArrayBlockingQueue<BlockPos>(65455));
            return shown(pos, d);
        }
        for(BlockPos p : portals.get(d)) {
            if (p.equals(pos))
                return true;
        }
        return false;
    }

    private void chunkyBoi() {
        while (true) {
            if (!chunkStack.isEmpty()) {
                WorldChunk chunk = chunkStack.pop();
                if (chunk == null)
                    continue;

                ChunkPos cPos = chunk.getPos();
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 16; j++) {
                        for (int k = 0; k < 255; k++) {
                            BlockPos pos = new BlockPos(cPos.x * 16 + i, k, cPos.z * 16 + j);
                            BlockState state = chunk.getBlockState(pos);
                            if (state.getBlock().is(Blocks.NETHER_PORTAL) && !shown(pos, mc.world.getDimension()))
                                portals.get(mc.world.getDimension()).add(pos);
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onPacket(EventReadPacket e) {
        if (e.getPacket() instanceof ChunkDeltaUpdateS2CPacket) {
            ChunkDeltaUpdateS2CPacket p = (ChunkDeltaUpdateS2CPacket)e.getPacket();
            p.visitUpdates((bp, bs) -> {
                if (mc.player == null)
                    return;
                DimensionType dimension = mc.player.world.getDimension();
                if (portals.containsKey(dimension)) {
                    if (shown(bp, dimension)) {
                        if (!bs.getBlock().is(Blocks.NETHER_PORTAL))
                            this.portals.get(dimension).remove(bp);
                    } else {
                        if (bs.getBlock().is(Blocks.NETHER_PORTAL))
                            this.portals.get(dimension).add(new BlockPos(bp.getX(), bp.getY(), bp.getZ())); // do not event touch it
                    }
                } else {
                    portals.put(dimension, new ArrayBlockingQueue<BlockPos>(65455));
                }
            });
        }
    }

    @Subscribe
    public void chunkLoaded(EventLoadChunk e) {
        WorldChunk chunk = e.getChunk();
        chunkStack.push(chunk);
    }

    @Subscribe
    public void onRender(EventWorldRender event) {
        if (!isToggled()) // Temp fix lmao, idk why is that even called when disabled.
            return;  // Also when enabled chunks need to be reloaded in the way so they can get to chunkyBoi thread

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
            blue = 1.0F - blue;

        if (red > 1.0F)
            red = 1.0F - red;

        ArrayBlockingQueue<BlockPos> portals = this.portals.get(mc.player.world.getDimension());
        if (portals != null) {
            for (BlockPos p : portals)
                if (mc.player.getPos().distanceTo(Vec3d.ofCenter(p)) < 128) // put max range here
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
        double x = (double) blockPos.getX();
        double y = (double) blockPos.getY();
        double z = (double) blockPos.getZ();

        float or = (float) (this.getSettings().get(1).asSlider().getValue() / 255.0D);
        float og = (float) (this.getSettings().get(2).asSlider().getValue() / 255.0D);
        float ob = (float) (this.getSettings().get(3).asSlider().getValue() / 255.0D);
        if (getSetting(5).asToggle().state) {
            BleachLogger.infoMessage(this.mc.world.getBlockState(new BlockPos(x,y,z)).getEntries().toString());
        }
        if (this.mc.world.getBlockState(new BlockPos(x,y,z)).getEntries().toString().contains("values=[x, z]}=x")) {
                RenderUtils.drawFilledBox(new Box(x, y, z + 0.5D, x + 1.0D, y + 1.0D, z + 0.5D), or, og, ob, a);
                RenderUtils.drawFilledBox(new Box(x, y, z + 0.5D, x + 1.0D, y + 1.0D, z + 0.5D), or, og, ob, a * 1.5F);
        } else {
                RenderUtils.drawFilledBox(new Box(x + 0.5D, y, z, x + 0.5D, y + 1.0D, z + 1.0D), or, og, ob, a);
                RenderUtils.drawFilledBox(new Box(x + 0.5D, y, z, x + 0.5D, y + 1.0D, z + 1.0D), or, og, ob, a * 1.5F);
        }
    }
    public void onDisable () {
        portals.clear();
    }
}