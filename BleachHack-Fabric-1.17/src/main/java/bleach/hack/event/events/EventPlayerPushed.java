package bleach.hack.event.events;

import net.minecraft.util.math.Vec3d;

public class EventPlayerPushed {
	
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
