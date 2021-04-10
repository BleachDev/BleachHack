package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSkyRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.util.BleachQueue;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

	@Final @Shadow private SkyProperties skyProperties;

	@Inject(method = "tickEntities", at = @At("HEAD"), cancellable = true)
	public void tickEntities(CallbackInfo info) {
		try {
			if (MinecraftClient.getInstance().player.age % 100 == 0) {
				if (BleachFileHelper.SCHEDULE_SAVE_MODULES)
					BleachFileHelper.saveModules();
				if (BleachFileHelper.SCHEDULE_SAVE_CLICKGUI)
					BleachFileHelper.saveClickGui();
				if (BleachFileHelper.SCHEDULE_SAVE_FRIENDS)
					BleachFileHelper.saveFriends();
			}

			BleachQueue.nextQueue();
		} catch (Exception e) {}

		EventTick event = new EventTick();
		BleachHack.eventBus.post(event);
		if (event.isCancelled())
			info.cancel();
	}

	@Inject(method = "method_23777", at = @At("HEAD"), cancellable = true)
	public void method_23777(Vec3d vec, float f, CallbackInfoReturnable<Vec3d> ci) {
		EventSkyRender.Color.SkyColor event = new EventSkyRender.Color.SkyColor(f);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.setReturnValue(Vec3d.ZERO);
		} else if (event.getColor() != null) {
			ci.setReturnValue(event.getColor());
		}
	}

	@Inject(method = "getCloudsColor", at = @At("HEAD"), cancellable = true)
	public void getCloudsColor(float f, CallbackInfoReturnable<Vec3d> ci) {
		EventSkyRender.Color.CloudColor event = new EventSkyRender.Color.CloudColor(f);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.setReturnValue(Vec3d.ZERO);
		} else if (event.getColor() != null) {
			ci.setReturnValue(event.getColor());
		}
	}

	@Overwrite
	public SkyProperties getSkyProperties() {
		if (MinecraftClient.getInstance().world == null) {
			return skyProperties;
		}

		EventSkyRender.Properties event = new EventSkyRender.Properties(skyProperties);
		BleachHack.eventBus.post(event);

		return event.getSky();
	}
}
