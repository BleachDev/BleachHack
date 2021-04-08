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

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonSyntaxException;
import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.shader.OutlineShaderManager;
import bleach.hack.util.shader.StaticShaders;
import bleach.hack.util.shader.StringShaderEffect;
import bleach.hack.util.world.EntityUtils;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public class ESP extends Module {

	private int lastWidth = -1;
	private int lastHeight = -1;
	private double lastShaderWidth;
	private int lastShaderMode;
	private boolean shaderUnloaded = true;

	public ESP() {
		super("ESP", KEY_UNBOUND, Category.RENDER, "Allows you to see entities though walls.",
				new SettingMode("Render", "Outline", "Shader", "Box+Fill", "Box", "Fill"),
				new SettingSlider("Shader", 0, 3, 1.5, 1).withDesc("The thickness of the shader outline"),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),
				new SettingToggle("DrawBehind", false).withDesc("Draws the box/fill behind the entity (definitely not a bug turned into a feature)"),

				new SettingToggle("Players", true).withDesc("Show Players").withChildren(
						new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Outline color for players"),
						new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Outline color for friends")),

				new SettingToggle("Mobs", false).withDesc("Show Mobs").withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Outline color for mobs")),

				new SettingToggle("Animals", false).withDesc("Show Animals").withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Outline color for animals")),

				new SettingToggle("Items", true).withDesc("Show Items").withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Outline color for items")),

				new SettingToggle("Crystals", true).withDesc("Show End Crystals").withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Outline color for crystals")),

				new SettingToggle("Vehicles", false).withDesc("Show Vehicles").withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Outline color for vehicles (minecarts/boats)")),

				new SettingToggle("Donkeys", false).withDesc("Show Donkeys and Llamas for duping").withChildren(
						new SettingColor("Color", 0f, 0f, 1f, false).withDesc("Outline color for donkeys")));
	}

	@Override
	public void onDisable() {
		for (Entity e : mc.world.getEntities()) {
			if (e != mc.player) {
				if (e.isGlowing()) {
					e.setGlowing(false);
				}
			}
		}

		super.onDisable();
	}

	@Subscribe
	public void onEntityRenderPre(EventEntityRender.PreAll event) {
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
	public void onWorldRenderPost(EventWorldRender.Post event) {
		if (!getSetting(4).asToggle().state) {
			for (Entity e: mc.world.getEntities()) {
				if (e == mc.player || e == mc.player.getVehicle()) {
					continue;
				}

				float[] color = getColorForEntity(e);
				if (color != null) {
					if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 4) {
						RenderUtils.drawFill(e.getBoundingBox(), color[0], color[1], color[2], (float) getSetting(3).asSlider().getValue());
					}

					if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 3) {
						RenderUtils.drawOutline(e.getBoundingBox(), color[0], color[1], color[2], 1f, (float) getSetting(2).asSlider().getValue());
					}
				}
			}
		}
	}

	@Subscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		float[] color = getColorForEntity(event.getEntity());

		if (color != null) {
			if (getSetting(4).asToggle().state) {
				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 3) {
					RenderUtils.drawOutline(event.getEntity().getBoundingBox(), color[0], color[1], color[2], 1f, (float) getSetting(2).asSlider().getValue());
				}

				if (getSetting(0).asMode().mode == 2 || getSetting(0).asMode().mode == 4) {
					RenderUtils.drawFill(event.getEntity().getBoundingBox(), color[0], color[1], color[2], (float) getSetting(3).asSlider().getValue());
				}
			}

			if (getSetting(0).asMode().mode <= 1) {
				event.setVertex(getOutline(mc.getBufferBuilders(), color[0], color[1], color[2]));
				event.getEntity().setGlowing(true);
			} else {
				event.getEntity().setGlowing(false);
			}
		}
	}

	private float[] getColorForEntity(Entity entity) {
		if (entity instanceof PlayerEntity && entity != mc.player && getSetting(5).asToggle().state) {
			return getSetting(5).asToggle().getChild(BleachHack.friendMang.has(entity.getName().getString()) ? 1 : 0).asColor().getRGBFloat();
		} else if (entity instanceof Monster && getSetting(6).asToggle().state) {
			return getSetting(6).asToggle().getChild(0).asColor().getRGBFloat();
		} // Before animals to prevent animals from overlapping donkeys
		else if (entity instanceof AbstractDonkeyEntity && getSetting(11).asToggle().state) {
			return getSetting(11).asToggle().getChild(0).asColor().getRGBFloat();
		} else if (EntityUtils.isAnimal(entity) && getSetting(7).asToggle().state) {
			return getSetting(7).asToggle().getChild(0).asColor().getRGBFloat();
		} else if (entity instanceof ItemEntity && getSetting(8).asToggle().state) {
			return getSetting(8).asToggle().getChild(0).asColor().getRGBFloat();
		} else if (entity instanceof EndCrystalEntity && getSetting(9).asToggle().state) {
			return getSetting(9).asToggle().getChild(0).asColor().getRGBFloat();
		} else if ((entity instanceof BoatEntity || entity instanceof AbstractMinecartEntity) && getSetting(10).asToggle().state) {
			return getSetting(10).asToggle().getChild(0).asColor().getRGBFloat();
		}

		return null;
	}

	private VertexConsumerProvider getOutline(BufferBuilderStorage buffers, float r, float g, float b) {
		OutlineVertexConsumerProvider ovsp = buffers.getOutlineVertexConsumers();
		ovsp.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
		return ovsp;
	}
}