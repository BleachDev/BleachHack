//package bleach.hack.module.mods;
//
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.setting.base.SettingBase;
//import bleach.hack.setting.base.SettingMode;
//import bleach.hack.setting.base.SettingSlider;
//import bleach.hack.utils.RenderUtils;
//import com.google.common.eventbus.Subscribe;
//import com.ibm.icu.impl.ICUConfig;
//import com.sun.prism.image.Coords;
//import net.minecraft.block.Block;
//import net.minecraft.block.Blocks;
//import net.minecraft.network.Packet;
//import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.util.registry.Registry;
//import net.minecraft.world.Heightmap;
//import net.minecraft.world.biome.source.BiomeArray;
//import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.WorldChunk;
//TODO FIGURE OUT WHY THIS DOESNT WORK
//import java.util.*;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//public class TunnelESP extends Module
//{
//
//    private final List<TunnelESP.Space> spaces = new ArrayList<>();
//    private final ConcurrentLinkedQueue<Chunk> scanQueue = new ConcurrentLinkedQueue<>();
//    private final List<Chunk> addChunkQueue = new ArrayList<>();
//
//    public TunnelESP()
//    {
//        super("TunnelESP", 0, Category.RENDER, "Shows 1x2/small tunnels (toggle when changing mode)",
//                new SettingMode("Mode: ", "1x2", "All (slow)"),
//                new SettingSlider("All Max: ", 2.0D, 20.0D, 10.0D, 0),
//                new SettingSlider("Red: ", 0.0D, 255.0D, 100.0D, 0),
//                new SettingSlider("Green: ", 0.0D, 255.0D, 255.0D, 0),
//                new SettingSlider("Blue: ", 0.0D, 255.0D, 100.0D, 0),
//                new SettingSlider("Alpha: ", 0.0D, 255.0D, 100.0D, 0));
//
//        Thread scanThread = new Thread(() ->
//        {
//            while (true)
//            {
//                if (!this.scanQueue.isEmpty())
//                {
//                    try
//                    {
//                        this.scan(this.scanQueue.poll());
//                    } catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//
//                    System.out.println("Done Scanning Chunk! (Queue: " + this.scanQueue.size() + ")");
//                }
//            }
//        });
//
//        scanThread.start();
//    }
//
//    public void onDisable()
//    {
//        this.spaces.clear();
//        this.scanQueue.clear();
//    }
//
//    public void onEnable()
//    {
//        for (int x = (int) (this.mc.player.getPos().x - 120.0D); x < (int) this.mc.player.getPos().x + 120; x += 16)
//        {
//            for (int z = (int) (this.mc.player.getPos().z - 120.0D); z < (int) this.mc.player.getPos().z + 120; z += 16)
//            {
//                BlockPos b = new BlockPos(x, 0, z);
//
//                if (this.mc.world.getChunkManager().isChunkLoaded(x, z))
//                {
//                    this.scanQueue.add(this.mc.world.getChunk(b));
//                }
//            }
//        }
//    }
//
//    @Subscribe
//    public boolean onPacketRead(Packet<?> packet)
//    {
//        if (packet instanceof ChunkDataS2CPacket)
//        {
//            ChunkDataS2CPacket chunkPack = (ChunkDataS2CPacket) packet;
//            int i = chunkPack.getX();
//            int j = chunkPack.getZ();
//            BiomeArray biomeArray = null;
//            WorldChunk chunk1 = mc.world.getChunkManager().loadChunkFromPacket(i, j, biomeArray, chunkPack.getReadBuffer(), chunkPack.getHeightmaps(), chunkPack.getVerticalStripBitmask(), chunkPack.isFullChunk());
//            //chunk.read(chunkPack.getReadBuffer(), chunkPack.getExtractedSize(), chunkPack.isFullChunk());
//
//            this.addChunkQueue.add(chunk1);
//        }
//
//        return false;
//    }
//
//    @Subscribe
//    public void onUpdate()
//    {
//        Iterator chunkIter = this.addChunkQueue.iterator();
//
//        while (chunkIter.hasNext())
//        {
//            Chunk c = (Chunk) chunkIter.next();
//            this.scanQueue.add(c);
//        }
//
//        this.addChunkQueue.clear();
//        chunkIter = (new ArrayList<>(this.spaces)).iterator();
//
//        while (chunkIter.hasNext())
//        {
//            TunnelESP.Space s = (TunnelESP.Space) chunkIter.next();
//
//            if (this.mc.player.getPos().add(0, -((int) this.mc.player.getPos().y), 0).distanceTo(new Vec3d(s.pos.getX(), 0, s.pos.getZ())) > 160.0D)
//            {
//                this.spaces.remove(s);
//            }
//        }
//    }
//
//    public void onRender()
//    {
//        float r = (float) (this.getSettings().get(2).asSlider().getValue() / 255.0D);
//        float g = (float) (this.getSettings().get(3).asSlider().getValue() / 255.0D);
//        float b = (float) (this.getSettings().get(4).asSlider().getValue() / 255.0D);
//        float a = (float) (this.getSettings().get(5).asSlider().getValue() / 255.0D);
//
//        for (Object o : new ArrayList<>(this.spaces))
//        {
//            Space s = (Space) o;
//
//            if (s.xpos)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX() + 1, s.pos.getY(), s.pos.getZ(), s.pos.getX() + 1, s.pos.getY() + 1, s.pos.getZ() + 1), r, g, b, a);
//            }
//
//            if (s.xneg)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX(), s.pos.getY(), s.pos.getZ(), s.pos.getX(), s.pos.getY() + 1, s.pos.getZ() + 1), r, g, b, a);
//            }
//
//            if (s.ypos)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX(), s.pos.getY() + 1, s.pos.getZ(), s.pos.getX() + 1, s.pos.getY() + 1, s.pos.getZ() + 1), r, g, b, a);
//            }
//
//            if (s.yneg)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX(), s.pos.getY(), s.pos.getZ(), s.pos.getX() + 1, s.pos.getY(), s.pos.getZ() + 1), r, g, b, a);
//            }
//
//            if (s.zpos)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX(), s.pos.getY(), s.pos.getZ() + 1, s.pos.getX() + 1, s.pos.getY() + 1, s.pos.getZ() + 1), r, g, b, a);
//            }
//
//            if (s.zneg)
//            {
//                RenderUtils.drawFilledBox(new Box(s.pos.getX(), s.pos.getY(), s.pos.getZ(), s.pos.getX() + 1, s.pos.getY() + 1, s.pos.getZ()), r, g, b, a);
//            }
//        }
//    }
//
//    private void scan(Chunk c)
//    {
//        if (this.isToggled())
//        {
//            List<BlockPos> air = new ArrayList<>();
//            BlockPos start = new BlockPos(c.getPos().x * 16, 0, c.getPos().z * 16);
//
//            int max;
//
//            for (int x = 0; x < 16; ++x)
//            {
//                for (max = 0; max < 16; ++max)
//                {
//                    for (int y = 0; y < this.mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, start.add(x, 0, max).getY() - 1, ++y);)
//                    {
//                        if (c.getBlockState(new BlockPos(x, y, max)).getBlock() == Blocks.AIR)
//                        {
//                            air.add(start.add(x, y, max));
//                        }
//                    }
//                }
//            }
//
//            List<List<BlockPos>> rotations = Arrays.asList(Arrays.asList(new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(0, 1, 0), new BlockPos(0, -1, 0)), Arrays.asList(new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 1, 0), new BlockPos(0, -1, 0)));
//
//            if (this.getSettings().get(0).asMode().mode == 0)
//            {
//                Iterator airIter = air.iterator();
//
//                while (true)
//                {
//                    while (true)
//                    {
//                        while (true)
//                        {
//                            BlockPos b;
//                            do
//                            {
//                                do
//                                {
//                                    do
//                                    {
//                                        if (!airIter.hasNext())
//                                        {
//                                            return;
//                                        }
//
//                                        b = (BlockPos) airIter.next();
//                                    } while (!air.contains(b.down()));
//                                } while (air.contains(b.up()));
//                            } while (air.contains(b.down(2)));
//
//                            if (this.isOnEdge(b))
//                            {
//                                if (this.mc.world.getBlockState(b.east()).getBlock() != Blocks.AIR && this.mc.world.getBlockState(b.east().down()).getBlock() != Blocks.AIR && this.mc.world.getBlockState(b.west()).getBlock() != Blocks.AIR && this.mc.world.getBlockState(b.west().down()).getBlock() != Blocks.AIR)
//                                {
//                                    this.spaces.add(new Space(b, true, true, true, false, false, false));
//                                    this.spaces.add(new Space(b.down(), true, true, false, true, false, false));
//                                } else if (!air.contains(b.north()) && !air.contains(b.north().down()) && !air.contains(b.south()) && !air.contains(b.south().down()))
//                                {
//                                    this.spaces.add(new Space(b, false, false, true, false, true, true));
//                                    this.spaces.add(new Space(b.down(), false, false, false, true, true, true));
//                                }
//                            } else if (!air.contains(b.east()) && !air.contains(b.east().down()) && !air.contains(b.west()) && !air.contains(b.west().down()))
//                            {
//                                this.spaces.add(new Space(b, true, true, true, false, false, false));
//                                this.spaces.add(new Space(b.down(), true, true, false, true, false, false));
//                            } else if (!air.contains(b.north()) && !air.contains(b.north().down()) && !air.contains(b.south()) && !air.contains(b.south().down()))
//                            {
//                                this.spaces.add(new Space(b, false, false, true, false, true, true));
//                                this.spaces.add(new Space(b.down(), false, false, false, true, true, true));
//                            }
//                        }
//                    }
//                }
//            } else
//            {
//                max = (int) this.getSettings().get(1).asSlider().getValue();
//                Iterator rotsIter = rotations.iterator();
//
//                fern:
//                while (rotsIter.hasNext())
//                {
//                    List<BlockPos> rot = (List) rotsIter.next();
//                    ArrayList toExplore = new ArrayList<>(air);
//
//                    while (true)
//                    {
//                        ArrayList toSpacesAdd;
//                        boolean shouldExit;
//
//                        do
//                        {
//                            if (toExplore.isEmpty())
//                            {
//                                continue fern;
//                            }
//
//                            int found = 1;
//
//                            List<BlockPos> explored = new ArrayList<>();
//                            List<BlockPos> exploring = new ArrayList<>(Collections.singletonList((BlockPos) toExplore.get(0)));
//
//                            toSpacesAdd = new ArrayList<>(Collections.singletonList(new Space(exploring.get(0), true, true, true, true, true, true)));
//                            shouldExit = false;
//
//                            while (!exploring.isEmpty())
//                            {
//                                for (BlockPos b : new ArrayList<>(exploring))
//                                {
//                                    for (BlockPos r : rot)
//                                    {
//                                        BlockPos next = b.add(r);
//
//                                        if (!explored.contains(next) && !exploring.contains(next) && toExplore.contains(next))
//                                        {
//                                            toSpacesAdd.add(new Space(next, true, true, true, true, true, true));
//
//                                            exploring.add(next);
//                                            ++found;
//
//                                            if (found > max)
//                                            {
//                                                shouldExit = true;
//                                            }
//                                        }
//                                    }
//
//                                    explored.add(b);
//                                    exploring.remove(b);
//                                }
//                            }
//
//                            toExplore.removeAll(explored);
//                        } while (shouldExit);
//
//                        int y = -1;
//
//                        for (Object o : toSpacesAdd)
//                        {
//                            Space s = (Space) o;
//
//                            if (y == -1)
//                            {
//                                y = s.pos.getY();
//                            } else if (s.pos.getY() != y)
//                            {
//                                shouldExit = true;
//
//                                break;
//                            }
//                        }
//
//                        if (shouldExit)
//                        {
//                            this.spaces.addAll(toSpacesAdd);
//                        }
//                    }
//                }
//
//            }
//        }
//    }
//
//    private boolean isOnEdge(BlockPos b)
//    {
//        return b.getX() % 16 == 0 || b.getX() % 16 == 15 || b.getZ() % 16 == 0 || b.getZ() % 16 == 15;
//    }
//
//    static class Space
//    {
//        public BlockPos pos;
//
//        public boolean xpos;
//        public boolean xneg;
//        public boolean ypos;
//        public boolean yneg;
//        public boolean zpos;
//        public boolean zneg;
//
//        public Space(BlockPos pos, boolean xpos, boolean xneg, boolean ypos, boolean yneg, boolean zpos, boolean zneg)
//        {
//            this.pos = pos;
//
//            this.xpos = xpos;
//            this.xneg = xneg;
//            this.ypos = ypos;
//            this.yneg = yneg;
//            this.zpos = zpos;
//            this.zneg = zneg;
//        }
//
//        public boolean equals(Object o)
//        {
//            if (!(o instanceof TunnelESP.Space))
//            {
//                return false;
//            } else
//            {
//                TunnelESP.Space s = (TunnelESP.Space) o;
//
//                return s.pos.equals(this.pos) && s.xpos == this.xpos && s.xneg == this.xneg && s.pos == this.pos && s.ypos == this.ypos && s.zpos == this.zpos && s.zneg == this.zneg;
//            }
//        }
//    }
//}