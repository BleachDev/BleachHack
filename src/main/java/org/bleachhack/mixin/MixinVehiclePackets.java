package org.bleachhack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
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

@Mixin({VehicleMoveC2SPacket.class})
public class MixinVehiclePackets {
    @Shadow
    @Mutable
    @Final
    private double x;
    @Shadow
    @Mutable
    @Final
    private double z;
    @Shadow
    @Final
    private double y;

    @Inject(at = {@At("RETURN")}, method = {"<init>(Lnet/minecraft/entity/Entity;)V"})
    public void onMovePacket(Entity e, CallbackInfo ci) {
        Module iamBot = ModuleManager.getModule(IamBot.class);

        if (iamBot.isEnabled() && iamBot.getSetting(0).asMode().getMode() == 1) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            double prevX = player.prevX;
            double prevZ = player.prevZ;
            long xMod = (long) (this.x * 1000.0) % 10L;
            long zMod = (long) (this.x * 1000.0) % 10L;
            if (xMod != 0L) {
                if (prevX > this.x) {
                    this.x += (double) (10L - xMod) / 1000.0;
                } else {
                    this.x += (double) (-xMod) / 1000.0;
                }
            }

            if (zMod != 0L) {
                if (prevZ > this.z) {
                    this.z += (double) (10L - zMod) / 1000.0;
                } else {
                    this.z += (double) (-zMod) / 1000.0;
                }
            }

            player.setPos(this.x, this.y, this.z);
        }
    }

    @Inject(at = {@At("RETURN")}, method = {"<init>(Lnet/minecraft/network/PacketByteBuf;)V"})
    public void onMove2(PacketByteBuf buf, CallbackInfo ci) {
        Module iamBot = ModuleManager.getModule(IamBot.class);

        if (iamBot.isEnabled() && iamBot.getSetting(0).asMode().getMode() == 1) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            double prevX = player.prevX;
            double prevZ = player.prevZ;
            long xMod = (long) (this.x * 1000.0) % 10L;
            long zMod = (long) (this.z * 1000.0) % 10L;
            if (xMod != 0L) {
                if (prevX > this.x) {
                    this.x += (double) (10L - xMod) / 1000.0;
                } else {
                    this.x += (double) (-xMod) / 1000.0;
                }
            }

            if (zMod != 0L) {
                if (prevZ > this.z) {
                    this.z += (double) (10L - zMod) / 1000.0;
                } else {
                    this.z += (double) (-zMod) / 1000.0;
                }
            }

            player.setPos(this.x, this.y, this.z);
        }
    }
}
