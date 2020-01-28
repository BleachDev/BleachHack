package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
	
	@Inject(at = @At("HEAD"), method = "sendMovementPackets()V", cancellable = true)
	public void sendMovementPackets(CallbackInfo info) {
		EventMovementTick event = new EventMovementTick();
		BleachHack.eventBus.post(new EventMovementTick());
		if (event.isCancelled()) info.cancel();
	}

}

