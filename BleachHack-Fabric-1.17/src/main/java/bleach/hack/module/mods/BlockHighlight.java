package bleach.hack.module.mods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonSyntaxException;

import bleach.hack.event.events.EventRenderBlockOutline;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingColor;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.shader.OutlineShaderManager;
import bleach.hack.util.shader.OutlineVertexConsumers;
import bleach.hack.util.shader.ShaderEffectLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BlockHighlight extends Module {
	
	private int lastWidth = -1;
	private int lastHeight = -1;
	private double lastShaderWidth;
	private boolean shaderUnloaded = true;

	public BlockHighlight() {
		super("BlockHighlight", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights blocks that you're looking at.",
				new SettingMode("Render", "Shader", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
				new SettingSlider("Shader", 0, 6, 2, 0).withDesc("The thickness of the shader outline."),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The width/thickness of the box lines."),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
				new SettingColor("Color", 0.0f, 0.5f, 0.5f, false).withDesc("The color of the highlight."));
	}

	@BleachSubscribe
	public void onRenderBlockOutline(EventRenderBlockOutline event) {
		event.setCancelled(true);
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		int mode = getSetting(0).asMode().mode;

		// Shader boilerplate
		if (mode == 0) {
			if (mc.getWindow().getFramebufferWidth() != lastWidth || mc.getWindow().getFramebufferHeight() != lastHeight
					|| lastShaderWidth != getSetting(1).asSlider().getValue() || shaderUnloaded) {
				try {
					ShaderEffect shader = ShaderEffectLoader.load(mc.getFramebuffer(), "blockhighlight-shader",
							String.format(
									Locale.ENGLISH,
									IOUtils.toString(getClass().getResource("/assets/bleachhack/shaders/mc_outline.ujson"), StandardCharsets.UTF_8), 
									getSetting(1).asSlider().getValue() / 2,
									getSetting(1).asSlider().getValue() / 4));

					shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
					lastWidth = mc.getWindow().getFramebufferWidth();
					lastHeight = mc.getWindow().getFramebufferHeight();
					lastShaderWidth = getSetting(1).asSlider().getValue();
					shaderUnloaded = false;

					OutlineShaderManager.loadShader(shader);
				} catch (JsonSyntaxException | IOException e) {
					e.printStackTrace();
				}
			}
		} else if (!shaderUnloaded) {
			OutlineShaderManager.loadDefaultShader();
			shaderUnloaded = true;
		}
		
		if (!(mc.crosshairTarget instanceof BlockHitResult))
			return;

		float[] rgb = this.getSetting(4).asColor().getRGBFloat();

		BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
		BlockState state = mc.world.getBlockState(pos);

		if (state.getMaterial() == Material.AIR || !mc.world.getWorldBorder().contains(pos))
			return;

		if (mode == 0) {
			MatrixStack matrices = Renderer.matrixFrom(pos.getX(), pos.getY(), pos.getZ());

			BlockEntity be = mc.world.getBlockEntity(pos);
			if (be != null) {
				BlockEntityRenderer<BlockEntity> beRenderer = mc.getBlockEntityRenderDispatcher().get(be);

				if (beRenderer != null) {
					beRenderer.render(
							be, mc.getTickDelta(), matrices, OutlineVertexConsumers.outlineOnlyProvider(rgb[0], rgb[1], rgb[2], 1f), 0xf000f0, OverlayTexture.DEFAULT_UV);
				}
			} else {
				mc.getBlockRenderManager().getModelRenderer().render(
						mc.world, mc.getBlockRenderManager().getModel(state), state, BlockPos.ORIGIN, matrices, OutlineVertexConsumers.outlineOnlyConsumer(rgb[0], rgb[1], rgb[2], 1f), false, new Random(), 0L, OverlayTexture.DEFAULT_UV);
			}
		} else {
			Box box = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
			if (mode == 1 || mode == 2) {
				float outlineWidth = getSetting(2).asSlider().getValueFloat();
				Renderer.drawBoxOutline(box, QuadColor.single(rgb[0], rgb[1], rgb[2], 1f), outlineWidth);
			}

			if (mode == 1 || mode == 3) {
				float fillAlpha = getSetting(3).asSlider().getValueFloat();
				Renderer.drawBoxFill(box, QuadColor.single(rgb[0], rgb[1], rgb[2], fillAlpha));
			}
		}
	}
}