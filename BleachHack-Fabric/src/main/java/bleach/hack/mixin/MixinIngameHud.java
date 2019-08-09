package bleach.hack.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;

@Mixin(InGameHud.class)
public class MixinIngameHud {
	
	@Inject(at = @At(value = "RETURN"), method = "render(F)V")
	public void render(float float_1, CallbackInfo info) {
		try {
			for(Module m: ModuleManager.getModules()) {
				if(m.isToggled()) m.onOverlay();
			}
		}catch(Exception e) {}
	}
}
