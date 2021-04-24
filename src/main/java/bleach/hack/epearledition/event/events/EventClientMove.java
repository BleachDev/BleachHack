package bleach.hack.epearledition.event.events;

import bleach.hack.epearledition.event.Event;
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
