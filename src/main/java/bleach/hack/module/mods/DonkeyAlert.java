package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DonkeyAlert extends Module {
    public List<String> mob_uuids = new ArrayList<>();
    public DonkeyAlert(){
        super("Donkey Alert", KEY_UNBOUND, Category.WORLD, "Stole this shit from aurora");

    }

    @Subscribe
    public void onLivingRender(EventEntityRender.Render event) {
        if(mc.world == null){
            return;
        }
        for (final Entity e : mc.world.getEntities()) {
            if (e instanceof AbstractDonkeyEntity && !mob_uuids.toString().contains(e.getUuidAsString())) {
                final AbstractDonkeyEntity abstractDonkeyEntity = (AbstractDonkeyEntity) e;
                final boolean impact_toggle_state = ModuleManager.getModule(UI.class).getSetting(23).asToggle().state;
                switch (e.getType().toString()) {
                    case "entity.minecraft.donkey":
                        BleachLogger.infoMessage("Found Donkey! X: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getX() + (impact_toggle_state ? "\u00A79" : "") + " Z: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getZ());
                        break;
                    case "entity.minecraft.llama":
                        BleachLogger.infoMessage("Found Llama! X: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getX() + (impact_toggle_state ? "\u00A79" : "") + " Z: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getZ());
                        break;
                    case "entity.minecraft.mule":
                        BleachLogger.infoMessage("Found Mule! X: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getX() + (impact_toggle_state ? "\u00A79" : "") + " Z: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getZ());
                        break;
                }
                mob_uuids.add(e.getUuidAsString());
            }
        }
    }
    @Override
    public void onDisable() {
        super.onDisable();
        mob_uuids.clear();
    }
}
