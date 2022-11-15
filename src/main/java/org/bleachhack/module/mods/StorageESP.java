/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import com.google.gson.JsonSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;

import org.bleachhack.event.events.EventEntityRender;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.Boxes;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.shader.BleachCoreShaders;
import org.bleachhack.util.shader.ColorVertexConsumerProvider;
import org.bleachhack.util.shader.ShaderEffectWrapper;
import org.bleachhack.util.shader.ShaderLoader;
import org.bleachhack.util.world.WorldUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StorageESP extends Module {

	private ShaderEffectWrapper shader;
	private ColorVertexConsumerProvider colorVertexer;

	public StorageESP() {
		super("StorageESP", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights storage containers in the world.",
				new SettingMode("Render", "Shader", "Box").withDesc("The Render mode."),
				new SettingSlider("ShaderFill", 1, 255, 50, 0).withDesc("How opaque the fill on shader mode should be."),
				new SettingSlider("Box", 0, 5, 2, 1).withDesc("How thick the box outline should be."),
				new SettingSlider("BoxFill", 0, 255, 50, 0).withDesc("How opaque the fill on box mode should be."),

				new SettingToggle("Chests", true).withDesc("Highlights chests/barrels."),
				new SettingToggle("Enderchests", true).withDesc("Highlights enderchests."),
				new SettingToggle("Furnaces", true).withDesc("Highlights furnaces."),
				new SettingToggle("Dispensers", true).withDesc("Highlights dispensers/droppers."),
				new SettingToggle("Hoppers", true).withDesc("Highlights hoppers."),
				new SettingToggle("Shulkers", true).withDesc("Highlights shulkers."),
				new SettingToggle("Brewingstands", true).withDesc("Highlights brewing stands."),
				new SettingToggle("ChestCarts", true).withDesc("Highlights chests in minecarts."),
				new SettingToggle("FurnaceCarts", true).withDesc("Highlights furnaces in minecarts."),
				new SettingToggle("HopperCarts", true).withDesc("Highlights hoppers in minecarts."),
				new SettingToggle("Itemframes", true).withDesc("Highlights item frames."));
		
		try {
			shader = new ShaderEffectWrapper(
					ShaderLoader.loadEffect(mc.getFramebuffer(), new Identifier("bleachhack", "shaders/post/entity_outline.json")));

			colorVertexer = new ColorVertexConsumerProvider(shader.getFramebuffer("main"), BleachCoreShaders::getColorOverlayShader);
		} catch (JsonSyntaxException | IOException e) {
			throw new RuntimeException("Failed to initialize StorageESP Shader! loaded too early?", e);
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Pre event) {
		shader.prepare();
		shader.clearFramebuffer("main");
	}

	@BleachSubscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (getSetting(0).asMode().getMode() != 0)
			return;

		int[] color = getColorForEntity(event.getEntity());

		if (color != null) {
			event.setVertex(colorVertexer.createDualProvider(event.getVertex(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()));
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(0).asMode().getMode() == 0) {
			// Manually render blockentities because of culling
			for (BlockEntity be: WorldUtils.getBlockEntities()) {
				int[] color = getColorForBlock(be);

				if (color != null) {
					BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().get(be);
					MatrixStack matrices = Renderer.matrixFrom(be.getPos().getX(), be.getPos().getY(), be.getPos().getZ());
					if (renderer != null) {
						renderer.render(be, mc.getTickDelta(), matrices,
								colorVertexer.createSingleProvider(mc.getBufferBuilders().getEntityVertexConsumers(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()),
								LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
					} else {
						BlockState state = be.getCachedState();
						mc.getBlockRenderManager().getModelRenderer().renderFlat(mc.world,
								mc.getBlockRenderManager().getModel(state), state, be.getPos(), matrices,
								colorVertexer.createSingleProvider(mc.getBufferBuilders().getEntityVertexConsumers(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()).getBuffer(RenderLayers.getMovingBlockLayer(state)),
								false, Random.create(0L), 0L, OverlayTexture.DEFAULT_UV);
					}
				}
			}

			colorVertexer.draw();
			shader.render();
			shader.drawFramebufferToMain("main");
		} else {
			float width = getSetting(2).asSlider().getValueFloat();
			int fill = getSetting(3).asSlider().getValueInt();

			for (Entity e: mc.world.getEntities()) {
				int[] color = getColorForEntity(e);
				Box box = e.getBoundingBox();

				if (e instanceof ItemFrameEntity && ((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP) {
					Axis axis = e.getHorizontalFacing().getAxis();
					box = box.expand(axis == Axis.X ? 0 : 0.125, axis == Axis.Y ? 0 : 0.125, axis == Axis.Z ? 0 : 0.125);
				}

				if (color != null) {
					if (width != 0)
						Renderer.drawBoxOutline(box, QuadColor.single(color[0], color[1], color[2], 255), width);

					if (fill != 0)
						Renderer.drawBoxFill(box, QuadColor.single(color[0], color[1], color[2], fill));
				}
			}

			Set<BlockPos> skip = new HashSet<>();
			for (BlockEntity be: WorldUtils.getBlockEntities()) {
				if (skip.contains(be.getPos()))
					continue;

				int[] color = getColorForBlock(be);
				Box box = be.getCachedState().getOutlineShape(mc.world, be.getPos()).getBoundingBox().offset(be.getPos());

				Direction dir = getChestDirection(be);
				if (dir != null) {
					box = Boxes.stretch(box, dir, 0.94);
					skip.add(be.getPos().offset(dir));
				}

				if (color != null) {
					if (width != 0)
						Renderer.drawBoxOutline(box, QuadColor.single(color[0], color[1], color[2], 255), width);

					if (fill != 0)
						Renderer.drawBoxFill(box, QuadColor.single(color[0], color[1], color[2], fill));
				}
			}
		}
	}

	private int[] getColorForBlock(BlockEntity be) {
		if ((be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && getSetting(4).asToggle().getState()) {
			return new int[] { 255, 155, 75 };
		} else if (be instanceof EnderChestBlockEntity && getSetting(5).asToggle().getState()) {
			return new int[] { 255, 13, 255 };
		} else if (be instanceof AbstractFurnaceBlockEntity && getSetting(6).asToggle().getState()) {
			return new int[] { 128, 128, 128 };
		} else if (be instanceof DispenserBlockEntity && getSetting(7).asToggle().getState()) {
			return new int[] { 140, 140, 178 };
		} else if (be instanceof HopperBlockEntity && getSetting(8).asToggle().getState()) {
			return new int[] { 115, 115, 155 };
		} else if (be instanceof ShulkerBoxBlockEntity && getSetting(9).asToggle().getState()) {
			return new int[] { 128, 50, 255 };
		} else if (be instanceof BrewingStandBlockEntity && getSetting(10).asToggle().getState()) {
			return new int[] { 128, 100, 50 };
		}

		return null;
	}

	private int[] getColorForEntity(Entity e) {
		if (e instanceof ChestMinecartEntity && getSetting(11).asToggle().getState()) {
			return new int[] { 255, 165, 75 };
		} else if (e instanceof FurnaceMinecartEntity && getSetting(12).asToggle().getState()) {
			return new int[] { 128, 128, 128 };
		} else if (e instanceof HopperMinecartEntity && getSetting(13).asToggle().getState()) {
			return new int[] { 115, 115, 155 };
		} else if (e instanceof ItemFrameEntity && getSetting(14).asToggle().getState()) {
			if (((ItemFrameEntity) e).getHeldItemStack().isEmpty()) {
				return new int[] { 115, 25, 25 };
			} else if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP) {
				return new int[] { 25, 25, 128 };
			} else {
				return new int[] { 25, 115, 25 };
			}
		}

		return null;
	}

	/** returns the direction of the other chest if its linked, otherwise null **/
	private Direction getChestDirection(BlockEntity entity) {
		if (entity instanceof ChestBlockEntity && entity.getCachedState().get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
			return ChestBlock.getFacing(entity.getCachedState());
		}

		return null;
	}
}