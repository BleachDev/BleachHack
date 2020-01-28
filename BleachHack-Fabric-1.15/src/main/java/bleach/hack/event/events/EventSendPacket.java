package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.network.Packet;

public class EventSendPacket extends Event {
    private Packet<?> packet;

    public EventSendPacket(Packet<?> packet){
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
