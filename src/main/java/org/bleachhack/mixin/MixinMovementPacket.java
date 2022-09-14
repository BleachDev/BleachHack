package org.bleachhack.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.IamBot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerMoveC2SPacket.class})
public class MixinMovementPacket {
    @Shadow
    @Mutable
    @Final
    protected double x;

    @Shadow
    @Mutable
    @Final
    protected double z;

    @Inject(at = @At("RETURN"), method = "<init>*")
    public void onMovePacket(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean changePosition, boolean changeLook, CallbackInfo ci) {
        Module iamBot = ModuleManager.getModule(IamBot.class);

        if (iamBot.isEnabled()) {
            if (iamBot.getSetting(0).asMode().getMode() == 0) {
                this.x = (((long) x * 100) / 100.0);
                this.z = (((long) z * 100) / 100.0);
            }

            if (iamBot.getSetting(0).asMode().getMode() == 1) {
                if (changePosition) {
                    long xMod = (long) (x * 1000.0) % 10L;
                    long zMod = (long) (z * 1000.0) % 10L;
                    if (xMod != 0L) {
                        this.x += (double) (xMod > 5L ? 10L - xMod : -xMod) / 1000.0;
                    }

                    if (zMod != 0L) {
                        this.z += (double) (zMod > 5L ? 10L - zMod : -zMod) / 1000.0;
                    }
                }
            }
        }
    }
}
