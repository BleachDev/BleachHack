package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;

public class EventParticle extends Event {

    public static class Normal extends EventParticle {

        public Particle particle;

        public Normal(Particle particle) {
            this.particle = particle;
        }
    }

    public static class Emitter extends EventParticle {

        public ParticleEffect effect;

        public Emitter(ParticleEffect effect) {
            this.effect = effect;
        }
    }
}
