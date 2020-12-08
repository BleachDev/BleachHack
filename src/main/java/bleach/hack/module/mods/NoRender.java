package bleach.hack.module.mods;

import bleach.hack.event.events.EventParticle;
import bleach.hack.event.events.EventSignBlockEntityRender;
import bleach.hack.event.events.EventSoundPlay;
import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.particle.ParticleTypes;

public class NoRender extends Module {

    public NoRender() {
        super("NoRender", KEY_UNBOUND, Category.RENDER, "Blocks certain elements from rendering",
                new SettingToggle("Blindness", true).withDesc("Removes the blindness effect"), // 0
                new SettingToggle("Fire", true).withDesc("Removes the fire overlay"), // 1
                new SettingToggle("Hurtcam", true).withDesc("Removes shaking when you get hurt"), // 2
                new SettingToggle("Liquid", true).withDesc("Removes the underwater overlay when you are in water"), // 3
                new SettingToggle("Pumpkin", true).withDesc("Removes the pumpkin overlay"), // 4
                new SettingToggle("Signs", false).withDesc("Doesn't render signs"), /* .withChildren( // 5 new SettingMode("Mode", "Unrender", "Blank", "Custom").
                 * withDesc("How to render signs, use the \"customsign\" command to set sign text"
                 * )), */
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
                new SettingToggle("Snowball", false).withDesc("Disables rendering snowballs"),
                new SettingToggle("Falling Blocks", false).withDesc("Disables rendering falling blocks"),
                new SettingToggle("Armor Stands", false).withDesc("Disables rendering armor stands"));
    }

    @Subscribe
    public void signRender(EventSignBlockEntityRender event) {
        if (this.getSetting(5).asToggle().state) {
            event.setCancelled(true);
        }
    }


    @Subscribe
    public void onRenderSnowball(EventWorldRenderEntity event) {
        if (this.getSetting(14).asToggle().state && event.entity instanceof SnowballEntity) {
            event.entity.remove();
            event.setCancelled(true);
        }
        if (this.getSetting(15).asToggle().state && event.entity instanceof FallingBlockEntity) {
            event.entity.remove();
            event.setCancelled(true);
        }
        if (this.getSetting(16).asToggle().state && event.entity instanceof ArmorStandEntity) {
            event.entity.remove();
            event.setCancelled(true);
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