package bleach.hack.mixin;

import net.minecraft.client.render.FirstPersonRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FirstPersonRenderer.class)
public interface FirstPersonRendererAccessor {
    @Accessor("mainHand")
    public abstract void setItemStackMainHand(ItemStack value);
    @Accessor("offHand")
    public abstract void setItemStackOffHand(ItemStack value);
    @Accessor("equipProgressMainHand")
    public abstract void setEquippedProgressMainHand(float value);
    @Accessor("prevEquipProgressMainHand")
    public abstract void setPrevEquippedProgressMainHand(float value);
    @Accessor("equipProgressOffHand")
	public abstract void setEquippedProgressOffHand(float value);
    @Accessor("prevEquipProgressOffHand")
    public abstract void setPrevEquippedProgressOffHand(float value);
}
