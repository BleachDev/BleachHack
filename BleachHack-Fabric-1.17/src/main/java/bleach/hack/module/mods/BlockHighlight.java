package bleach.hack.module.mods;

import java.io.IOException;
import java.util.Random;

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
import bleach.hack.util.shader.BleachCoreShaders;
import bleach.hack.util.shader.ColorVertexConsumerProvider;
import bleach.hack.util.shader.ShaderEffectWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BlockHighlight extends Module {

	private ShaderEffectWrapper shader;
	private ColorVertexConsumerProvider colorVertexer;

	public BlockHighlight() {
		super("BlockHighlight", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights blocks that you're looking at.",
				new SettingMode("Render", "Shader", "Box").withDesc("The Render mode."),
				new SettingSlider("ShaderFill", 0, 255, 50, 0).withDesc("How opaque the fill on shader mode should be."),
				new SettingSlider("Box", 0, 5, 2, 1).withDesc("How thick the box outline should be."),
				new SettingSlider("BoxFill", 0, 255, 50, 0).withDesc("How opaque the fill on box mode should be."),
				new SettingColor("Color", 0.0f, 0.5f, 0.5f, false).withDesc("The color of the highlight."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		try {
			shader = new ShaderEffectWrapper(
					new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new Identifier("bleachhack", "shaders/post/entity_outline.json")));

			colorVertexer = new ColorVertexConsumerProvider(shader.getFramebuffer("main"), BleachCoreShaders::getColorOverlayShader);
		} catch (JsonSyntaxException | IOException e) {
			e.printStackTrace();
			setEnabled(false);
		}
	}

	@BleachSubscribe
	public void onRenderBlockOutline(EventRenderBlockOutline event) {
		event.setCancelled(true);
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		int mode = getSetting(0).asMode().mode;

		if (!(mc.crosshairTarget instanceof BlockHitResult))
			return;

		BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
		BlockState state = mc.world.getBlockState(pos);

		if (state.getMaterial() == Material.AIR || !mc.world.getWorldBorder().contains(pos))
			return;

		int[] color = this.getSetting(4).asColor().getRGBArray();
		if (mode == 0) {
			shader.prepare();
			shader.clearFramebuffer("main");

			Vec3d offset = state.getModelOffset(mc.world, pos);
			MatrixStack matrices = Renderer.matrixFrom(pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z);

			BlockEntity be = mc.world.getBlockEntity(pos);
			if (be != null) {
				mc.getBlockEntityRenderDispatcher().get(be).render(
						be, mc.getTickDelta(), matrices,
						colorVertexer.createSingleProvider(mc.getBufferBuilders().getEntityVertexConsumers(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()),
						LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
			} else {
				mc.getBlockRenderManager().getModelRenderer().renderFlat(
						mc.world, mc.getBlockRenderManager().getModel(state), state, pos, matrices,
						colorVertexer.createSingleProvider(mc.getBufferBuilders().getEntityVertexConsumers(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()).getBuffer(RenderLayers.getMovingBlockLayer(state)),
						false, new Random(), 0L, OverlayTexture.DEFAULT_UV);
			}

			colorVertexer.draw();
			shader.render();
			shader.drawFramebufferToMain("main");
		} else {
			Box box = state.getOutlineShape(mc.world, pos).getBoundingBox().offset(pos);
			float width = getSetting(2).asSlider().getValueFloat();
			float fill = getSetting(3).asSlider().getValueFloat();

			if (width != 0)
				Renderer.drawBoxOutline(box, QuadColor.single(color[0], color[1], color[2], 255), width);

			if (fill != 0)
				Renderer.drawBoxFill(box, QuadColor.single(color[0], color[1], color[2], fill));
		}
	}
}