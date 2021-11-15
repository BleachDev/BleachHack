package org.bleachhack.mixin;

import org.bleachhack.module.ModuleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {

	@ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt(Lnet/minecraft/nbt/NbtTagSizeTracker;)Lnet/minecraft/nbt/NbtCompound;"))
    private NbtTagSizeTracker increaseLimit(NbtTagSizeTracker in) {
        return ModuleManager.getModule("AntiChunkBan").isEnabled() ? NbtTagSizeTracker.EMPTY : in;
    }
}
