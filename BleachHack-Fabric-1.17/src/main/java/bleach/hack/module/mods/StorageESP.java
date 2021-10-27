/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import java.util.Random;

import bleach.hack.eventbus.BleachSubscribe;
import com.google.gson.JsonSyntaxException;

import bleach.hack.event.events.EventBlockEntityRender;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.shader.OutlineShaderManager;
import bleach.hack.util.shader.OutlineVertexConsumers;
import bleach.hack.util.shader.ShaderEffectLoader;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class StorageESP extends Module {

	private Map<BlockEntity, float[]> blockEntities = new HashMap<>();
	private Map<Entity, float[]> entities = new HashMap<>();

	private Set<BlockPos> blacklist = new HashSet<>();

	private int lastWidth = -1;
	private int lastHeight = -1;
	private double lastShaderWidth;
	private boolean shaderUnloaded = true;

	public StorageESP() {
		super("StorageESP", KEY_UNBOUND, ModuleCategory.RENDER, "Draws a box around storage containers.",
				new SettingMode("Render", "Shader", "Box+Fill", "Box", "Fill").withDesc("The ESP mode."),
				new SettingSlider("Shader", 0, 6, 2, 0).withDesc("The thickness of the shader outline."),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines."),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),

				new SettingToggle("Chests", true).withDesc("Highlights chests."),
				new SettingToggle("EnderChests", true).withDesc("Highlights enderchests."),
				new SettingToggle("Furnaces", true).withDesc("Highlights furnaces."),
				new SettingToggle("Dispensers", true).withDesc("Highlights dispensers."),
				new SettingToggle("Hoppers", true).withDesc("Highlights hoppers."),
				new SettingToggle("Shulkers", true).withDesc("Highlights shulkers."),
				new SettingToggle("BrewingStands", true).withDesc("Highlights brewing stands."),
				new SettingToggle("ChestCarts", true).withDesc("Highlights chests in minecarts."),
				new SettingToggle("FurnaceCarts", true).withDesc("Highlights furnaces in minecarts."),
				new SettingToggle("HopperCarts", true).withDesc("Highlights hoppers in minecarts."),
				new SettingToggle("ItemFrames", true).withDesc("Highlights item frames."),
				new SettingToggle("ArmorStands", true).withDesc("Highlights armor stands."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		blockEntities.clear();
		entities.clear();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		blockEntities.clear();
		entities.clear();

		for (BlockEntity be: WorldUtils.getBlockEntities()) {
			float[] color = getColorForBlock(be);

			if (color != null) {
				blockEntities.put(be, color);
			}
		}

		for (Entity e: mc.world.getEntities()) {
			float[] color = getColorForEntity(e);

			if (color != null) {
				entities.put(e, color);
			}
		}

	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		if (getSetting(0).asMode().mode >= 1) {
			for (Entry<BlockEntity, float[]> e: blockEntities.entrySet()) {
				if (blacklist.contains(e.getKey().getPos())) {
					continue;
				}

				Box box = new Box(e.getKey().getPos());

				Block block = e.getKey().getCachedState().getBlock();

				if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
					box = box.contract(0.06);
					box = box.offset(0, -0.06, 0);

					Direction dir = getChestDirection(e.getKey().getPos());
					if (dir != null) {
						box = box.expand(Math.abs(dir.getOffsetX()) / 2d, 0, Math.abs(dir.getOffsetZ()) / 2d);
						box = box.offset(dir.getOffsetX() / 2d, 0, dir.getOffsetZ() / 2d);
						blacklist.add(e.getKey().getPos().offset(dir));
					}
				}

				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 3) {
					Renderer.drawBoxFill(box, QuadColor.single(e.getValue()[0], e.getValue()[1], e.getValue()[2], getSetting(3).asSlider().getValueFloat()));
				}

				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 2) {
					Renderer.drawBoxOutline(box, QuadColor.single(e.getValue()[0], e.getValue()[1], e.getValue()[2], 1f), getSetting(2).asSlider().getValueFloat());
				}
			}

			blacklist.clear();

			for (Entry<Entity, float[]> e: entities.entrySet()) {
				Box box = e.getKey().getBoundingBox();

				if (e.getKey() instanceof ItemFrameEntity && ((ItemFrameEntity) e.getKey()).getHeldItemStack().getItem() == Items.FILLED_MAP) {
					int axis = box.maxX - box.minX < box.maxY - box.minY ? 0 : box.maxY - box.minY < box.maxZ - box.minZ ? 1 : 2;
					box = box.expand(axis == 0 ? 0 : 0.12, axis == 1 ? 0 : 0.12, axis == 2 ? 0 : 0.12);
				}

				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 3) {
					Renderer.drawBoxFill(box, QuadColor.single(e.getValue()[0], e.getValue()[1], e.getValue()[2], getSetting(3).asSlider().getValueFloat()));
				}

				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 2) {
					Renderer.drawBoxOutline(box, QuadColor.single(e.getValue()[0], e.getValue()[1], e.getValue()[2], 1f), getSetting(2).asSlider().getValueFloat());
				}
			}
		}
	}

	@BleachSubscribe
	public void onBlockEntityRenderPre(EventBlockEntityRender.PreAll event) throws JsonSyntaxException, IOException {
		if (getSetting(0).asMode().mode == 0) {
			if (mc.getWindow().getFramebufferWidth() != lastWidth || mc.getWindow().getFramebufferHeight() != lastHeight
					|| lastShaderWidth != getSetting(1).asSlider().getValue() || shaderUnloaded) {
				try {
					ShaderEffect shader = ShaderEffectLoader.load(mc.getFramebuffer(), "storageesp-shader",
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
	}

	@BleachSubscribe
	public void onBlockEntityRender(EventBlockEntityRender.PreAll event) {
		if (getSetting(0).asMode().mode == 0) {
			for (Entry<BlockEntity, float[]> be: blockEntities.entrySet()) {
				BlockEntityRenderer<BlockEntity> beRenderer = mc.getBlockEntityRenderDispatcher().get(be.getKey());
	
				BlockPos pos = be.getKey().getPos();
				MatrixStack matrices = Renderer.matrixFrom(pos.getX(), pos.getY(), pos.getZ());
				if (beRenderer != null) {
					beRenderer.render(
							be.getKey(),
							mc.getTickDelta(),
							matrices,
							OutlineVertexConsumers.outlineOnlyProvider(be.getValue()[0], be.getValue()[1], be.getValue()[2], 1f),
							0xf000f0, OverlayTexture.DEFAULT_UV);
				} else {
					BlockState state = be.getKey().getCachedState();
	
					mc.getBlockRenderManager().getModelRenderer().render(
							mc.world,
							mc.getBlockRenderManager().getModel(state),
							state,
							BlockPos.ORIGIN,
							matrices,
							OutlineVertexConsumers.outlineOnlyConsumer(be.getValue()[0], be.getValue()[1], be.getValue()[2], 1f),
							false,
							new Random(),
							0L,
							OverlayTexture.DEFAULT_UV);
				}
			}
		}
	}

	@BleachSubscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (getSetting(0).asMode().mode == 0) {
			float[] color = entities.get(event.getEntity());
			
			if (color != null) {
				OutlineVertexConsumerProvider ovsp = mc.getBufferBuilders().getOutlineVertexConsumers();
				ovsp.setColor((int) (color[0] * 255), (int) (color[1] * 255), (int) (color[2] * 255), 255);

				event.setVertex(ovsp);
			}
		}
	}

	private float[] getColorForBlock(BlockEntity be) {
		if ((be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && getSetting(4).asToggle().state) {
			return new float[] { 1F, 0.6F, 0.3F };
		} else if (be instanceof EnderChestBlockEntity && getSetting(5).asToggle().state) {
			return new float[] { 1F, 0.05F, 1F };
		} else if (be instanceof AbstractFurnaceBlockEntity && getSetting(6).asToggle().state) {
			return new float[] { 0.5F, 0.5F, 0.5F };
		} else if (be instanceof DispenserBlockEntity && getSetting(7).asToggle().state) {
			return new float[] { 0.55F, 0.55F, 0.7F };
		} else if (be instanceof HopperBlockEntity && getSetting(8).asToggle().state) {
			return new float[] { 0.45F, 0.45F, 0.6F };
		} else if (be instanceof ShulkerBoxBlockEntity && getSetting(9).asToggle().state) {
			return new float[] { 0.5F, 0.2F, 1F };
		} else if (be instanceof BrewingStandBlockEntity && getSetting(10).asToggle().state) {
			return new float[] { 0.5F, 0.4F, 0.2F };
		}

		return null;
	}

	private float[] getColorForEntity(Entity e) {
		if (e instanceof ChestMinecartEntity && getSetting(11).asToggle().state) {
			return new float[] { 1F, 0.65F, 0.3F };
		} else if (e instanceof FurnaceMinecartEntity && getSetting(12).asToggle().state) {
			return new float[] { 0.5F, 0.5F, 0.5F };
		} else if (e instanceof HopperMinecartEntity && getSetting(13).asToggle().state) {
			return new float[] { 0.45F, 0.45F, 0.6F };
		} else if (e instanceof ItemFrameEntity && getSetting(14).asToggle().state) {
			if (((ItemFrameEntity) e).getHeldItemStack().isEmpty()) {
				return new float[] { 0.45F, 0.1F, 0.1F };
			} else if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP) {
				return new float[] { 0.1F, 0.1F, 0.5F };
			} else {
				return new float[] { 0.1F, 0.45F, 0.1F };
			}
		} else if (e instanceof ArmorStandEntity && getSetting(15).asToggle().state) {
			return new float[] { 0.7F, 0.6F, 0.2F };
		}

		return null;
	}

	/** returns the direction of the other chest if its linked, otherwise null **/
	private Direction getChestDirection(BlockPos pos) {
		BlockState state = mc.world.getBlockState(pos);

		if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
			return ChestBlock.getFacing(state);
		}

		return null;
	}
}
