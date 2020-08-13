package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSignBlockEntityRender;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntityRenderer.class)
public class MixinSignBlockEntityRenderer {

	@Inject(method = "method_3582", at = @At("HEAD"), cancellable = true)
	private void render(SignBlockEntity signBlockEntity_1, double double_1, double double_2, double double_3, float float_1, int int_1, CallbackInfo ci) {
		EventSignBlockEntityRender event = new EventSignBlockEntityRender(signBlockEntity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) ci.cancel();
	}

}
