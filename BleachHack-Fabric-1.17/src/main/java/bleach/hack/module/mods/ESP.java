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
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import bleach.hack.eventbus.BleachSubscribe;
import com.google.gson.JsonSyntaxException;
import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingColor;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.QuadColor;
import bleach.hack.util.shader.OutlineShaderManager;
import bleach.hack.util.shader.ShaderEffectLoader;
import bleach.hack.util.world.EntityUtils;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public class ESP extends Module {

	private int lastWidth = -1;
	private int lastHeight = -1;
	private double lastShaderWidth;
	private boolean shaderUnloaded = true;

	public ESP() {
		super("ESP", KEY_UNBOUND, ModuleCategory.RENDER, "Allows you to see entities though walls.",
				new SettingMode("Render", "Shader", "Box+Fill", "Box", "Fill").withDesc("The ESP mode."),
				new SettingSlider("Shader", 0, 6, 2, 0).withDesc("The thickness of the shader outline in shader mode."),
				new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines."),
				new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
				new SettingToggle("DrawBehind", false).withDesc("Draws the box/fill behind the entity (definitely not a bug turned into a feature)."),

				new SettingToggle("Players", true).withDesc("Highlights Players.").withChildren(
						new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Outline color for players."),
						new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Outline color for friends.")),

				new SettingToggle("Mobs", false).withDesc("Highlights Mobs.").withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Outline color for mobs.")),

				new SettingToggle("Animals", false).withDesc("Highlights Animals").withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Outline color for animals.")),

				new SettingToggle("Items", true).withDesc("Highlights Items.").withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Outline color for items.")),

				new SettingToggle("Crystals", true).withDesc("Highlights End Crystals.").withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Outline color for crystals.")),

				new SettingToggle("Vehicles", false).withDesc("Highlights Vehicles.").withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Outline color for vehicles (minecarts/boats).")),

				new SettingToggle("Donkeys", false).withDesc("Highlights Donkeys and Llamas for duping.").withChildren(
						new SettingColor("Color", 0f, 0f, 1f, false).withDesc("Outline color for donkeys.")));
	}

	@BleachSubscribe
	public void onEntityRenderPre(EventEntityRender.PreAll event) {
		if (getSetting(0).asMode().mode <= 1) {
			if (mc.getWindow().getFramebufferWidth() != lastWidth || mc.getWindow().getFramebufferHeight() != lastHeight
					|| lastShaderWidth != getSetting(1).asSlider().getValue() || shaderUnloaded) {
				try {
					ShaderEffect shader = ShaderEffectLoader.load(mc.getFramebuffer(), "esp-shader",
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
	public void onWorldRenderPost(EventWorldRender.Post event) {
		if (!getSetting(4).asToggle().state) {
			for (Entity e: mc.world.getEntities()) {
				if (e == mc.player || e == mc.player.getVehicle()) {
					continue;
				}

				float[] color = getColorForEntity(e);
				if (color != null) {

					if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 3) {
						Renderer.drawBoxFill(e.getBoundingBox(), QuadColor.single(color[0], color[1], color[2], getSetting(3).asSlider().getValueFloat()));
					}

					if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 2) {
						Renderer.drawBoxOutline(e.getBoundingBox(), QuadColor.single(color[0], color[1], color[2], 1f), getSetting(2).asSlider().getValueFloat());
					}
				}
			}
		}
	}

	@BleachSubscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		float[] color = getColorForEntity(event.getEntity());

		if (color != null) {
			if (getSetting(4).asToggle().state) {
				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 2) {
					Renderer.drawBoxOutline(event.getEntity().getBoundingBox(), QuadColor.single(color[0], color[1], color[2], 1f),getSetting(2).asSlider().getValueFloat());
				}

				if (getSetting(0).asMode().mode == 1 || getSetting(0).asMode().mode == 3) {
					Renderer.drawBoxFill(event.getEntity().getBoundingBox(), QuadColor.single(color[0], color[1], color[2], getSetting(3).asSlider().getValueFloat()));
				}
			}

			if (getSetting(0).asMode().mode == 0) {
				event.setVertex(getOutline(mc.getBufferBuilders(), color[0], color[1], color[2]));
			}
		}
	}

	private float[] getColorForEntity(Entity entity) {
		if (EntityUtils.isPlayer(entity) && entity != mc.player && getSetting(5).asToggle().state) {
			return getSetting(5).asToggle().getChild(BleachHack.friendMang.has(entity) ? 1 : 0).asColor().getRGBFloat();
		} else if (EntityUtils.isMob(entity) && getSetting(6).asToggle().state) {
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