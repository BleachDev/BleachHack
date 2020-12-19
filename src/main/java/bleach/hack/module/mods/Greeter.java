package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Greeter extends Module {

    private final Random rand = new Random();
    private List<String> lines = new ArrayList<>();
    private int lineCount = 0;
    public List<String> message_queue = new ArrayList<>();
    public String player;
    public String message;


    public Greeter() {
        super("Greeter", KEY_UNBOUND, Category.CHAT, "auto welcomer bruh (edit in greeter.txt)",
                new SettingMode("Read", "Order", "Random"),
                new SettingSlider("Delay", 1, 20, 3, 0).withDesc("Second delay between messages to avoid spam kicks")
        );
    }

    @Subscribe
    public void onTick(EventTick event)
    {
        if (mc.player.age % (this.getSettings().get(1).asSlider().getValue()*20) == 0 && this.isToggled())
        {
            if(message_queue.size() > 0) {
                message = message_queue.get(0);
                mc.player.sendChatMessage(message);
                message_queue.remove(0);
            }
        }
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
            message_queue.add(lines.get(lineCount).replace("$p", player));
        } else if (getSetting(0).asMode().mode == 1) {
            message_queue.add(lines.get(rand.nextInt(lines.size())).replace("$p", player));
        }
        if (lineCount >= lines.size() - 1) lineCount = 0;
        else lineCount++;
        player = null;
    }
}
