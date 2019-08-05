package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.command.CommandManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerNetworkHandler {
	
	@Inject(at = @At("HEAD"), method = "onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V", cancellable = true)
	public void onChatMessage(ChatMessageC2SPacket chatMessageC2SPacket_1, CallbackInfo info) {
		if(chatMessageC2SPacket_1.getChatMessage().startsWith(".")) {
    		CommandManager cmd = new CommandManager();
    		cmd.callCommand(chatMessageC2SPacket_1.getChatMessage().substring(1));
    		info.cancel();
    	}
	}
}
