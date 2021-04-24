package bleach.hack.epearledition.event.events;

import bleach.hack.epearledition.event.Event;
import net.minecraft.world.chunk.WorldChunk;

public class EventLoadChunk extends Event {
    private WorldChunk chunk;

    public EventLoadChunk(WorldChunk chunk) {
        this.chunk = chunk;
    }

    public WorldChunk getChunk() {
        return chunk;
    }
}
