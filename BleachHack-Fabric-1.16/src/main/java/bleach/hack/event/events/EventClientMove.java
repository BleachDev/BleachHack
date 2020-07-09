package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.Vec3d;

public class EventClientMove extends Event {

	public Vec3d vec3d;
	
	public EventClientMove(Vec3d vec3d) {
		this.vec3d = vec3d;
	}
}
