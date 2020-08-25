package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.network.Packet;

/**
 * Created by 086 on 13/11/2017.
 */
public class PacketEvent extends Event {

    private final Packet packet;

    public PacketEvent(Packet packet) {
        super();
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet packet) {
            super(packet);
        }
    }

    public static class Send extends PacketEvent {
        public Send(Packet packet) {
            super(packet);
        }
    }
}