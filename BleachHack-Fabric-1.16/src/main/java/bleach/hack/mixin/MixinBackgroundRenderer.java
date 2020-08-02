package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSkyColor;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

	@Shadow private static float red;
	@Shadow private static float green;
	@Shadow private static float blue;

	private static float oldr = Float.NaN, oldg = Float.NaN, oldb = Float.NaN;

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"), method = {"render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZ)V"})
	private static boolean hasStatusEffect(LivingEntity entity, StatusEffect effect) {
		if(effect == StatusEffects.BLINDNESS && ModuleManager.getModule(NoRender.class).isToggled()
				&& ModuleManager.getModule(NoRender.class).getSetting(0).asToggle().state)
			return false;

		return entity.hasStatusEffect(effect);
	}

	@Inject(at = @At("HEAD"), method = "applyFog", cancellable = true)
	private static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
		if (fogType != BackgroundRenderer.FogType.FOG_SKY) return;
		
		EventSkyColor.FogColor event = new EventSkyColor.FogColor(1f);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) {
			ci.cancel();
			return;
		} else if (event.getColor() != null) {
			oldr = (float) event.getColor().x;
			oldg = (float) event.getColor().y;
			oldb = (float) event.getColor().z;
			ci.cancel();
		} else {
			oldr = Float.NaN;
			oldg = Float.NaN;
			oldb = Float.NaN;
		}
	}
	
	@Inject(at = @At("RETURN"), method = "applyFog", cancellable = true)
	private static void applyFog1(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
		if (oldr != Float.NaN && oldg != Float.NaN && oldb != Float.NaN) {
			red = oldr;
			green = oldg;
			blue = oldb;
			oldr = Float.NaN;
			oldg = Float.NaN;
			oldb = Float.NaN;
		}
	}
}
