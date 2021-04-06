package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.Vec3d;

public class EventPlayerPushed extends Event {
	
	private Vec3d push;

	public EventPlayerPushed(Vec3d push) {
		this.push = push;
	}

	public Vec3d getPush() {
		return push;
	}

	public void setPush(Vec3d push) {
		this.push = push;
	}
}
