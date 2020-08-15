package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;

public class EventSoundPlay extends Event {

    public static class Normal extends EventSoundPlay {

        public SoundInstance instance;

        public Normal(SoundInstance si) {
            instance = si;
        }
    }

    public static class Preloaded extends EventSoundPlay {

        public Sound sound;

        public Preloaded(Sound s) {
            sound = s;
        }
    }

}
