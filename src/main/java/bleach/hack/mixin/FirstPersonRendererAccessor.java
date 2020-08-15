package bleach.hack.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface FirstPersonRendererAccessor {
    @Accessor("mainHand")
    void setItemStackMainHand(ItemStack value);

    @Accessor("offHand")
    void setItemStackOffHand(ItemStack value);

    @Accessor("equipProgressMainHand")
    void setEquippedProgressMainHand(float value);

    @Accessor("prevEquipProgressMainHand")
    void setPrevEquippedProgressMainHand(float value);

    @Accessor("equipProgressOffHand")
    void setEquippedProgressOffHand(float value);

    @Accessor("prevEquipProgressOffHand")
    void setPrevEquippedProgressOffHand(float value);
}
