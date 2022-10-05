package org.bleachhack.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinMinecraftClientMain {
    @Inject(method = "main([Ljava/lang/String;Z)V", at = @At("HEAD"))
    private static void whyHeadless(String[] args, boolean bl, CallbackInfo ci) {
        System.setProperty("java.awt.headless", "false");
    }
}
