package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import java.util.*;

public class StrengthESP extends Module {
    public StrengthESP() {
        super("StrengthESP", KEY_UNBOUND, Category.RENDER, "Renders red box around people with strength");
    }

    @Subscribe
    public void onWorldEntityRender(EventWorldRenderEntity event) {
        if (event.entity instanceof PlayerEntity) {
            assert event.entity != null;
            if (((PlayerEntity) event.entity).hasStatusEffect(StatusEffects.STRENGTH)) {
                RenderUtils.drawFilledBox(event.entity.getBoundingBox(), 1.0F, 0.0F, 0.0F, 1F);
            }
        }
    }
}