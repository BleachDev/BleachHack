package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Greeter extends Module {

    private final Random rand = new Random();
    private List<String> lines = new ArrayList<>();
    private int lineCount = 0;
    public List<String> dead_uuids = new ArrayList<>();
    public String player;


    public Greeter() {
        super("Greeter", KEY_UNBOUND, Category.CHAT, "auto welcomer bruh (edit in greeter.txt)",
                new SettingMode("Read", "Random", "Order")
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!BleachFileMang.fileExists("greeter.txt")) {
            BleachFileMang.createFile("greeter.txt");
            BleachFileMang.appendFile("Welcome back, $p", "greeter.txt");

        }
        lines = BleachFileMang.readFileLines("greeter.txt");
        lineCount = 0;
    }

    @Subscribe
    public void onPacketRead(EventReadPacket event) {
        if (event.getPacket() instanceof PlayerListS2CPacket && ((PlayerListS2CPacket) event.getPacket()).getAction().name().equals("ADD_PLAYER")) {
            player = ((PlayerListS2CPacket) event.getPacket()).getEntries().get(0).getProfile().getName();
        }
        if (lines.isEmpty()) return;
        if (player == null) return;
        if (mc.player == null) return;
        if (player.equals(mc.player.getDisplayName().asString())) return;
        if (getSetting(0).asMode().mode == 0) {
            mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())).replace("$p", player));
        } else if (getSetting(0).asMode().mode == 1) {
            mc.player.sendChatMessage(lines.get(lineCount).replace("$p", player));
        }
        if (lineCount >= lines.size() - 1) lineCount = 0;
        else lineCount++;
        player = null;
    }
}
