package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.entity.damage.DamageSource;

public class EventDamage extends Event {

	public static class Normal extends EventDamage {

		private DamageSource source;
		private float amount;

		public Normal(DamageSource source, float amount) {
			this.source = source;
			this.amount = amount;
		}

		public DamageSource getSource() {
			return source;
		}

		public float getAmount() {
			return amount;
		}
	}

	public static class Knockback extends EventDamage {

		private double velX;
		private double velY;
		private double velZ;

		public Knockback(double velX, double velY, double velZ) {
			this.velX = velX;
			this.velY = velY;
			this.velZ = velZ;
		}

		public double getVelX() {
			return velX;
		}

		public void setVelX(double velX) {
			this.velX = velX;
		}

		public double getVelY() {
			return velY;
		}

		public void setVelY(double velY) {
			this.velY = velY;
		}

		public double getVelZ() {
			return velZ;
		}

		public void setVelZ(double velZ) {
			this.velZ = velZ;
		}
	}
}
