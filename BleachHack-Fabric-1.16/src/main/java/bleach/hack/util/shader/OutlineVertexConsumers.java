package bleach.hack.util.shader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;

// i have not idea how or why any of this works
public class OutlineVertexConsumers {

	private static final Identifier nonExistentId = new Identifier("bleachhack", "ea.p48s3.4vg/xgx");

	public static VertexConsumer outlineOnlyConsumer(float r, float g, float b, float a) {
		OutlineVertexConsumerProvider vertexProvider = MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers();
		vertexProvider.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));

		return vertexProvider.getBuffer(RenderLayer.getOutline(nonExistentId));
	}

	public static VertexConsumerProvider outlineOnlyProvider(float r, float g, float b, float a) {
		OutlineVertexConsumerProvider vertexProvider = MinecraftClient.getInstance().getBufferBuilders().getOutlineVertexConsumers();
		vertexProvider.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));

		return new Override(vertexProvider);
	}

	static class Override implements VertexConsumerProvider {

		private OutlineVertexConsumerProvider parentProvider;

		public Override(OutlineVertexConsumerProvider parent) {
			this.parentProvider = parent;
		}

		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			return parentProvider.getBuffer(RenderLayer.getOutline(nonExistentId));
		}
	}
}
