package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventPreTick;
import bleach.hack.event.events.EventTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.utils.BleachQueue;
import bleach.hack.utils.file.BleachFileHelper;

@Mixin(ClientPlayerEntity.class)
public class MixinPlayerEntity {
	
	@Inject(at = @At("RETURN"), method = "tick()V", cancellable = true)
	public void tick(CallbackInfo info) {
		try {
			if(MinecraftClient.getInstance().player.age % 100 == 0) {
				BleachFileHelper.saveModules();
				BleachFileHelper.saveSettings();
				BleachFileHelper.saveBinds();
				BleachFileHelper.saveClickGui();
			}
			
			BleachQueue.nextQueue();
		}catch(Exception e) {}
		EventTick event = new EventTick();
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
	public void tick2(CallbackInfo info) {
		EventPreTick event = new EventPreTick();
		BleachHack.eventBus.post(new EventPreTick());
		if (event.isCancelled()) info.cancel();
	}
}
