package bleach.hack.mixin;

import net.minecraft.entity.passive.LlamaEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.EntityControl;

@Mixin(LlamaEntity.class)
public abstract class MixinLlamaEntity {

	@Inject(at = @At("HEAD"), method = "canBeControlledByRider()Z", cancellable = true)
	public void canBeControlledByRider(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(ModuleManager.getModule(EntityControl.class).isToggled());
	}
}
