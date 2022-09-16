package org.bleachhack.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.util.math.BlockPos;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.SignRestorer;
import org.bleachhack.util.SignData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public class MixinSignEditScreen {
    @Shadow
    @Final
    private String[] text;

    @Shadow
    @Final
    public SignBlockEntity sign;

    @Inject(at = {@At("HEAD")}, method = {"init()V"})
    private void onInit(CallbackInfo ci) {
        Module signRestorer = ModuleManager.getModule(SignRestorer.class);

        if (signRestorer.isEnabled()) {
            BlockPos pos = sign.getPos();
            SignData data = null;
            SignData tempData = null;
            for (int i = 0; i < SignRestorer.signData.size(); i++) {
                tempData = SignRestorer.signData.get(i).get(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                if (tempData != null) {
                    data = tempData;
                }
            }
            if (data == null) return;


            for (int i = 0; i < 4; i++)
                text[i] = data.text[i];

            finishEditing();
        }
    }

    @Shadow
    private void finishEditing() {

    }
}
