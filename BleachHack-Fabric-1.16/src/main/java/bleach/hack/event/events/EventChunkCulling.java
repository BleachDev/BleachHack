package bleach.hack.event.events;

import bleach.hack.event.Event;

public class EventChunkCulling extends Event {

	private boolean shouldCull;

	public EventChunkCulling(boolean shouldCull) {
		this.setCull(shouldCull);
	}

	public boolean shouldCull() {
		return shouldCull;
	}

	public void setCull(boolean shouldCull) {
		this.shouldCull = shouldCull;
	}
}
