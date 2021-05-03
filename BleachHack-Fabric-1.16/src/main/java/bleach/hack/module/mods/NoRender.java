/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonElement;

import bleach.hack.event.events.EventBlockEntityRender;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventParticle;
import bleach.hack.event.events.EventRenderOverlay;
import bleach.hack.event.events.EventSoundPlay;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class NoRender extends Module {

	public Text[] signText = new Text[] { LiteralText.EMPTY, LiteralText.EMPTY, LiteralText.EMPTY, LiteralText.EMPTY };

	public NoRender() {
		super("NoRender", KEY_UNBOUND, Category.RENDER, "Blocks certain elements from rendering",
				new SettingToggle("Overlays", true).withDesc("Removes certain overlays").withChildren( // 0
						new SettingToggle("Blindness", true).withDesc("Removes the blindness effect"), // 0-0
						new SettingToggle("Fire", true).withDesc("Removes the fire overlay"), // 0-1
						new SettingToggle("Hurtcam", true).withDesc("Removes shaking when you get hurt"), // 0-2
						new SettingToggle("Liquid", true).withDesc("Removes the underwater overlay when you are in water"), // 0-3
						new SettingToggle("Pumpkin", true).withDesc("Removes the pumpkin overlay"), // 0-4
						new SettingToggle("Wobble", true).withDesc("Removes the nausea effect"), // 0-5
						new SettingToggle("BossBar", false).withDesc("Removes bossbars")), // 0-6
				new SettingToggle("World", true).withDesc("Removes certain things in the world").withChildren( // 1
						new SettingToggle("Signs", false).withDesc("Doesn't render signs").withChildren( // 1-0
								new SettingMode("Mode", "Unrender", "Blank", "Custom").withDesc("How to render signs, use the \"customsign\" command to set sign text")),
						new SettingToggle("Totem", false).withDesc("Removes the totem animation").withChildren( // 1-1
								new SettingToggle("Particles", true).withDesc("Removes the yellow-green particles when a totem is used"),
								new SettingToggle("Sound", false).withDesc("Removes the totem sound when a totem is used")),
						new SettingToggle("EG Curse", true).withDesc("Removes the elder guardian curse"), // 1-2
						new SettingToggle("Maps", false).withDesc("Blocks mapart (useful if you're streaming)"), // 1-3
						new SettingToggle("Skylight", false).withDesc("Disables skylight updates to reduce skylight lag"), // 1-4
						new SettingToggle("Explosions", false).withDesc("Removes explosion particles").withChildren( // 1-5
								new SettingSlider("Keep", 0, 100, 0, 0).withDesc("How much of the explosion particles to keep")),
						new SettingToggle("Armor Stands", false).withDesc("Disables rendering of armor stands"), // 1-6
						new SettingToggle("Falling Blocks", false).withDesc("Disables rendering of falling blocks"), // 1-7
						new SettingToggle("Campfire", false).withDesc("Disables rendering of campfire smoke particles"))); // 1-8

		JsonElement signText = BleachFileHelper.readMiscSetting("customSignText");

		if (signText != null) {
			for (int i = 0; i < Math.min(4, signText.getAsJsonArray().size()); i++) {
				this.signText[i] = new LiteralText(signText.getAsJsonArray().get(i).getAsString());
			}
		}
	}
	
	public boolean shouldRemoveOverlay(int overlayChild) {
		return isEnabled() && getSetting(0).asToggle().state && getSetting(0).asToggle().getChild(overlayChild).asToggle().state;
	}
	
	public boolean shouldRemoveWorld(int worldChild) {
		return isEnabled() && getSetting(1).asToggle().state && getSetting(1).asToggle().getChild(worldChild).asToggle().state;
	}
	
	public SettingToggle getWorldChild(int worldChild) {
		return getSetting(1).asToggle().getChild(worldChild).asToggle();
	}

	@Subscribe
	public void onRenderOverlay(EventRenderOverlay event) {
		if (event.getTexture().getPath().equals("textures/misc/pumpkinblur.png") && shouldRemoveOverlay(4)) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (shouldRemoveWorld(6) && event.getEntity() instanceof ArmorStandEntity) {
			event.setCancelled(true);
			return;
		}

		if (shouldRemoveWorld(7) && event.getEntity() instanceof FallingBlockEntity) {
			event.setCancelled(true);
			return;
		}
	}

	@Subscribe
	public void signRender(EventBlockEntityRender.Single.Pre event) {
		if (event.getBlockEntity() instanceof SignBlockEntity && shouldRemoveWorld(0)) {
			SettingToggle signSettings = getWorldChild(0);

			if (signSettings.state) {
				if (signSettings.getChild(0).asMode().mode == 0) {
					event.setCancelled(true);
				} else {
					SignBlockEntity sign = new SignBlockEntity();
					sign.setLocation(mc.world, event.getBlockEntity().getPos());

					if (signSettings.getChild(0).asMode().mode == 2) {
						for (int i = 0; i < 4; i++) {
							sign.setTextOnRow(i, signText[i]);
						}
					}

					event.setBlockEntity(sign);
				}
			}
		}
	}

	@Subscribe
	public void onParticle(EventParticle.Normal event) {
		if (shouldRemoveWorld(2) && event.particle instanceof ElderGuardianAppearanceParticle) {
			event.setCancelled(true);
		} else if (shouldRemoveWorld(5) && event.particle instanceof ExplosionLargeParticle) {
			if (Math.abs(event.particle.getBoundingBox().hashCode() % 101) >= getWorldChild(5).getChild(0).asSlider().getValueInt()) {
				event.setCancelled(true);
			}
		} else if (shouldRemoveWorld(8) && event.particle instanceof CampfireSmokeParticle) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onParticleEmitter(EventParticle.Emitter event) {
		if (shouldRemoveWorld(1) && getWorldChild(1).getChild(0).asToggle().state && event.effect.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		if (shouldRemoveOverlay(1) && getWorldChild(1).getChild(1).asToggle().state && event.instance.getId().getPath().equals("item.totem.use")) {
			event.setCancelled(true);
		} else if (shouldRemoveWorld(2) && event.instance.getId().getPath().equals("entity.elder_guardian.curse")) {
			event.setCancelled(true);
		}
	}
}
