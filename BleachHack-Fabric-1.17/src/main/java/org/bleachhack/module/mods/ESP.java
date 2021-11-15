/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.io.IOException;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventEntityRender;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingColor;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.shader.BleachCoreShaders;
import org.bleachhack.util.shader.ColorVertexConsumerProvider;
import org.bleachhack.util.shader.ShaderEffectWrapper;
import org.bleachhack.util.world.EntityUtils;

import com.google.gson.JsonSyntaxException;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;

public class ESP extends Module {

	private ShaderEffectWrapper shader;
	private ColorVertexConsumerProvider colorVertexer;

	public ESP() {
		super("ESP", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights Entities in the world.",
				new SettingMode("Render", "Shader", "Box").withDesc("The Render mode."),
				new SettingSlider("ShaderFill", 0, 255, 50, 0).withDesc("How opaque the fill on shader mode should be."),
				new SettingSlider("Box", 0, 5, 2, 1).withDesc("How thick the box outline should be."),
				new SettingSlider("BoxFill", 0, 255, 50, 0).withDesc("How opaque the fill on box mode should be."),

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
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Outline color for vehicles (minecarts/boats).")));
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
	public void onWorldRender(EventWorldRender.Pre event) {
		shader.prepare();
		shader.clearFramebuffer("main");
	}

	@BleachSubscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (getSetting(0).asMode().mode != 0)
			return;

		int[] color = getColor(event.getEntity());

		if (color != null) {
			event.setVertex(colorVertexer.createDualProvider(event.getVertex(), color[0], color[1], color[2], getSetting(1).asSlider().getValueInt()));
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (getSetting(0).asMode().mode == 0) {
			colorVertexer.draw();
			shader.render();
			shader.drawFramebufferToMain("main");
		} else {
			float width = getSetting(2).asSlider().getValueFloat();
			int fill = getSetting(3).asSlider().getValueInt();

			for (Entity e: mc.world.getEntities()) {
				int[] color = getColor(e);

				if (color != null) {
					if (width != 0)
						Renderer.drawBoxOutline(e.getBoundingBox(), QuadColor.single(color[0], color[1], color[2], 255), width);

					if (fill != 0)
						Renderer.drawBoxFill(e.getBoundingBox(), QuadColor.single(color[0], color[1], color[2], fill));
				}
			}
		}
	}

	private int[] getColor(Entity e) {
		if (e == mc.player)
			return null;

		if (e instanceof PlayerEntity && getSetting(4).asToggle().state) {
			return getSetting(4).asToggle().getChild(BleachHack.friendMang.has(e) ? 1 : 0).asColor().getRGBArray();
		} else if (e instanceof Monster && getSetting(5).asToggle().state) {
			return getSetting(5).asToggle().getChild(0).asColor().getRGBArray();
		} else if (EntityUtils.isAnimal(e) && getSetting(6).asToggle().state) {
			return getSetting(6).asToggle().getChild(0).asColor().getRGBArray();
		} else if (e instanceof ItemEntity && getSetting(7).asToggle().state) {
			return getSetting(7).asToggle().getChild(0).asColor().getRGBArray();
		} else if (e instanceof EndCrystalEntity && getSetting(8).asToggle().state) {
			return getSetting(8).asToggle().getChild(0).asColor().getRGBArray();
		} else if ((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSetting(9).asToggle().state) {
			return getSetting(9).asToggle().getChild(0).asColor().getRGBArray();
		}

		return null;
	}
}