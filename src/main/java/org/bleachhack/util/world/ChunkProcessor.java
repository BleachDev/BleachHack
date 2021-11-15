package org.bleachhack.util.world;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventReadPacket;
import org.bleachhack.eventbus.BleachSubscribe;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkProcessor {

	private ExecutorService executor;

	private int threads;
	private BiConsumer<ChunkPos, Chunk> loadChunkConsumer;
	private BiConsumer<ChunkPos, Chunk> unloadChunkConsumer;
	private BiConsumer<BlockPos, BlockState> updateBlockConsumer;

	public ChunkProcessor(int threads,
			BiConsumer<ChunkPos, Chunk> loadChunkConsumer,
			BiConsumer<ChunkPos, Chunk> unloadChunkConsumer,
			BiConsumer<BlockPos, BlockState> updateBlockConsumer) {
		this.threads = threads;
		this.loadChunkConsumer = loadChunkConsumer;
		this.unloadChunkConsumer = unloadChunkConsumer;
		this.updateBlockConsumer = updateBlockConsumer;
	}

	public void start() {
		executor = Executors.newFixedThreadPool(threads);
		BleachHack.eventBus.subscribe(this);
	}

	public void stop() {
		BleachHack.eventBus.unsubscribe(this);
		executor.shutdownNow();
		executor = null;
	}

	public void restartExecutor() {
		executor.shutdownNow();
		executor = Executors.newFixedThreadPool(threads);
	}

	public void submitAllLoadedChunks() {
		if (loadChunkConsumer != null) {
			for (Chunk chunk: WorldUtils.getLoadedChunks()) {
				executor.execute(() -> loadChunkConsumer.accept(chunk.getPos(), chunk));
			}
		}
	}

	@BleachSubscribe
	public void onReadPacket(EventReadPacket event) {
		if (updateBlockConsumer != null && event.getPacket() instanceof BlockUpdateS2CPacket) {
			BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket) event.getPacket();

			executor.execute(() -> updateBlockConsumer.accept(packet.getPos(), packet.getState()));
		} else if (updateBlockConsumer != null && event.getPacket() instanceof ExplosionS2CPacket) {
			ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();

			for (BlockPos pos: packet.getAffectedBlocks()) {
				executor.execute(() -> updateBlockConsumer.accept(pos, Blocks.AIR.getDefaultState()));
			}
		} else if (updateBlockConsumer != null && event.getPacket() instanceof ChunkDeltaUpdateS2CPacket) {
			ChunkDeltaUpdateS2CPacket packet = (ChunkDeltaUpdateS2CPacket) event.getPacket();

			packet.visitUpdates((pos, state) -> {
				// java bruh moment lambda bruh to prevent it from not becoming immutable
				BlockPos impos/*ter*/ = pos.toImmutable();
				executor.execute(() -> updateBlockConsumer.accept(impos, state));
			});
		} else if (loadChunkConsumer != null && event.getPacket() instanceof ChunkDataS2CPacket) {
			ChunkDataS2CPacket packet = (ChunkDataS2CPacket) event.getPacket();

			ChunkPos cp = new ChunkPos(packet.getX(), packet.getZ());
			WorldChunk chunk = new WorldChunk(MinecraftClient.getInstance().world, cp, null);
			chunk.loadFromPacket(null, packet.getReadBuffer(), new NbtCompound(), packet.getVerticalStripBitmask());

			executor.execute(() -> loadChunkConsumer.accept(cp, chunk));
		} else if (unloadChunkConsumer != null && event.getPacket() instanceof UnloadChunkS2CPacket) {
			UnloadChunkS2CPacket packet = (UnloadChunkS2CPacket) event.getPacket();

			ChunkPos cp = new ChunkPos(packet.getX(), packet.getZ());
			WorldChunk chunk = MinecraftClient.getInstance().world.getChunk(cp.x, cp.z);

			executor.execute(() -> unloadChunkConsumer.accept(cp, chunk));
		}
	}
}
