package bleach.hack.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.command.CommandManager;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayHandler {
	
	@Inject(at = @At("HEAD"), method = "sendPacket(Lnet/minecraft/network/Packet;)V", cancellable = true)
	public void sendPacket(Packet<?> packet_1, CallbackInfo info) {
		if(packet_1 instanceof ChatMessageC2SPacket) {
			ChatMessageC2SPacket pack = (ChatMessageC2SPacket) packet_1;
			if(pack.getChatMessage().startsWith(".")) {
				CommandManager cmd = new CommandManager();
	    		cmd.callCommand(pack.getChatMessage().substring(1));
				info.cancel();
			}
		}
	}
}
