package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;

import java.util.ArrayList;
import java.util.List;


public class DonkeyAlert extends Module {
    public List<String> mob_uuids = new ArrayList<>();
    public DonkeyAlert(){
        super("DonkeyAlert", KEY_UNBOUND, Category.CHAT, "Alerts you if a donkey appears in your render distance");

    }

    @Subscribe
    public void onLivingRender(EventEntityRender.Render event) {
        if(mc.world == null){
            return;
        }
        for (final Entity e : mc.world.getEntities()) {
            if (e instanceof AbstractDonkeyEntity && !mob_uuids.toString().contains(e.getUuidAsString())) {
                final boolean impact_toggle_state = ModuleManager.getModule(UI.class).getSetting(24).asToggle().state;
                final String coords = (impact_toggle_state ? "\u00A7f" : "") + (int) e.getX() + (impact_toggle_state ? "\u00A79" : "") + " Z: " + (impact_toggle_state ? "\u00A7f" : "") + (int) e.getZ();
                switch (e.getType().toString()) {
                    case "entity.minecraft.donkey":
                        BleachLogger.infoMessage("Found Donkey! X: " + coords);
                        break;
                    case "entity.minecraft.llama":
                        BleachLogger.infoMessage("Found Llama! X: " + coords);
                        break;
                    case "entity.minecraft.mule":
                        BleachLogger.infoMessage("Found Mule! X: " + coords);
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
