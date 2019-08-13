package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventReadPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Shadow
    private Channel channel;

    @Inject(method = "method_10770", at = @At("HEAD"), cancellable = true)
    public void IchannelRead0(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo callback) {
        if (this.channel.isOpen()) {
            try {
                EventReadPacket event = new EventReadPacket(packet_1);
                BleachHack.getEventBus().post(event);
                if (event.isCancelled()) {
                    callback.cancel();
                }
            } catch (Exception exception) {}
        }
    }
}
