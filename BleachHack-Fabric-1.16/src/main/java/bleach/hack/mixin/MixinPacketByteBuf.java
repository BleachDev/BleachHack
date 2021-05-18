package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import bleach.hack.module.ModuleManager;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.network.PacketByteBuf;

@Mixin(PacketByteBuf.class)
public class MixinPacketByteBuf {

	@ModifyArg(method = "readCompoundTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;method_30616(Lnet/minecraft/nbt/PositionTracker;)Lnet/minecraft/nbt/CompoundTag;"))
    private PositionTracker increaseLimit(PositionTracker in) {
        return ModuleManager.getModule("AntiChunkBan").isEnabled() ? PositionTracker.DEFAULT : in;
    }
}
