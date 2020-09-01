package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StashFinder extends Module {
    public StashFinder() {
        super("StashFinder", KEY_UNBOUND, Category.WORLD, "Find Stashes!",
                new SettingToggle("Render", true),
                new SettingToggle("Toggle Stash Logger", true),
                new SettingSlider("Range: ", 1.0D, 10.0D, 4.0D, 0));
    }

    public int range = 0;
    public int timeout = 0;

    public ChunkPos nextChunk;
    public ChunkPos startChunk;

    public List<ChunkPos> chunks = new ArrayList<>();
    public List<ChunkPos> nextChunks = new ArrayList<>();

    public void onEnable() {
        super.onEnable();

        if (this.startChunk != null) {
            this.range = ((int) Math.max(Math.abs(this.mc.player.getX() - (double) this.startChunk.getStartX()), Math.abs(this.mc.player.getZ() - (double) this.startChunk.getStartZ())) >> 4) - 1;
        } else {
            this.startChunk = new ChunkPos(this.mc.player.getBlockPos());
        }
        /*if (ToggleLog.getValue()) {
            final Module mod = ModuleManager.Get().GetMod(StashLoggerModule.class);

            if (!mod.isEnabled())
                mod.toggle();
        }*/ //implement later this is salhacks way of enabling modules
    }

    @Subscribe
    public void onTick(EventTick event){
        if (mc.player.isFallFlying() || mc.player.isRiding()) {
            mc.options.keyForward.setPressed(true);
            if (this.startChunk == null) {
                this.startChunk = new ChunkPos(this.mc.player.getBlockPos());
            }
            int view = 16;
            int step = 1;

            boolean sorted = false;

            ChunkPos c;

            int x;
            int z;

            for (x = -view; x <= view; x += 16) {
                for (z = -view; z <= view; z += 16) {
                    c = new ChunkPos(this.mc.player.getBlockPos().add(x, 0, z));

                    if (!this.chunks.contains(c)) {
                        this.chunks.add(c);

                        if (this.nextChunks.contains(c)) {
                            this.nextChunks.remove(c);
                            if (!sorted) {
                                this.nextChunks.sort(Comparator.comparingDouble(a -> a.getCenterBlockPos().getSquaredDistance(this.mc.player.getBlockPos())));
                                sorted = true;
                            }
                        }
                    }
                }
            }

            if (!this.nextChunks.isEmpty()) {
                this.nextChunk = this.nextChunks.get(0);
                final double rotations[] = EntityUtils.calculateLookAt(
                        nextChunk.getStartX() + 8,
                        64,
                        nextChunk.getStartZ() + 8,
                        mc.player);

                mc.player.yaw = (float) rotations[0];


            } else {
                this.chunks.clear();
                this.range += (int) this.getSettings().get(2).asSlider().getValue();

                for (x = this.startChunk.x - this.range; x <= this.startChunk.x + this.range; ++x) {
                    for (z = this.startChunk.z - this.range; z <= this.startChunk.z + this.range; ++z) {
                        if (Math.abs(x - this.startChunk.x) > this.range - step || Math.abs(z - this.startChunk.z) > this.range - step) {
                            c = new ChunkPos(x, z);
                            if (!this.chunks.contains(c)) {
                                this.nextChunks.add(c);
                            }
                        }
                    }
                }

                this.nextChunks.sort(Comparator.comparingDouble(a -> a.getCenterBlockPos().getSquaredDistance(this.mc.player.getBlockPos())));
            }
        }
    }

    public void onDisable() {
        mc.options.keyForward.setPressed(false);
        super.onDisable();
    }
    /*@Subscribe
    public void onRenderWorld(EventWorldRender event) {
        if (this.getSettings().get(1).asToggle().state)
        {
            Iterator chonkIter = this.chunks.iterator();

            ChunkPos c;
            while (chonkIter.hasNext())
            {
                c = (ChunkPos) chonkIter.next();
                RenderUtils.drawFilledBox(new AxisAlignedBB(c.getCenterBlockPos(), c.getCenterBlockPos(), 1.0F, 0.0F, 0.0F, 0.3F);
            }

            chonkIter = this.nextChunks.iterator();

            while (chonkIter.hasNext())
            {
                c = (ChunkPos) chonkIter.next();
                RenderUtils.drawFilledBox(new AxisAlignedBB(c.getCenterBlockPos(), c.getCenterBlockPos(), 0.0F, 0.0F, 1.0F, 0.3F);
            }

            if (this.nextChunk != null)
            {
                RenderUtils.drawFilledBox(new AxisAlignedBB(this.nextChunk.getCenterBlockPos(), this.nextChunk.getCenterBlockPos(), 0.0F, 1.0F, 0.0F, 0.3F);
            }
        }
    }*/

    // This ensures that if the game crashes, the StashFinder will not be saved as enabled. this will cause the game to crash every time it turns on
    @Subscribe
    private void EventDisconnect(EventReadPacket event) {
        if (event.getPacket() instanceof CloseScreenS2CPacket || event.getPacket() instanceof DisconnectS2CPacket)
            setToggled(false);
    }
}
