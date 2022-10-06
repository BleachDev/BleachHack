package org.bleachhack.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Main.class)
public class MixinMinecraftClientMain {
    @ModifyConstant(method = "<clinit>", constant = @Constant(stringValue = "true"))
    private static String whyHeadless(String constant) {
        return "false";
    }
}
