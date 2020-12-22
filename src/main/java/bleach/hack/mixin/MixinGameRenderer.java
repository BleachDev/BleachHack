/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleManager;
//import bleach.hack.module.mods.LiquidInteract;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "renderHand", cancellable = true)
    private void renderHand(MatrixStack matrixStack_1, Camera camera, float tickDelta, CallbackInfo info) {
        EventWorldRender event = new EventWorldRender(tickDelta);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) info.cancel();
    }


    @Inject(
            at = @At("HEAD"),
            method = "bobViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            cancellable = true)
    private void onBobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(2).asToggle().state)
            ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "showFloatingItem", cancellable = true)
    private void showFloatingItem(ItemStack itemStack_1, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(8).asToggle().state
                && itemStack_1.getItem() == Items.TOTEM_OF_UNDYING)
            ci.cancel();
    }

    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0),
            method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V")
    private float nauseaWobble(float delta, float first, float second) {
        if (!(ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(6).asToggle().state))
            return MathHelper.lerp(delta, first, second);

        return 0;
    }

    //@Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    //private HitResult updateTargetedEntityEntityRayTraceProxy(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
    //    if (ModuleManager.getModule(LiquidInteract.class).isToggled()) return entity.raycast(maxDistance, tickDelta, true);
    //    return entity.raycast(maxDistance, tickDelta, includeFluids);
    //}
}
