package bleach.hack.mixinterface;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;

public interface IMixinWorldRenderer {

	public Framebuffer getOutlineFramebuffer();
	public void setOutlineFramebuffer(Framebuffer framebuffer);

	public ShaderEffect getOutlineShader();
	public void setOutlineShader(ShaderEffect shader);
}
