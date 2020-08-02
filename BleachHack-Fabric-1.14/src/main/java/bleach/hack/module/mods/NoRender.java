package bleach.hack.module.mods;

import bleach.hack.event.events.EventParticle;
import bleach.hack.event.events.EventSignBlockEntityRender;
import bleach.hack.event.events.EventSoundPlay;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.particle.ParticleTypes;

import com.google.common.eventbus.Subscribe;

public class NoRender extends Module {

	public NoRender() {
		super("NoRender", KEY_UNBOUND, Category.RENDER, "Blocks certain elements from rendering",
				new SettingToggle("Blindness", true).withDesc("Removes the blindness effect"), // 0
				new SettingToggle("Fire", true).withDesc("Removes the fire overlay"), // 1
				new SettingToggle("Hurtcam", true).withDesc("Removes shaking when you get hurt"), // 2
				new SettingToggle("Liquid", true).withDesc("Removes the underwater overlay when you are in water"), // 3
				new SettingToggle("Pumpkin", true).withDesc("Removes the pumpkin overlay"), // 4
				new SettingToggle("Signs", false).withDesc("Doesn't render signs"),/*.withChildren( // 5
						new SettingMode("Mode: ", "Unrender", "Blank", "Custom").withDesc("How to render signs, use the \"customsign\" command to set sign text")),*/
				new SettingToggle("Wobble", true).withDesc("Removes the nuasea effect"), // 6
				new SettingToggle("BossBar", false).withDesc("Removes bossbars"), // 7
				new SettingToggle("Totem", false).withDesc("Removes the totem animation").withChildren(
						new SettingToggle("Particles", true).withDesc("Removes the yellow-green particles when a totem is used"),
						new SettingToggle("Sound", false).withDesc("Removes the totem sound when a totem is used")),
				new SettingToggle("Shield-WIP", false).withDesc("Removes your sheild"), // 9
				new SettingToggle("EG Curse", true).withDesc("Removes the elder guardian curse"),
				new SettingToggle("Maps", false).withDesc("Blocks mapart (useful if you're streaming)"),
				new SettingToggle("Skylight", false).withDesc("Disables skylight updates to reduce skylight lag"));
	}

	@Subscribe
	public void signRender(EventSignBlockEntityRender event) {
		if (this.getSetting(5).asToggle().state) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onParticle(EventParticle.Normal event) {
		if (getSetting(10).asToggle().state && event.particle instanceof ElderGuardianAppearanceParticle) {
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
