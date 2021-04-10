/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventRenderShader;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

	@Shadow private ShaderEffect shader;

	@Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
	private void onBobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo ci) {
		if (ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(2).asToggle().state) {
			ci.cancel();
		}
	}

	@Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
	private void showFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
		if (ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(8).asToggle().state
				&& floatingItem.getItem() == Items.TOTEM_OF_UNDYING) {
			ci.cancel();
		}
	}

	@Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0),
			require = 0 /* TODO: meteor */)
	private float nauseaWobble(float delta, float first, float second) {
		if (!(ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(6).asToggle().state)) {
			return MathHelper.lerp(delta, first, second);
		}

		return 0;
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;shader:Lnet/minecraft/client/gl/ShaderEffect;", ordinal = 0))
	private ShaderEffect render_Shader(GameRenderer renderer, float tickDelta) {
		EventRenderShader event = new EventRenderShader(shader);
		BleachHack.eventBus.post(event);

		if (event.getEffect() != null) {
			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.disableAlphaTest();
			RenderSystem.enableTexture();
			RenderSystem.matrixMode(GL11.GL_TEXTURE);
			RenderSystem.pushMatrix();
			RenderSystem.loadIdentity();
			event.getEffect().render(tickDelta);
			RenderSystem.popMatrix();
		}

		return null;
	}
}
