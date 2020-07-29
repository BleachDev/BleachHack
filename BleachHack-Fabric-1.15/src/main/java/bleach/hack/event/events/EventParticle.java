package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.particle.Particle;

public class EventParticle extends Event {
	
	public Particle particle;
	
	public EventParticle(Particle particle) {
		this.particle = particle;
	}
}
