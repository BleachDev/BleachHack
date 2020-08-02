package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EventSkyColor extends Event {
	
	private float tickDelta;
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
		
		private BlockPos pos;

		public SkyColor(BlockPos pos, float tickDelta) {
			super(tickDelta);
			this.pos = pos;
		}
		
		public BlockPos getPos() {
			return pos;
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
