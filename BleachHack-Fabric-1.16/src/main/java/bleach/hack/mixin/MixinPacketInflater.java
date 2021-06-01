package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import bleach.hack.module.ModuleManager;
import net.minecraft.network.PacketInflater;

@Mixin(PacketInflater.class)
public class MixinPacketInflater {

	@ModifyConstant(method = "decode", constant = @Constant(intValue = 2097152),
			require = 0 /* TODO inertia */)
	private int increaseDecodeLimit(int old) {
		return ModuleManager.getModule("AntiChunkBan").isEnabled() ? Integer.MAX_VALUE : old;
	}
}
