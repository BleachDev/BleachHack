/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonSyntaxException;

import bleach.hack.event.events.EventBlockEntityRender;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.shader.OutlineShaderManager;
import bleach.hack.util.shader.StaticShaders;
import bleach.hack.util.shader.StringShaderEffect;
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
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
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
	private int lastShaderMode;
	private boolean shaderUnloaded = true;

	public StorageESP() {
		super("StorageESP", KEY_UNBOUND, Category.RENDER, "Draws a box around storage containers.",
				new SettingMode("Render", "Outline", "Shader", "Box+Fill", "Box", "Fill"),
				new SettingSlider("Shader", 0, 3, 1.5, 1).withDesc("The thickness of the shader outline"),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),

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

		for (Entity e: mc.world.getEntities()) {
			e.setGlowing(false);
		}

		super.onDisable();
	}

	@Subscribe
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

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		if (getSetting(0).asMode().mode >= 2) {
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

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 4) {
					RenderUtils.drawFill(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], (float) getSetting(3).asSlider().getValue());
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 3) {
					RenderUtils.drawOutline(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], 1f, (float) getSetting(2).asSlider().getValue());
				}
			}

			blacklist.clear();

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
					RenderUtils.drawOutline(box, e.getValue()[0], e.getValue()[1], e.getValue()[2], 1f, (float) getSetting(2).asSlider().getValue());
				}
			}
		}
	}

	@Subscribe
	public void onBlockEntityRenderPre(EventBlockEntityRender.PreAll event) throws JsonSyntaxException, IOException {
		if (getSetting(0).asMode().mode <= 1) {
			if (mc.getWindow().getFramebufferWidth() != lastWidth || mc.getWindow().getFramebufferHeight() != lastHeight
					|| lastShaderWidth != getSetting(1).asSlider().getValue() || lastShaderMode != getSetting(0).asMode().mode) {
				try {
					ShaderEffect shader = new StringShaderEffect(mc.getFramebuffer(), mc.getResourceManager(), mc.getTextureManager(),
							getSetting(0).asMode().mode == 0 ? StaticShaders.OUTLINE_SHADER : StaticShaders.MC_SHADER_UNFOMATTED
									.replace("%1", "" + getSetting(1).asSlider().getValue())
									.replace("%2", "" + (getSetting(1).asSlider().getValue() / 2)));

					shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
					lastWidth = mc.getWindow().getFramebufferWidth();
					lastHeight = mc.getWindow().getFramebufferHeight();
					lastShaderWidth = getSetting(1).asSlider().getValue();
					lastShaderMode = getSetting(0).asMode().mode;
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

	@Subscribe
	public void onBlockEntityRender(EventBlockEntityRender.Single.Pre event) {
		if (getSetting(0).asMode().mode <= 1 && blockEntities.containsKey(event.getBlockEntity())) {
			float[] color = blockEntities.get(event.getBlockEntity());
			event.setVertexConsumers(getOutline(mc.getBufferBuilders(), color[0], color[1], color[2]));
		}
	}

	@Subscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (getSetting(0).asMode().mode <= 1 && entities.containsKey(event.getEntity())) {
			float[] color = entities.get(event.getEntity());
			event.setVertex(getOutline(mc.getBufferBuilders(), color[0], color[1], color[2]));
			event.getEntity().setGlowing(true);
		} else {
			event.getEntity().setGlowing(false);
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

	/** returns the direction of the other chest if its linked, othwise null **/
	private Direction getChestDirection(BlockPos pos) {
		BlockState state = mc.world.getBlockState(pos);

		if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
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
