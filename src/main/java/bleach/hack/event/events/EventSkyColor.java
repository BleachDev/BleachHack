package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.Vec3d;

public class EventSkyColor extends Event {

    private final float tickDelta;
    private Vec3d color = null;

    public EventSkyColor(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public void setColor(float[] color) {
        this.color = new Vec3d(color[0], color[1], color[2]);
    }

    public Vec3d getColor() {
        return color;
    }

    public static class SkyColor extends EventSkyColor {

        public SkyColor(float tickDelta) {
            super(tickDelta);
        }
    }

    public static class CloudColor extends EventSkyColor {

        public CloudColor(float tickDelta) {
            super(tickDelta);
        }
    }

    public static class FogColor extends EventSkyColor {

        public FogColor(float tickDelta) {
            super(tickDelta);
        }
    }
}
