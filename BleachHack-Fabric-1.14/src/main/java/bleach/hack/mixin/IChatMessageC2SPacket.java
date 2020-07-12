package bleach.hack.mixin;

import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageC2SPacket.class)
public interface IChatMessageC2SPacket {

    @Accessor
    String getChatMessage();

    @Accessor
    void setChatMessage(String newMessage);

}