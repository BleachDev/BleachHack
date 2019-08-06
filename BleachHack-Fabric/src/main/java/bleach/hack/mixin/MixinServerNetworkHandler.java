package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.command.CommandManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinServerNetworkHandler {
	
	@Inject(at = @At("HEAD"), method = "sendPacket(Lnet/minecraft/Packet;)V", cancellable = true)
	public void sendPacket(Packet<?> packet_1, CallbackInfo info) {
		if(!(packet_1 instanceof ChatMessageC2SPacket)) return;
		
		ChatMessageC2SPacket packet = (ChatMessageC2SPacket) packet_1;
		
		if(packet.getChatMessage().startsWith(".")) {
    		CommandManager cmd = new CommandManager();
    		cmd.callCommand(packet.getChatMessage().substring(1));
    		info.cancel();
    	}
	}
}
