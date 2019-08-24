package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.entity.LivingEntity;

public class EventLivingRender extends Event {
	
	private LivingEntity entity;
	
	public EventLivingRender(LivingEntity entity) {
		this.entity = entity;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
}
