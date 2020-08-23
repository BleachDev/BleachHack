//package bleach.hack.module.mods;
//
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.module.ModuleManager;
//import bleach.hack.utils.RenderUtils;
//import bleach.hack.utils.file.BleachFileMang;
//import net.minecraft.block.Block;
//import net.minecraft.block.Blocks;
//import net.minecraft.network.Packet;
//import net.minecraft.util.math.Box;
//import net.minecraft.util.math.ChunkPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.Vec3d;
//import org.apache.commons.lang3.StringUtils;
//TODO FINISH CONVERTING THIS :I
//import java.time.LocalDateTime;
//import java.util.*;
//
//public class StashFinder extends Module
//{
//    private static final List<SettingBase> settings = Arrays.asList(new SettingMode("Mode: ", "Current", "0, 0"), new SettingSlider(0.0D, 10.0D, 4.0D, 0, "Fly Gap: "), new SettingToggle(false, "Debug"), new SettingToggle(true, "AutoReopen"), new SettingToggle(true, "Shulker Log"), new SettingToggle(true, "Dupe Log"), new SettingToggle(true, "Chest Log"), new SettingToggle(true, "Sign Log"), new SettingToggle(false, "Illegal Log"), new SettingSlider(0.0D, 50.0D, 20.0D, 0, "Min Chest: "), new SettingToggle(true, "Active"));
//    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//
//    public List<ChunkPos> chunks = new ArrayList<>();
//    public List<ChunkPos> nextChunks = new ArrayList<>();
//
//    public int range = 0;
//
//    public ChunkPos nextChunk;
//    public ChunkPos startChunk;
//
//    public List<ChunkPos> chestList = new ArrayList<>();
//    public List<UUID> dupeList = new ArrayList<>();
//    public List<BlockPos> shulkers = new ArrayList<>();
//    public List<BlockPos> signs = new ArrayList<>();
//
//    public boolean elytraing;
//    public int elytratime;
//
//    public int timeout = 0;
//
//    public StashFinder()
//    {
//        super("StashFinder", 0, Category.MISC, "Explores Chunks around you using the elytra and logs stashes (saves in \".minecraft/bleach/ruhama/\", /stashfinder x z to set a start point)", settings);
//    }
//
//    public void onDisable()
//    {
//        this.chunks = new ArrayList<>();
//        this.nextChunks = new ArrayList<>();
//        this.nextChunk = null;
//        this.startChunk = null;
//        this.range = 0;
//        this.shulkers.clear();
//        this.signs.clear();
//    }
//
//    public boolean onPacketRead(Packet<?> packet)
//    {
//        if (this.getSettings().get(8).asToggle().state && packet instanceof SPacketChunkData && this.mc.world.getBiome(this.mc.player.getPosition()) != Biomes.HELL)
//        {
//            SPacketChunkData chunkPack = (SPacketChunkData) packet;
//
//            Chunk chunk = new Chunk(this.mc.world, chunkPack.getChunkX(), chunkPack.getChunkZ());
//            chunk.read(chunkPack.getReadBuffer(), chunkPack.getExtractedSize(), chunkPack.isFullChunk());
//
//            List<Block> blocks = Arrays.asList(Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL);
//
//            StringBuilder content = new StringBuilder();
//
//            for (int x = 0; x < 15; ++x)
//            {
//                for (int y = 0; y < 255; ++y)
//                {
//                    for (int z = 0; z < 15; ++z)
//                    {
//                        Block b = chunk.getBlockState(x, y, z).getBlock();
//
//                        if (blocks.contains(b) && (b != Blocks.BEDROCK || y > 5))
//                        {
//                            this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§3Illegal: " + b.getLocalizedName() + ": " + (chunk.x * 16 + x) + ", " + y + ", " + (chunk.z * 16 + z)));
//                            content.append("Illegal | ").append(b.getLocalizedName()).append(" | ").append(chunk.x * 16 + x).append(", ").append(y).append(", ").append(chunk.z * 16 + z).append(" | ").append(this.dtf.format(LocalDateTime.now())).append("\n");
//                        }
//                    }
//                }
//            }
//
//            if (content.length() > 0)
//            {
//                BleachFileMang.createFile("stashfinder.txt");
//                BleachFileMang.appendFile(StringUtils.chop(content.toString()), "stashfinder.txt");
//            }
//        }
//
//        return false;
//    }
//
//    public void onEnable()
//    {
//        if (this.startChunk != null)
//        {
//            RuhamaLogger.log("StashFinder: Starting from " + this.startChunk.getXStart() + ", " + this.startChunk.getZStart());
//
//            this.range = ((int) Math.max(Math.abs(this.mc.player.getPos().x - (double) this.startChunk.getXStart()), Math.abs(this.mc.player.getPos().z - (double) this.startChunk.getZStart())) >> 4) - 1;
//        } else if (this.getSettings().get(0).toMode().mode == 1)
//        {
//            this.range = ((int) Math.max(Math.abs(this.mc.player.getPos().x), Math.abs(this.mc.player.getPos().z)) >> 4) - 1;
//
//            if (this.range < 0)
//            {
//                this.range = 0;
//            }
//
//            this.startChunk = new ChunkPos(0, 0);
//        } else
//        {
//            this.startChunk = new ChunkPos(this.mc.player.getPosition());
//        }
//
//        if (this.getSettings().get(8).asToggle().state)
//        {
//            this.mc.renderGlobal.loadRenderers();
//        }
//    }
//
//    public void onUpdate()
//    {
//        if (this.getSettings().get(10).asToggle().state)
//        {
//            StringBuilder content;
//            Iterator teIter;
//            TileEntity t;
//
//            if (this.getSettings().get(4).asToggle().state && mc.player.age % 10 == 0)
//            {
//                content = new StringBuilder();
//                teIter = this.mc.world.loadedTileEntityList.iterator();
//
//                ferngayler:
//
//                while (true)
//                {
//                    do
//                    {
//                        do
//                        {
//                            if (!teIter.hasNext())
//                            {
//                                if (content.length() > 0)
//                                {
//                                    BleachFileMang.createFile("stashfinder.txt");
//                                    BleachFileMang.appendFile(StringUtils.chop(content.toString()), "stashfinder.txt");
//                                }
//
//                                break ferngayler;
//                            }
//
//                            t = (TileEntity) teIter.next();
//                        } while (!(t instanceof TileEntityShulkerBox));
//                    } while (this.shulkers.contains(t.getPos()));
//
//                    int count = 0;
//                    Iterator tileEntityIterator = this.mc.world.loadedTileEntityList.iterator();
//
//                    while (true)
//                    {
//                        TileEntity t1;
//                        do
//                        {
//                            do
//                            {
//                                if (!tileEntityIterator.hasNext())
//                                {
//                                    this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§3Shulker: " + t.getPos().getX() + ", " + t.getPos().getY() + ", " + t.getPos().getZ()));
//                                    content.append(Objects.requireNonNull(t.getDisplayName()).getUnformattedText()).append(" | ").append(Block.REGISTRY.getNameForObject(t.getBlockType())).append(" | ").append(t.getPos().getX()).append(", ").append(t.getPos().getY()).append(", ").append(t.getPos().getZ()).append(" | ").append(this.dtf.format(LocalDateTime.now())).append(" (").append(count).append(" nearby)\n");
//
//                                    this.shulkers.add(t.getPos());
//
//                                    continue ferngayler;
//                                }
//
//                                t1 = (TileEntity) tileEntityIterator.next();
//                            } while (t1 == t);
//                        } while (!(t1 instanceof TileEntityChest) && !(t1 instanceof TileEntityShulkerBox));
//
//                        if (t1.getPos().distanceSq(t.getPos()) < 20.0D)
//                        {
//                            ++count;
//                        }
//                    }
//                }
//            }
//
//            if (this.getSettings().get(5).asToggle().state && this.mc.player.ticksExisted % 10 == 0)
//            {
//                content = new StringBuilder();
//                HashMap<ChunkPos, Integer> chunkMap = new HashMap<>();
//                Iterator tileEntityIterator = this.mc.world.loadedTileEntityList.iterator();
//
//                while (tileEntityIterator.hasNext())
//                {
//                    TileEntity te = (TileEntity) tileEntityIterator.next();
//
//                    if (te instanceof TileEntityChest)
//                    {
//                        Integer i = chunkMap.get(new ChunkPos(te.getPos()));
//                        chunkMap.put(new ChunkPos(te.getPos()), i == null ? 0 : i + 1);
//                    }
//                }
//
//                tileEntityIterator = chunkMap.entrySet().iterator();
//
//                while (tileEntityIterator.hasNext())
//                {
//                    Map.Entry<ChunkPos, Integer> e = (Map.Entry) tileEntityIterator.next();
//
//                    if (!this.chestList.contains(e.getKey()) && (double) e.getValue() >= this.getSettings().get(9).asSlider().getValue())
//                    {
//                        this.chestList.add(e.getKey());
//                        String text = e.getValue() + "x Chest | " + e.getKey().getXStart() + ", " + e.getKey().getZStart();
//
//                        this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§3" + text));
//
//                        content.append(text).append(" | ").append(this.dtf.format(LocalDateTime.now())).append("\n");
//                    }
//                }
//
//                if (content.length() > 0)
//                {
//                    BleachFileMang.createFile("stashfinder.txt");
//                    BleachFileMang.appendFile(StringUtils.chop(content.toString()), "stashfinder.txt");
//                }
//            }
//
//            if (this.getSettings().get(6).asToggle().state && this.mc.player.ticksExisted % 10 == 0)
//            {
//                content = new StringBuilder();
//                teIter = this.mc.world.loadedEntityList.iterator();
//
//                while (teIter.hasNext())
//                {
//                    Entity e = (Entity) teIter.next();
//
//                    if (e instanceof AbstractChestHorse)
//                    {
//                        AbstractChestHorse e1 = (AbstractChestHorse) e;
//
//                        if (e1.hasChest() && !this.dupeList.contains(e1.getUniqueID()))
//                        {
//                            this.dupeList.add(e1.getUniqueID());
//                            this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§3Dupe / " + e1.getDisplayName().getUnformattedText() + ": " + e1.getPos().x + ", " + e1.posY + ", " + e1.getPos().z));
//
//                            content = new StringBuilder("Dupe | " + e1.getDisplayName().getUnformattedText() + " | " + e1.getPos().x + ", " + e1.posY + ", " + e1.getPos().z + " | " + this.dtf.format(LocalDateTime.now()) + "\n");
//                        }
//                    }
//                }
//
//                if (content.length() > 0)
//                {
//                    BleachFileMang.createFile("stashfinder.txt");
//                    BleachFileMang.appendFile(StringUtils.chop(content.toString()), "stashfinder.txt");
//                }
//            }
//
//            if (this.getSettings().get(7).asToggle().state && this.mc.player.ticksExisted % 10 == 0)
//            {
//                content = new StringBuilder();
//                teIter = this.mc.world.loadedTileEntityList.iterator();
//
//                while (teIter.hasNext())
//                {
//                    t = (TileEntity) teIter.next();
//
//                    if (t instanceof TileEntitySign)
//                    {
//                        TileEntitySign t1 = (TileEntitySign) t;
//
//                        if (!this.signs.contains(t1.getPos()) && !Arrays.equals(t1.signText, new ITextComponent[] {new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")}))
//                        {
//                            this.signs.add(t1.getPos());
//                            this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString("§3Sign: <\"" + t1.signText[0].getUnformattedText() + " | " + t1.signText[1].getUnformattedText() + " | " + t1.signText[2].getUnformattedText() + " | " + t1.signText[3].getUnformattedText() + "\"> at: " + t1.getPos().getX() + ", " + t1.getPos().getY() + ", " + t1.getPos().getZ()));
//
//                            content = new StringBuilder("Sign <" + t1.signText[0].getUnformattedText() + " | " + t1.signText[1].getUnformattedText() + " | " + t1.signText[2].getUnformattedText() + " | " + t1.signText[3].getUnformattedText() + "> | " + t1.getPos().getX() + ", " + t1.getPos().getY() + ", " + t1.getPos().getZ() + " | " + this.dtf.format(LocalDateTime.now()) + "\n");
//                        }
//                    }
//                }
//
//                if (content.length() > 0)
//                {
//                    BleachFileMang.createFile("stashfinder.txt");
//                    BleachFileMang.appendFile(StringUtils.chop(content.toString()), "stashfinder.txt");
//                }
//            }
//
//            if (!this.mc.player.isFallFlying() && this.elytraing)
//            {
//                this.elytraing = false;
//                this.elytratime = 40;
//            }
//
//            --this.elytratime;
//
//            boolean flat = Objects.requireNonNull(ModuleManager.getModuleByName("ElytraFly")).isToggled() && Objects.requireNonNull(ModuleManager.getModuleByName("ElytraFly")).getSettings().get(0).toMode().mode == 0;
//
//            if (!flat && this.getSettings().get(3).asToggle().state && this.elytratime > 0 && !this.mc.player.onGround && this.mc.currentScreen != null)
//            {
//                this.mc.player.connection.sendPacket(new CPacketEntityAction(this.mc.player, Action.START_FALL_FLYING));
//                this.timeout = 80;
//            }
//
//            if (this.timeout > 0)
//            {
//                --this.timeout;
//            }
//
//            if (flat && !this.mc.gameSettings.keyBindBack.isKeyDown())
//            {
//                this.elytraing = true;
//                this.elytratime = 0;
//
//                if (this.startChunk == null)
//                {
//                    this.startChunk = new ChunkPos(this.mc.player.getPos());
//                }
//
//                int view = 16;
//                int step = 1;
//
//                boolean sorted = false;
//
//                ChunkPos c;
//
//                int x;
//                int z;
//
//                for (x = -view; x <= view; x += 16)
//                {
//                    for (z = -view; z <= view; z += 16)
//                    {
//                        c = new ChunkPos(this.mc.player.getPos().add(x, 0, z));
//
//                        if (!this.chunks.contains(c))
//                        {
//                            this.chunks.add(c);
//
//                            if (this.nextChunks.contains(c))
//                            {
//                                this.nextChunks.remove(c);
//                                if (!sorted)
//                                {
//                                    this.nextChunks.sort(Comparator.comparingDouble(a -> a.getBlock(8, 0, 8).distanceSq(this.mc.player.getPos())));
//                                    sorted = true;
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if (!this.nextChunks.isEmpty())
//                {
//                    this.nextChunk = this.nextChunks.get(0);
//                    this.facePos(this.nextChunk.getXStart() + 8, this.nextChunk.getZStart() + 8);
//
//                    Vec3d forward = (new Vec3d(0.0D, 0.0D, Objects.requireNonNull(ModuleManager.getModuleByName("ElytraFly")).getSettings().get(4).asSlider().getValue())).rotateYaw(-((float) Math.toRadians(this.mc.player.rotationYaw)));
//                    this.mc.player.setVelocity(forward.x, forward.y, forward.z);
//                } else
//                {
//                    this.chunks.clear();
//                    this.range += (int) this.getSettings().get(1).asSlider().getValue();
//
//                    for (x = this.startChunk.x - this.range; x <= this.startChunk.x + this.range; ++x)
//                    {
//                        for (z = this.startChunk.z - this.range; z <= this.startChunk.z + this.range; ++z)
//                        {
//                            if (Math.abs(x - this.startChunk.x) > this.range - step || Math.abs(z - this.startChunk.z) > this.range - step)
//                            {
//                                c = new ChunkPos(x, z);
//                                if (!this.chunks.contains(c))
//                                {
//                                    this.nextChunks.add(c);
//                                }
//                            }
//                        }
//                    }
//
//                    this.nextChunks.sort(Comparator.comparingDouble(a -> a.getBlock(8, 0, 8).distanceSq(this.mc.player.getPosition())));
//                }
//            }
//        }
//    }
//
//    public void onRender()
//    {
//        if (this.getSettings().get(2).asToggle().state)
//        {
//            Iterator chonkIter = this.chunks.iterator();
//
//            ChunkPos c;
//            while (chonkIter.hasNext())
//            {
//                c = (ChunkPos) chonkIter.next();
//                RenderUtils.drawFilledBox(new Box(c.getBlock(0, 0, 0), c.getBlock(16, 0, 16)), 1.0F, 0.0F, 0.0F, 0.3F);
//            }
//
//            chonkIter = this.nextChunks.iterator();
//
//            while (chonkIter.hasNext())
//            {
//                c = (ChunkPos) chonkIter.next();
//                RenderUtils.drawFilledBox(new Box(c.getBlock(0, 0, 0), c.getBlock(16, 0, 16)), 0.0F, 0.0F, 1.0F, 0.3F);
//            }
//
//            if (this.nextChunk != null)
//            {
//                RenderUtils.drawFilledBox(new Box(this.nextChunk.getBlock(0, 0, 0), this.nextChunk.getBlock(16, 0, 16)), 0.0F, 1.0F, 0.0F, 0.3F);
//            }
//        }
//    }
//
//    public void facePos(double x, double z)
//    {
//        double diffX = x - this.mc.player.getPos().x;
//        double diffZ = z - this.mc.player.getPos().z;
//        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
//
//        mc.player.rotationYaw += MathHelper.wrapDegrees(yaw - this.mc.player.rotationYaw);
//    }
//}