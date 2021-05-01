package bleach.hack.epearledition.mixin;

import bleach.hack.epearledition.BleachHack;
import bleach.hack.epearledition.event.events.EventLoadChunk;
import bleach.hack.epearledition.event.events.EventReadPacket;
import bleach.hack.epearledition.event.events.EventUnloadChunk;
import net.minecraft.client.MinecraftClient;
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
        EventLoadChunk event = new EventLoadChunk(cir.getReturnValue());
        BleachHack.eventBus.post(event);
        //if (event.isCancelled()) cir.cancel();
    }


    @Inject(at = @At("HEAD"), cancellable = true, method = "unload(II)V")
    public void unload(int chunkX, int chunkZ, CallbackInfo ci) {
        BleachHack.eventBus.post(new EventUnloadChunk(chunkX, chunkZ));
    }
}
