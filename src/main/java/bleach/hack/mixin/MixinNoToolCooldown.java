package bleach.hack.mixin;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoToolCooldown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class MixinNoToolCooldown {

    @Shadow
    public float lastFrameDuration;

    @Shadow
    public float tickDelta;

    @Shadow
    private long prevTimeMillis;

    @Shadow
    private float tickTime;

    @Inject(at = @At("HEAD"), method = "beginRenderTick", cancellable = true)
    public void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
        Module ntc = ModuleManager.getModule(NoToolCooldown.class);
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc == null ? null : mc.player;
        if (player != null && player.handSwinging && ntc != null && ntc.isToggled()) {
            this.lastFrameDuration = (float) (((timeMillis - this.prevTimeMillis) / this.tickTime)
                    * ModuleManager.getModule(NoToolCooldown.class).getSetting(0).asSlider().getValue());
            this.prevTimeMillis = timeMillis;
            this.tickDelta += this.lastFrameDuration;
            int i = (int) this.tickDelta;
            this.tickDelta -= i;

            ci.setReturnValue(i);
            ci.cancel();
        }
    }

}
