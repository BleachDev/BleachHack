/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonSyntaxException;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.WorldRenderUtils;
import bleach.hack.util.shader.StaticShaders;
import bleach.hack.util.shader.StringShaderEffect;
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
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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

	private int lastWidth;
	private int lastHeight;
	private double lastShaderWidth;
	private int lastShaderMode;
	private boolean shaderUnloaded = true;

	public StorageESP() {
		super("StorageESP", KEY_UNBOUND, Category.RENDER, "Draws a box around storage containers.",
				new SettingMode("Render", "Outline", "Shader", "Box+Fill", "Box", "Fill"),
				new SettingSlider("Shader", 0, 3, 1.5, 1).withDesc("The thickness of the shader outline"),
				new SettingSlider("Box", 0.1, 4, 2.5, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.75, 2).withDesc("The opacity of the fill"),

				new SettingToggle("Chests", true).withDesc("Highlights chests"),
				new SettingToggle("EnderChests", true).withDesc("Highlights enderchests"),
				new SettingToggle("Furnaces", true).withDesc("Highlights furnaces"),
				new SettingToggle("Dispensers", true).withDesc("Highlights dispensers"),
				new SettingToggle("Hoppers", true).withDesc("Highlights hoppers"),
				new SettingToggle("Shulkers", true).withDesc("Highlights shulkers"),
				new SettingToggle("BrewingStands", true).withDesc("Highlights brewing stands"),
				new SettingToggle("ChestCarts", true).withDesc("Highlights chests in minecarts"),
				new SettingToggle("FurnaceCarts", true).withDesc("Highlights furnaces in minecarts"),
				new SettingToggle("HopperCarts", true).withDesc("Highlights hoppers in minecarts"),
				new SettingToggle("ItemFrames", true).withDesc("Highlights item frames"),
				new SettingToggle("ArmorStands", true).withDesc("Highlights armor stands"));
	}

	public void onDisable() {
		blockEntities.clear();
		entities.clear();
		blacklist.clear();
		
		for (Entity e: mc.world.getEntities()) {
			e.setGlowing(false);
		}

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		blockEntities.clear();
		entities.clear();
		blacklist.clear();

		for (BlockEntity be: new ArrayList<>(mc.world.blockEntities)) {
			if (blacklist.contains(be.getPos())) {
				continue;
			}

			float[] color = getColorForBlock(be);

			if (color != null) {
				blockEntities.put(be, color);
				
				if (be.getCachedState().getBlock() instanceof ChestBlock && be.getCachedState().get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
					blacklist.add(be.getPos().offset(ChestBlock.getFacing(be.getCachedState())));
				}
			}
		}
		
		for (Entity e: mc.world.getEntities()) {
			float[] color = getColorForEntity(e);

			if (color != null) {
				entities.put(e, color);
			}
		}

	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		if (getSetting(0).asMode().mode <= 1) {
			for (Entry<BlockEntity, float[]> e: blockEntities.entrySet()) {
				MatrixStack matrix = WorldRenderUtils.matrixFrom(e.getKey().getPos().getX(), e.getKey().getPos().getY(), e.getKey().getPos().getZ());

				Block block = e.getKey().getCachedState().getBlock();
				BlockState state = Blocks.DIRT.getDefaultState();

				if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
					matrix.scale(0.88f, 0.88f, 0.88f);
					matrix.translate(0.06, 0, 0.06);

					Direction dir = getChestDirection(e.getKey().getPos());
					if (dir != null) {
						matrix.scale(1f + Math.abs(dir.getOffsetX()) * 1.12f, 1f, 1f + Math.abs(dir.getOffsetZ()) * 1.12f);
						matrix.translate(
								Math.min(0, dir.getOffsetX() / 2d + dir.getOffsetX() * 0.03),
								0,
								Math.min(0, dir.getOffsetZ() / 2d + dir.getOffsetZ() * 0.03));
					}
				}

				float[] color = e.getValue();
				VertexConsumer vertex = getOutline(mc.getBufferBuilders(), color[0], color[1], color[2]).getBuffer(RenderLayers.getMovingBlockLayer(state));

				GL11.glDepthFunc(GL11.GL_NEVER);
				mc.getBlockRenderManager().getModelRenderer().render(
						mc.world,
						mc.getBlockRenderManager().getModel(state),
						state,
						new BlockPos(e.getKey().getPos().getX(), e.getKey().getPos().getY(), e.getKey().getPos().getZ()),
						matrix,
						vertex,
						false,
						new Random(),
						0,
						OverlayTexture.DEFAULT_UV);

				mc.getBufferBuilders().getEntityVertexConsumers().draw();
				GL11.glDepthFunc(GL11.GL_LEQUAL);
			}
		} else {
			for (Entry<BlockEntity, float[]> e: blockEntities.entrySet()) {
				Box box = new Box(e.getKey().getPos());

				Block block = e.getKey().getCachedState().getBlock();

				if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
					box = box.contract(0.06);
					box = box.offset(0, -0.06, 0);

					Direction dir = getChestDirection(e.getKey().getPos());
					if (dir != null) {
						box = box.expand(Math.abs(dir.getOffsetX()) / 2d, 0, Math.abs(dir.getOffsetZ()) / 2d);
						box = box.offset(dir.getOffsetX() / 2d, 0, dir.getOffsetZ() / 2d);
					}
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 4) {
					RenderUtils.drawFill(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], (float) getSetting(3).asSlider().getValue());
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 3) {
					RenderUtils.drawOutline(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], (float) getSetting(2).asSlider().getValue());
				}
			}
			
			for (Entry<Entity, float[]> e: entities.entrySet()) {
				Box box = e.getKey().getBoundingBox();
				
				if (e.getKey() instanceof ItemFrameEntity && ((ItemFrameEntity) e.getKey()).getHeldItemStack().getItem() == Items.FILLED_MAP) {
					int axis = box.maxX - box.minX < box.maxY - box.minY ? 0 : box.maxY - box.minY < box.maxZ - box.minZ ? 1 : 2;
					box = box.expand(axis == 0 ? 0 : 0.12, axis == 1 ? 0 : 0.12, axis == 2 ? 0 : 0.12);
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 4) {
					RenderUtils.drawFill(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], (float) getSetting(3).asSlider().getValue());
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 3) {
					RenderUtils.drawOutline(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], (float) getSetting(2).asSlider().getValue());
				}
			}
		}
	}

	@Subscribe
	public void onWorldRenderPre(EventWorldRender.Pre event) {
		if (getSetting(0).asMode().mode <= 1) {
			if (mc.getWindow().getFramebufferWidth() != lastWidth || mc.getWindow().getFramebufferHeight() != lastHeight
					|| lastShaderWidth != getSetting(1).asSlider().getValue() || lastShaderMode != getSetting(0).asMode().mode) {
				try {
					StringShaderEffect shader = new StringShaderEffect(mc.getFramebuffer(), mc.getResourceManager(), mc.getTextureManager(),
							getSetting(0).asMode().mode == 0 ? StaticShaders.OUTLINE_SHADER : StaticShaders.MC_SHADER_UNFOMATTED
									.replace("%1", "" + getSetting(1).asSlider().getValue())
									.replace("%2", "" + (getSetting(1).asSlider().getValue() / 2)));

					shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
					lastWidth = mc.getWindow().getFramebufferWidth();
					lastHeight = mc.getWindow().getFramebufferHeight();
					lastShaderWidth = getSetting(1).asSlider().getValue();
					lastShaderMode = getSetting(0).asMode().mode;
					shaderUnloaded = false;

					shader.loadToWorldRenderer();
				} catch (JsonSyntaxException | IOException e) {
					e.printStackTrace();
				}
			}
		} else if (!shaderUnloaded) {
			mc.worldRenderer.loadEntityOutlineShader();
			shaderUnloaded = true;
		}
	}

	@Subscribe
	public void onWorldEntityRender(EventWorldRenderEntity event) {
		if (getSetting(0).asMode().mode <= 1 && entities.containsKey(event.entity)) {
			float[] color = entities.get(event.entity);
			event.vertex = getOutline(event.buffers, color[0], color[1], color[2]);
			event.entity.setGlowing(true);
		} else {
			event.entity.setGlowing(false);
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
		} else if (e instanceof ArmorStandEntity && getSetting(11).asToggle().state) {
			return new float[] { 0.7F, 0.6F, 0.2F };
		}

		return null;
	}

	/** returns the direction of the other chest if its linked, othwise null **/
	private Direction getChestDirection(BlockPos pos) {
		BlockState state = mc.world.getBlockState(pos);

		if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
			//System.out.println(state.get(ChestBlock.CHEST_TYPE));
			return ChestBlock.getFacing(state);
		}

		return null;
	}

	private VertexConsumerProvider getOutline(BufferBuilderStorage buffers, float r, float g, float b) {
		OutlineVertexConsumerProvider ovsp = buffers.getOutlineVertexConsumers();
		ovsp.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
		return ovsp;
	}
}
