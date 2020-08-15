package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class EventClientMove extends Event {

    public MovementType type;
    public Vec3d vec3d;

    public EventClientMove(MovementType type, Vec3d vec3d) {
        this.type = type;
        this.vec3d = vec3d;
    }
}
