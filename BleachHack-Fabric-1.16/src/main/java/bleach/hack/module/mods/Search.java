package bleach.hack.module.mods;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.chunk.Chunk;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class Search extends Module {

	private Set<BlockPos> foundBlocks = new HashSet<>();

	private ExecutorService chunkSearchers = Executors.newFixedThreadPool(4);
	private Map<ChunkPos, Future<Set<BlockPos>>> chunkFutures = new HashMap<>();

	private Queue<ChunkPos> queuedChunks = new ArrayDeque<>();
	private Queue<ChunkPos> queuedUnloads = new ArrayDeque<>();
	private Queue<Pair<BlockPos, BlockState>> queuedBlocks = new ArrayDeque<>();

	private Set<Block> prevBlockList = new HashSet<>();
	
	private int oldViewDistance = -1;

	public Search() {
		super("Search", KEY_UNBOUND, Category.RENDER, "Highlights specified Blocks",
				new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),
				new SettingToggle("Tracers", false).withDesc("Renders a line from the player to all found blocks").withChildren(
						new SettingSlider("Width", 0.1, 5, 1.5, 1).withDesc("Thickness of the tracers"),
						new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the tracers")),
				SettingLists.newBlockList("Edit Blocks", "Edit Search Blocks",
						Blocks.DIAMOND_ORE,
						Blocks.EMERALD_ORE,
						Blocks.DIAMOND_BLOCK,
						Blocks.EMERALD_BLOCK,
						Blocks.ANCIENT_DEBRIS).withDesc("Edit the Search blocks"));
	}

	@Override
	public void onDisable() {
		reset();

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		Set<Block> blockList = getSetting(4).asList(Block.class).getItems();

		if (!prevBlockList.equals(blockList) || oldViewDistance != mc.options.viewDistance) {
			reset();

			for (Chunk chunk: WorldUtils.getLoadedChunks()) {
				submitChunk(chunk.getPos(), chunk);
			}

			prevBlockList = new HashSet<>(blockList);
			oldViewDistance = mc.options.viewDistance;
			return;
		}

		while (!queuedBlocks.isEmpty()) {
			Pair<BlockPos, BlockState> blockPair = queuedBlocks.poll();

			if (getSetting(4).asList(Block.class).contains(blockPair.getRight().getBlock())) {
				foundBlocks.add(blockPair.getLeft());
			} else {
				foundBlocks.remove(blockPair.getLeft());
			}
		}

		while (!queuedUnloads.isEmpty()) {
			ChunkPos chunkPos = queuedUnloads.poll();
			queuedChunks.remove(chunkPos);

			for (BlockPos pos: new HashSet<>(foundBlocks)) {
				if (pos.getX() >= chunkPos.getStartX()
						&& pos.getX() <= chunkPos.getEndX()
						&& pos.getZ() >= chunkPos.getStartZ()
						&& pos.getZ() <= chunkPos.getEndZ()) {
					foundBlocks.remove(pos);
				}
			}
		}

		while (!queuedChunks.isEmpty()) {
			submitChunk(queuedChunks.poll());
		}

		for (Entry<ChunkPos, Future<Set<BlockPos>>> f: new HashMap<>(chunkFutures).entrySet()) {
			if (f.getValue().isDone()) {
				try {
					foundBlocks.addAll(f.getValue().get());

					chunkFutures.remove(f.getKey());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (event.getPacket() instanceof DisconnectS2CPacket
				|| event.getPacket() instanceof GameJoinS2CPacket
				|| event.getPacket() instanceof PlayerRespawnS2CPacket) {
			reset();
		} else if (event.getPacket() instanceof BlockUpdateS2CPacket) {
			BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket) event.getPacket();

			queuedBlocks.add(Pair.of(packet.getPos(), packet.getState()));
		} else if (event.getPacket() instanceof ExplosionS2CPacket) {
			ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();

			for (BlockPos pos: packet.getAffectedBlocks()) {
				queuedBlocks.add(Pair.of(pos, Blocks.AIR.getDefaultState()));
			}
		} else if (event.getPacket() instanceof ChunkDeltaUpdateS2CPacket) {
			ChunkDeltaUpdateS2CPacket packet = (ChunkDeltaUpdateS2CPacket) event.getPacket();

			ChunkSectionPos chunkPos = (ChunkSectionPos) FabricReflect.getFieldValue(packet, "field_26345", "sectionPos");
			queuedChunks.add(chunkPos.toChunkPos());
		} else if (event.getPacket() instanceof ChunkDataS2CPacket) {
			ChunkDataS2CPacket packet = (ChunkDataS2CPacket) event.getPacket();

			queuedChunks.add(new ChunkPos(packet.getX(), packet.getZ()));
		} else if (event.getPacket() instanceof UnloadChunkS2CPacket) {
			UnloadChunkS2CPacket packet = (UnloadChunkS2CPacket) event.getPacket();

			queuedUnloads.add(new ChunkPos(packet.getX(), packet.getZ()));
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		int mode = getSetting(0).asMode().mode;

		for (BlockPos pos : foundBlocks) {
			BlockState state = mc.world.getBlockState(pos);

			int color = state.getTopMaterialColor(mc.world, pos).color;
			float red = ((color & 0xff0000) >> 16) / 255f;
			float green = ((color & 0xff00) >> 8) / 255f;
			float blue = (color & 0xff) / 255f;

			VoxelShape voxelShape = state.getOutlineShape(mc.world, pos);
			if (voxelShape.isEmpty()) {
				voxelShape = VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
			}

			if (mode == 0 || mode == 1) {
				float outlineWidth = (float) getSetting(1).asSlider().getValue();

				for (Box box: voxelShape.getBoundingBoxes()) {
					RenderUtils.drawOutline(box.offset(pos), red, green, blue, 1f, outlineWidth);
				}
			}

			if (mode == 0 || mode == 2) {
				float fillAlpha = (float) getSetting(2).asSlider().getValue();

				for (Box box: voxelShape.getBoundingBoxes()) {
					RenderUtils.drawFill(box.offset(pos), red, green, blue, fillAlpha);
				}
			}

			SettingToggle tracers = getSetting(3).asToggle();
			if (tracers.state) {
				// This is bad when bobbing is enabled!
				Vec3d lookVec = new Vec3d(0, 0, 75).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
						.rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
						.add(mc.cameraEntity.getPos().add(0, mc.cameraEntity.getEyeHeight(mc.cameraEntity.getPose()), 0));

				RenderUtils.drawLine(
						lookVec.x, lookVec.y, lookVec.z,
						pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						red, green, blue,
						(float) tracers.getChild(1).asSlider().getValue(),
						(float) tracers.getChild(0).asSlider().getValue());
			}
		}
	}

	private void submitChunk(ChunkPos pos) {
		submitChunk(pos, mc.world.getChunk(pos.x, pos.z));
	}

	private void submitChunk(ChunkPos pos, Chunk chunk) {
		chunkFutures.put(chunk.getPos(), chunkSearchers.submit(new Callable<Set<BlockPos>>() {

			@Override
			public Set<BlockPos> call() throws Exception {
				Set<BlockPos> found = new HashSet<>();

				for (int x = 0; x < 16; x++) {
					for (int y = 0; y <= mc.world.getHeight(); y++) {
						for (int z = 0; z < 16; z++) {
							BlockPos pos = new BlockPos(chunk.getPos().x * 16 + x, y, chunk.getPos().z * 16 + z);
							BlockState state = chunk.getBlockState(pos);

							if (getSetting(4).asList(Block.class).contains(state.getBlock())) {
								found.add(pos);
							}
						}
					}
				}

				return found;
			}
		}));
	}

	private void reset() {
		chunkSearchers.shutdownNow();
		chunkSearchers = Executors.newFixedThreadPool(4);

		chunkFutures.clear();
		foundBlocks.clear();
		queuedChunks.clear();
		queuedUnloads.clear();
		prevBlockList.clear();
	}
}
