package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Random;

public class AutoDox extends Module {

    public AutoDox() {
        super("AutoDox", KEY_UNBOUND, Category.CHAT, "Awowa",
                new SettingSlider("Delay", 1, 600, 20, 0));
    }

    Random Fortnite = new Random();

    public String getRandomName() {
        List<AbstractClientPlayerEntity> Fortnite2 = mc.world.getPlayers();
        return Fortnite2.get(Fortnite.nextInt(Fortnite2.size())).getName().asString();
    }

    public String Fortnite1(String[] list) {
        return list[Fortnite.nextInt(list.length)];
    }


    @Subscribe
    public void onTick(EventTick event) {

        String Fortnite4 = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;

        if (mc.world == null || mc.player == null) {
            return;
        }

        String Fortnite3 = getRandomName();

        if (Fortnite3.equals(mc.player.getName())) {
            return;
        }

        for (final Entity e : mc.world.getEntities()) {
            if (e instanceof PlayerEntity && mc.player.age % (int) (getSetting(0).asSlider().getValue() * 20) == 0) {
                mc.player.sendChatMessage("/w " + Fortnite3 + " lol get fucked https://doxbin.org/" + Fortnite3 + "_" + Fortnite4 + "_ezed_By_Epearl_Hack_Dox_Team");
            }
        }
    }
}
