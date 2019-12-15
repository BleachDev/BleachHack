package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.entity.Entity;

public class EventEntityRender extends Event {
	
	protected Entity entity;
	
	public Entity getEntity() {
		return entity;
	}
	
	public static class Render extends EventEntityRender {
		public Render(Entity entity) {
			this.entity = entity;
		}
	}
	
	public static class Label extends EventEntityRender {
		public Label(Entity entity) {
			this.entity = entity;
		}
	}
}
