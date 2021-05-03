package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FernBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

	//LocalCapture will break with a mapping change (j -> red k -> blue etc...), but it's better than having to recalculate block colors twice.
	@Inject(method = "renderQuad(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFIIIII)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFF[IIZ)V"),
			locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void renderQuad(BlockRenderView world, BlockState state, BlockPos pos, VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad,
			float brightness0, float brightness1, float brightness2, float brightness3, int light0, int light1, int light2, int light3, int overlay,
			CallbackInfo ci, float j, float k, float l) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");
		if (xray.isEnabled() && xray.getSetting(1).asToggle().state && !xray.isVisible(state.getBlock())) {
			if (xray.getSetting(1).asToggle().getChild(1).asToggle().state
					&& (state.getBlock() instanceof FernBlock
							|| state.getBlock().getClass() == TallPlantBlock.class
							|| WorldUtils.getTopBlockIgnoreLeaves(pos.getX(), pos.getZ()) == pos.getY())) {
				ci.cancel();
				return;
			}

			quadAdjustableAlpha(vertexConsumer, matrixEntry, quad, new float[] { brightness0, brightness1, brightness2, brightness3 }, j, k, l, xray.getSetting(1).asToggle().getChild(0).asSlider().getValueFloat() / 255F, new int[] { light0, light1, light2, light3 }, overlay, true);
			ci.cancel();
		}
	}

	//Thrown here because you can't reference imixins directly and you cannot do anything but overwrite methods in interfaces
	public void quadAdjustableAlpha(VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean useQuadColorData) {
		float[] fs = new float[]{brightnesses[0], brightnesses[1], brightnesses[2], brightnesses[3]};
		int[] is = new int[]{lights[0], lights[1], lights[2], lights[3]};
		int[] js = quad.getVertexData();
		Vec3i vec3i = quad.getFace().getVector();
		Vec3f vec3f = new Vec3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
		Matrix4f matrix4f = matrixEntry.getModel();
		vec3f.transform(matrixEntry.getNormal());
		int j = js.length / 8;
		MemoryStack memoryStack = MemoryStack.stackPush();
		Throwable var19 = null;

		try {
			ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			for (int k = 0; k < j; ++k) {
				intBuffer.clear();
				intBuffer.put(js, k * 8, 8);
				float f = byteBuffer.getFloat(0);
				float g = byteBuffer.getFloat(4);
				float h = byteBuffer.getFloat(8);
				float r;
				float s;
				float t;
				float v;
				float w;
				if (useQuadColorData) {
					float l = (float) (byteBuffer.get(12) & 255) / 255.0F;
					v = (float) (byteBuffer.get(13) & 255) / 255.0F;
					w = (float) (byteBuffer.get(14) & 255) / 255.0F;
					r = l * fs[k] * red;
					s = v * fs[k] * green;
					t = w * fs[k] * blue;
				} else {
					r = fs[k] * red;
					s = fs[k] * green;
					t = fs[k] * blue;
				}

				int u = is[k];
				v = byteBuffer.getFloat(16);
				w = byteBuffer.getFloat(20);
				Vector4f vector4f = new Vector4f(f, g, h, 1.0F);
				vector4f.transform(matrix4f);
				vertexConsumer.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), r, s, t, alpha, v, w, overlay, u, vec3f.getX(), vec3f.getY(), vec3f.getZ());
			}
		} catch (Throwable var40) {
			var19 = var40;
			throw var40;
		} finally {
			if (memoryStack != null) {
				if (var19 != null) {
					try {
						memoryStack.close();
					} catch (Throwable var39) {
						var19.addSuppressed(var39);
					}
				} else {
					memoryStack.close();
				}
			}

		}

	}
}

