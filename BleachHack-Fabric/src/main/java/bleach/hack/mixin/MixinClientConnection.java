package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Shadow
    private Channel channel;

    @Shadow
    private void sendImmediately(Packet<?> packet_1, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1) {}

    @Inject(method = "method_10770", at = @At("HEAD"), cancellable = true)
    public void IchannelRead0(ChannelHandlerContext channelHandlerContext_1, Packet<?> packet_1, CallbackInfo callback) {
        if (this.channel.isOpen()) {
            try {
                EventReadPacket event = new EventReadPacket(packet_1);
                BleachHack.eventBus.post(event);
                if (event.isCancelled()) callback.cancel();
            } catch (Exception exception) {}
        }
    }

    @Redirect(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "net/minecraft/network/ClientConnection.sendImmediately(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"))
    private void sendPacket(ClientConnection clientConnection_1, Packet<?> packet_1, final GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1) {
        EventSendPacket event = new EventSendPacket(packet_1);
        BleachHack.eventBus.post(event);

        if (event.isCancelled()) return;

        sendImmediately(event.getPacket(), genericFutureListener_1);
    }
}
