package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;

/**
 * BufferBuilder patch that allows the color to be temporarily fixed even when the VertexFormatElement isn't COLOR
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder extends FixedColorVertexConsumer implements BufferVertexConsumer {

	@Redirect(method = { "color", "vertex" }, at = @At(value = "FIELD", target = "*:Z", ordinal = 0, remap = false))
	private boolean redirect_colorFixed(BufferBuilder self) {
		return false;
	}

	@ModifyVariable(method = "color", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public int color_modifyColor1(int red) {
		return colorFixed && fixedRed != -1 ? fixedRed : red;
	}

	@ModifyVariable(method = "color", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public int color_modifyColor2(int green) {
		return colorFixed && fixedGreen != -1 ? fixedGreen : green;
	}

	@ModifyVariable(method = "color", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	public int color_modifyColor3(int blue) {
		return colorFixed && fixedBlue != -1 ? fixedBlue : blue;
	}

	@ModifyVariable(method = "color", at = @At("HEAD"), ordinal = 3, argsOnly = true)
	public int color_modifyColor4(int alpha) {
		return colorFixed && fixedAlpha != -1 ? fixedAlpha : alpha;
	}

	@ModifyVariable(method = "vertex", at = @At("HEAD"), ordinal = 3, argsOnly = true)
	public float vertex_modifyColor1(float red) {
		return colorFixed && fixedRed != -1 ? fixedRed / 255f : red;
	}

	@ModifyVariable(method = "vertex", at = @At("HEAD"), ordinal = 4, argsOnly = true)
	public float vertex_modifyColor2(float green) {
		return colorFixed && fixedGreen != -1 ? fixedGreen / 255f : green;
	}

	@ModifyVariable(method = "vertex", at = @At("HEAD"), ordinal = 5, argsOnly = true)
	public float vertex_modifyColor3(float blue) {
		return colorFixed && fixedBlue != -1 ? fixedBlue / 255f : blue;
	}

	@ModifyVariable(method = "vertex", at = @At("HEAD"), ordinal = 6, argsOnly = true)
	public float vertex_modifyColor4(float alpha) {
		return colorFixed && fixedAlpha != -1 ? fixedAlpha / 255f : alpha;
	}
}
