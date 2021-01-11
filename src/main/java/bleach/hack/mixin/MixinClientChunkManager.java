package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventLoadChunk;
import bleach.hack.event.events.EventUnloadChunk;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(at = @At("RETURN"), cancellable = true, method = "loadChunkFromPacket(IILnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/CompoundTag;IZ)Lnet/minecraft/world/chunk/WorldChunk;")
    public void loadChunkFromPacket(int x, int z, BiomeArray biomes, PacketByteBuf buf, CompoundTag tag, int verticalStripBitmask, boolean complete, CallbackInfoReturnable<WorldChunk> cir) {
        BleachHack.eventBus.post(new EventLoadChunk(cir.getReturnValue()));
    }


    @Inject(at = @At("HEAD"), cancellable = true, method = "unload(II)V")
    public void unload(int chunkX, int chunkZ, CallbackInfo ci) {
        BleachHack.eventBus.post(new EventUnloadChunk(chunkX, chunkZ));
    }
}
