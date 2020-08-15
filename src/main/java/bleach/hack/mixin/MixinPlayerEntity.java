package bleach.hack.mixin;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.SpeedMine;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    @Shadow
    public PlayerInventory inventory;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(at = @At("HEAD"), method = "getBlockBreakingSpeed", cancellable = true)
    public void getBlockBreakingSpeed(BlockState blockState_1, CallbackInfoReturnable<Float> ci) {
        Module mod = ModuleManager.getModule(SpeedMine.class);

        if (mod.isToggled()) {
            float float_1 = inventory.getBlockBreakingSpeed(blockState_1);
            if (float_1 > 1.0F) {
                int int_1 = EnchantmentHelper.getEfficiency(this);
                ItemStack itemStack_1 = this.getMainHandStack();
                if (int_1 > 0 && !itemStack_1.isEmpty()) {
                    float_1 += (float) (int_1 * int_1 + 1);
                }
            }

            if (StatusEffectUtil.hasHaste(this)) {
                float_1 *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
            }

            if (!mod.getSetting(4).asToggle().state) {
                if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                    float float_5;
                    switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                        case 0:
                            float_5 = 0.3F;
                            break;
                        case 1:
                            float_5 = 0.09F;
                            break;
                        case 2:
                            float_5 = 0.0027F;
                            break;
                        case 3:
                        default:
                            float_5 = 8.1E-4F;
                    }

                    float_1 *= float_5;
                }
            }

            if (!mod.getSetting(5).asToggle().state) {
                if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
                    float_1 /= 5.0F;
                }

                if (!this.onGround) {
                    float_1 /= 5.0F;
                }
            }

            if (mod.getSetting(0).asMode().mode == 1) float_1 *= (float) mod.getSetting(3).asSlider().getValue();

            ci.setReturnValue(float_1);
            ci.cancel();
        }
    }
}
