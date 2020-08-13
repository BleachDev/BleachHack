package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.entity.Entity;

public class EventOutlineColor extends Event {

	public Entity entity;
	public int color;

	public EventOutlineColor(Entity entity, int color) {
		this.entity = entity;
		this.color = color;
	}

}
