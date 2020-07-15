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
import bleach.hack.event.events.EventBlockBreakingProgress;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import bleach.hack.module.mods.SpeedMine;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 3))
    public void updateBlockBreakingProgress(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled() ?
        		(int) ModuleManager.getModule(Nuker.class).getSettings().get(2).asSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).asMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).asSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", ordinal = 4))
    public void updateBlockBreakingProgress2(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled()
        		? (int) ModuleManager.getModule(Nuker.class).getSettings().get(2).asSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).asMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).asSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }

    @Redirect(method = "attackBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I"))
    public void attackBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, int i) {
        i = ModuleManager.getModule(Nuker.class).isToggled()
        		? (int) ModuleManager.getModule(Nuker.class).getSettings().get(2).asSlider().getValue()
        		: ModuleManager.getModule(SpeedMine.class).isToggled()
        		&& ModuleManager.getModule(SpeedMine.class).getSettings().get(0).asMode().mode == 1
                ? (int) ModuleManager.getModule(SpeedMine.class).getSettings().get(2).asSlider().getValue() : 5;
        this.blockBreakingCooldown = i;
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEntityId()I", ordinal = 0),
            method = "updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", cancellable = true)
    private void onPlayerDamageBlock(BlockPos blockPos_1, Direction direction_1, CallbackInfoReturnable<Boolean> info) {
        EventBlockBreakingProgress event = new EventBlockBreakingProgress(blockPos_1, direction_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) info.cancel();
    }
}
