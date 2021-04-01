package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.Identifier;

public class EventRenderOverlay extends Event {

	private Identifier texture;
	private float opacity;
	
	public EventRenderOverlay(Identifier texture, float opacity) {
		this.texture = texture;
		this.opacity = opacity;
	}

	public Identifier getTexture() {
		return texture;
	}

	public float getOpacity() {
		return opacity;
	}

}
