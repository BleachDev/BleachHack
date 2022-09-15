package org.bleachhack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
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
public class MixinMovementPackets {
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
                PlayerEntity player = MinecraftClient.getInstance().player;
                double prevX = player.prevX;
                double prevZ = player.prevZ;
                if (changePosition) {
                    long xMod = (long)(x * 1000.0) % 10L;
                    long zMod = (long)(z * 1000.0) % 10L;
                    if (xMod != 0L) {
                        if (prevX > x) {
                            this.x += (double)(10L - xMod) / 1000.0;
                        } else {
                            this.x += (double)(-xMod) / 1000.0;
                        }
                    }

                    if (zMod != 0L) {
                        if (prevZ > z) {
                            this.z += (double)(10L - zMod) / 1000.0;
                        } else {
                            this.z += (double)(-zMod) / 1000.0;
                        }
                    }
                }
            }
        }
    }
}