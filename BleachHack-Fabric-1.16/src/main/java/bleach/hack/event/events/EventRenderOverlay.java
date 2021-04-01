package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.Identifier;

public class EventRenderOverlay extends Event {

	private Identifier texture;
	private float opacity;
	
	public EventRenderOverlay(Identifier texture, float opacity) {
		setTexture(texture);
		setOpacity(opacity);
	}

	public Identifier getTexture() {
		return texture;
	}

	public void setTexture(Identifier texture) {
		this.texture = texture;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

}
