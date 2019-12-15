package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import bleach.hack.module.mods.SpeedMine;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 3))
    public void updateBlockBreakingProgress(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled() ?
        		(int) ModuleManager.getModule(Nuker.class).getSettings().get(2).toSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).toMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).toSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 4))
    public void updateBlockBreakingProgress2(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled()
        		? (int) ModuleManager.getModule(Nuker.class).getSettings().get(2).toSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).toMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).toSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }

    @Redirect(method = "attackBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I"))
    public void attackBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled()
        		? (int) ModuleManager.getModule(Nuker.class).getSettings().get(2).toSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).toMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).toSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }
}
