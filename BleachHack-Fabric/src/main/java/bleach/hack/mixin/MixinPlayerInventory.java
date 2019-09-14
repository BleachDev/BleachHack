package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.SpeedMine;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
    @Shadow
    public DefaultedList<ItemStack> main;
    @Shadow
    public int selectedSlot;

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    public void getBlockBreakingSpeed(BlockState blockState_1, CallbackInfoReturnable<Float> callback) {
        SpeedMine speedMine = (SpeedMine) ModuleManager.getModule(SpeedMine.class);
        if(speedMine.isToggled() && speedMine.getSettings().get(0).toMode().mode == 1) {
            callback.setReturnValue((float) (this.main.get(this.selectedSlot).getMiningSpeed(blockState_1) * speedMine.getSettings().get(3).toSlider().getValue()));
            callback.cancel();
        }
    }
}
