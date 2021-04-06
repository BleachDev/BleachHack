package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.gl.ShaderEffect;

public class EventRenderShader extends Event {
	
	private ShaderEffect effect;
	
	public EventRenderShader(ShaderEffect effect) {
		this.setEffect(effect);
	}

	public ShaderEffect getEffect() {
		return effect;
	}

	public void setEffect(ShaderEffect effect) {
		this.effect = effect;
	}

}
