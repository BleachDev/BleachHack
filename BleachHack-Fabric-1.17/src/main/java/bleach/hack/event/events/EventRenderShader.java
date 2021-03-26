package bleach.hack.event.events;

import net.minecraft.client.gl.ShaderEffect;

public class EventRenderShader {
	
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
