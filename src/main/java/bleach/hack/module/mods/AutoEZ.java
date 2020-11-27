package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoEZ extends Module {

    private final Random rand = new Random();
    private List<String> lines = new ArrayList<>();
    private int lineCount = 0;
    public List<String> dead_uuids = new ArrayList<>();


    public AutoEZ() {
        super("AutoEZ", KEY_UNBOUND, Category.COMBAT, "auto ez bruh (edit in AutoEZ.txt)",
                new SettingMode("Read", "Random", "Order"),
                new SettingToggle("Ignore Friends", true)
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!BleachFileMang.fileExists("AutoEZ.txt")) {
            BleachFileMang.createFile("AutoEZ.txt");
            BleachFileMang.appendFile("You just got EZ'd by BleachHack epearl Edition, $p! Get good!", "AutoEZ.txt");

        }
        lines = BleachFileMang.readFileLines("AutoEZ.txt");
        lineCount = 0;
    }

    @Subscribe
    public void onEntityRender(EventEntityRender event) {
        if (lines.isEmpty()) return;
        if (event.getEntity().getType().toString().contains("entity.minecraft.player")){
                if (event.getEntity() == mc.player) return;
                if (
                    !event.getEntity().isAlive()
                    && event.getEntity().distanceTo(mc.player) <= 8
                    && !dead_uuids.toString().contains(event.getEntity().getUuidAsString())
                ) {
                    if (getSetting(0).asMode().mode == 0) {
                        if (getSetting(1).asToggle().state && BleachHack.friendMang.has(event.getEntity().getDisplayName().getString())) {return;}
                        mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())).replace("$p", event.getEntity().getDisplayName().getString()));
                    } else if (getSetting(0).asMode().mode == 1) {
                        if (getSetting(1).asToggle().state && BleachHack.friendMang.has(event.getEntity().getDisplayName().getString())) {return;}
                        mc.player.sendChatMessage(lines.get(lineCount).replace("$p", event.getEntity().getDisplayName().getString()));
                    }

                    if (lineCount >= lines.size() - 1) lineCount = 0;
                    else lineCount++;
                    dead_uuids.add(event.getEntity().getUuidAsString());


                } else if (
                    !event.getEntity().isAlive()
                    && event.getEntity().distanceTo(mc.player) > 8
                    && !dead_uuids.toString().contains(event.getEntity().getUuidAsString())
                ) {
                    dead_uuids.add(event.getEntity().getUuidAsString());

                }

        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.world != null;
        if (mc.player.age % 100 == 0) {
            dead_uuids.clear();
        }
    }
    @Override
    public void onDisable() {
        dead_uuids.clear();
        super.onDisable();
    }

}
