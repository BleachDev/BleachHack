package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.DotLock;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity2 {

    @Inject(at = @At("HEAD"), method = "applyDamage", cancellable = true)
    public void dotLock(CallbackInfo info) {
        if (ModuleManager.getModule(DotLock.class).isToggled()) info.cancel();
    }
}
