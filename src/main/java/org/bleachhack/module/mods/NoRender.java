/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventBlockEntityRender;
import org.bleachhack.event.events.EventEntityRender;
import org.bleachhack.event.events.EventParticle;
import org.bleachhack.event.events.EventRenderOverlay;
import org.bleachhack.event.events.EventRenderScreenBackground;
import org.bleachhack.event.events.EventSoundPlay;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.gui.window.Window;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonElement;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FireworksSparkParticle.FireworkParticle;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.particle.ParticleTypes;

import net.minecraft.text.Text;

public class NoRender extends Module {

	public Text[] signText = new Text[] { Text.empty(), Text.empty(), Text.empty(), Text.empty() };

	public NoRender() {
		super("NoRender", KEY_UNBOUND, ModuleCategory.RENDER, "Blocks certain elements from rendering.",
				new SettingToggle("Overlays", true).withDesc("Removes certain overlays.").withChildren(                            // 0
						new SettingToggle("Blindness", true).withDesc("Removes the blindness effect."),                            // 0-0
						new SettingToggle("Fire", true).withDesc("Removes the fire overlay."),                                     // 0-1
						new SettingToggle("Hurtcam", true).withDesc("Removes shaking when you get hurt."),                         // 0-2
						new SettingToggle("Liquid", true).withDesc("Removes the underwater overlay when you're in water."),        // 0-3
						new SettingToggle("Pumpkin", true).withDesc("Removes the pumpkin overlay."),                               // 0-4
						new SettingToggle("Wobble", true).withDesc("Removes the nausea effect."),                                  // 0-5
						new SettingToggle("BossBar", false).withDesc("Removes bossbars."),                                         // 0-6
						new SettingToggle("Gui", false).withDesc("Makes the gui background more transparent.").withChildren(       // 0-7
								new SettingSlider("Opacity", 0, 1, 0, 2).withDesc("The opacity of the gui background.")),
						new SettingToggle("Frostbite", true).withDesc("Removes the frostbite overlay when you walk in powdered snow.")), // 0-8
				
				new SettingToggle("World", true).withDesc("Removes miscellaneous things in the world.").withChildren(              // 1
						new SettingToggle("Signs", false).withDesc("Doesn't render signs.").withChildren(                          // 1-0
								new SettingMode("Mode", "Unrender", "Blank", "Custom").withDesc("How to render signs, use the " + Command.getPrefix() + "customsign command to set sign text.")),
						new SettingToggle("Totem", false).withDesc("Removes the totem animation.").withChildren(                   // 1-1
								new SettingToggle("Particles", true).withDesc("Removes the yellow-green particles when a totem is used."),
								new SettingToggle("Sound", false).withDesc("Removes the totem sound when a totem is used.")),
						new SettingToggle("EG Curse", true).withDesc("Removes the elder guardian curse."),                         // 1-2
						new SettingToggle("Maps", false).withDesc("Blocks mapart (useful if you're streaming)."),                  // 1-3
						new SettingToggle("Skylight", false).withDesc("Disables skylight updates to reduce skylight lag.")),       // 1-4
				
				new SettingToggle("Particles", true).withDesc("Removes certain particles from the world.").withChildren(           // 2
						new SettingToggle("Campfires", true).withDesc("Removes campfire smoke particles."),                        // 2-0
						new SettingToggle("Explosions", false).withDesc("Removes explosion particles.").withChildren(              // 2-1
								new SettingSlider("Keep", 0, 100, 0, 0).withDesc("How much of the explosion particles to keep.")), // 2-2
						new SettingToggle("Fireworks", false).withDesc("Removes firework explosion particles.")),                  // 2-3
				
				new SettingToggle("Entities", true).withDesc("Removes certain entities from the world.").withChildren(             // 3
						new SettingToggle("Armor Stands", false).withDesc("Removes armor stands."),                                // 3-0
						new SettingToggle("Falling Blocks", false).withDesc("Removes falling blocks."),                            // 3-1
						new SettingToggle("Minecarts", false).withDesc("Removes minecarts."),                                      // 3-2
						new SettingToggle("Snowballs", false).withDesc("Removes snowballs."),                                      // 3-3
						new SettingToggle("Xp Orbs", false).withDesc("Removes experience orbs."),
						new SettingToggle("Items", false).withDesc("Removes items.")));

		JsonElement signText = BleachFileHelper.readMiscSetting("customSignText");

		if (signText != null) {
			for (int i = 0; i < Math.min(4, signText.getAsJsonArray().size()); i++) {
				this.signText[i] = Text.literal(signText.getAsJsonArray().get(i).getAsString());
			}
		}
	}
	
	public SettingToggle getOverlayChild(int overlayChild) {
		return getSetting(0).asToggle().getChild(overlayChild).asToggle();
	}
	
	public SettingToggle getWorldChild(int worldChild) {
		return getSetting(1).asToggle().getChild(worldChild).asToggle();
	}
	
	public SettingToggle getParticleChild(int particleChild) {
		return getSetting(2).asToggle().getChild(particleChild).asToggle();
	}
	
	public SettingToggle getEntityChild(int entityChild) {
		return getSetting(3).asToggle().getChild(entityChild).asToggle();
	}

	public boolean isOverlayToggled(int overlayChild) {
		return isEnabled() && getSetting(0).asToggle().getState() && getOverlayChild(overlayChild).getState();
	}

	public boolean isWorldToggled(int worldChild) {
		return isEnabled() && getSetting(1).asToggle().getState() && getWorldChild(worldChild).getState();
	}

	public boolean isParticleToggled(int particleChild) {
		return isEnabled() && getSetting(2).asToggle().getState() && getParticleChild(particleChild).getState();
	}

	public boolean isEntityToggled(int entityChild) {
		return isEnabled() && getSetting(3).asToggle().getState() && getEntityChild(entityChild).getState();
	}

	@BleachSubscribe
	public void onRenderOverlay(EventRenderOverlay event) {
		if (event.getTexture().getPath().equals("textures/misc/pumpkinblur.png") && isOverlayToggled(4)) {
			event.setCancelled(true);
		} else if (event.getTexture().getPath().equals("textures/misc/powder_snow_outline.png") && isOverlayToggled(8)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if ((isEntityToggled(0) && event.getEntity() instanceof ArmorStandEntity)
				|| (isEntityToggled(1) && event.getEntity() instanceof FallingBlockEntity)
				|| (isEntityToggled(2) && event.getEntity() instanceof AbstractMinecartEntity)
				|| (isEntityToggled(3) && event.getEntity() instanceof SnowballEntity)
				|| (isEntityToggled(4) && event.getEntity() instanceof ExperienceOrbEntity)
				|| (isEntityToggled(5) && event.getEntity() instanceof ItemEntity)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSignRender(EventBlockEntityRender.Single.Pre event) {
		if (event.getBlockEntity() instanceof SignBlockEntity && isWorldToggled(0)) {
			SettingToggle signSetting = getWorldChild(0);

			if (signSetting.getChild(0).asMode().getMode() == 0) {
				event.setCancelled(true);
			} else {
				SignBlockEntity sign = new SignBlockEntity(event.getBlockEntity().getPos(), event.getBlockEntity().getCachedState());
				sign.setWorld(mc.world);

				if (signSetting.getChild(0).asMode().getMode() == 2) {
					for (int i = 0; i < 4; i++) {
						sign.setTextOnRow(i, signText[i]);
					}
				}

				event.setBlockEntity(sign);
			}
		}
	}

	@BleachSubscribe
	public void onParticle(EventParticle.Normal event) {
		if ((isWorldToggled(2) && event.getParticle() instanceof ElderGuardianAppearanceParticle)
				|| (isParticleToggled(0) && event.getParticle() instanceof CampfireSmokeParticle)
				|| (isParticleToggled(1) && event.getParticle() instanceof ExplosionLargeParticle && Math.abs(event.getParticle().getBoundingBox().hashCode()) % 101 >= getParticleChild(1).getChild(0).asSlider().getValueInt())
				|| (isParticleToggled(2) && event.getParticle() instanceof FireworkParticle)) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onParticleEmitter(EventParticle.Emitter event) {
		if (isWorldToggled(1) && getWorldChild(1).getChild(0).asToggle().getState() && event.getEffect().getType() == ParticleTypes.TOTEM_OF_UNDYING) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		String path = event.getInstance().getId().getPath();
		if (isWorldToggled(1) && getWorldChild(1).getChild(1).asToggle().getState() && path.equals("item.totem.use")) {
			event.setCancelled(true);
		} else if (isWorldToggled(2) && path.equals("entity.elder_guardian.curse")) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onRenderGuiBackground(EventRenderScreenBackground event) {
		if (mc.world != null && isOverlayToggled(7)) {
			SettingToggle guiSetting = getOverlayChild(7);
			int opacity = (int) (guiSetting.getChild(0).asSlider().getValue() * 255);

			if (opacity != 0) {
				int opacity2 = (int) (opacity * 0.93);
				Window.verticalGradient(
						event.getMatrices(),
						0, 0,
						mc.currentScreen.width, mc.currentScreen.height,
						(opacity2 << 24) | 0x101010,
						(opacity  << 24) | 0x101010);
			}

			event.setCancelled(true);
		}
	}
}
