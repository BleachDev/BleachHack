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
				new SettingToggle("Blindness", true).withDesc("Removes the blindness effect"), // 0
				new SettingToggle("Fire", true).withDesc("Removes the fire overlay"), // 1
				new SettingToggle("Hurtcam", true).withDesc("Removes shaking when you get hurt"), // 2
				new SettingToggle("Liquid", true).withDesc("Removes the underwater overlay when you are in water"), // 3
				new SettingToggle("Pumpkin", true).withDesc("Removes the pumpkin overlay"), // 4
				new SettingToggle("Signs", false).withDesc("Doesn't render signs").withChildren(
						new SettingMode("Mode", "Unrender", "Blank", "Custom").withDesc("How to render signs, use the \"customsign\" command to set sign text")),
				new SettingToggle("Wobble", true).withDesc("Removes the nausea effect"), // 6
				new SettingToggle("BossBar", false).withDesc("Removes bossbars"), // 7
				new SettingToggle("Totem", false).withDesc("Removes the totem animation").withChildren(
						new SettingToggle("Particles", true).withDesc("Removes the yellow-green particles when a totem is used"),
						new SettingToggle("Sound", false).withDesc("Removes the totem sound when a totem is used")),
				new SettingToggle("Shield-WIP", false).withDesc("Removes your sheild"), // 9
				new SettingToggle("EG Curse", true).withDesc("Removes the elder guardian curse"),
				new SettingToggle("Maps", false).withDesc("Blocks mapart (useful if you're streaming)"),
				new SettingToggle("Skylight", false).withDesc("Disables skylight updates to reduce skylight lag"),
				new SettingToggle("Explosions", false).withDesc("Removes explosion particles").withChildren(
						new SettingSlider("Keep", 0, 100, 0, 0).withDesc("How much of the explosion particles to keep")),
				new SettingToggle("Armor Stands", false).withDesc("Disables rendering of armor stands"),
				new SettingToggle("Falling Blocks", false).withDesc("Disables rendering of falling blocks"),
				new SettingToggle("Campfire", false).withDesc("Disables rendering of campfire smoke particles"));

		JsonElement signText = BleachFileHelper.readMiscSetting("customSignText");

		if (signText != null) {
			for (int i = 0; i < Math.min(4, signText.getAsJsonArray().size()); i++) {
				this.signText[i] = new LiteralText(signText.getAsJsonArray().get(i).getAsString());
			}
		}
	}

	@Subscribe
	public void onRenderOverlay(EventRenderOverlay event) {
		if (event.getTexture().getPath().equals("textures/misc/pumpkinblur.png") && getSetting(4).asToggle().state) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onEntityRender(EventEntityRender.Single.Pre event) {
		if (getSetting(14).asToggle().state && event.getEntity() instanceof ArmorStandEntity) {
			event.setCancelled(true);
			return;
		}

		if (getSetting(15).asToggle().state && event.getEntity() instanceof FallingBlockEntity) {
			event.setCancelled(true);
			return;
		}
	}

	@Subscribe
	public void signRender(EventBlockEntityRender.Single.Pre event) {
		if (event.getBlockEntity() instanceof SignBlockEntity) {
			if (this.getSetting(5).asToggle().state) {
				if (getSetting(5).asToggle().getChild(0).asMode().mode == 0) {
					event.setCancelled(true);
				} else {
					SignBlockEntity sign = new SignBlockEntity();
					sign.setLocation(mc.world, event.getBlockEntity().getPos());

					if (getSetting(5).asToggle().getChild(0).asMode().mode == 2) {
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
		if (getSetting(10).asToggle().state && event.particle instanceof ElderGuardianAppearanceParticle) {
			event.setCancelled(true);
		} else if (getSetting(13).asToggle().state && event.particle instanceof ExplosionLargeParticle) {
			if (Math.abs(event.particle.getBoundingBox().hashCode() % 101) >= (int) getSetting(13).asToggle().getChild(0).asSlider().getValue()) {
				event.setCancelled(true);
			}
		} else if (getSetting(16).asToggle().state && event.particle instanceof CampfireSmokeParticle) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onParticleEmitter(EventParticle.Emitter event) {
		if (getSetting(8).asToggle().state && getSetting(8).asToggle().getChild(0).asToggle().state && event.effect.getType() == ParticleTypes.TOTEM_OF_UNDYING) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onSoundPlay(EventSoundPlay.Normal event) {
		if (getSetting(8).asToggle().state && getSetting(8).asToggle().getChild(1).asToggle().state && event.instance.getId().getPath().equals("item.totem.use")) {
			event.setCancelled(true);
		} else if (getSetting(10).asToggle().state && event.instance.getId().getPath().equals("entity.elder_guardian.curse")) {
			event.setCancelled(true);
		}
	}
}
