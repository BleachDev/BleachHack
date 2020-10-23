package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.mods.CustomChat.CustomFont.CharMap;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.file.BleachFileHelper;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CustomChat extends Module {

    private static final List<CustomFont> fonts = Arrays.asList(
            new CustomFont(CharMap.range('!', 0xFF01, 95)),
            new CustomFont(CharMap.single('a', '\u1D00'), CharMap.single('b', '\u0299'), CharMap.range('c', 0x1d04, 2), CharMap.single('e', '\u1d07'),
                    CharMap.single('f', '\ua730'), CharMap.single('g', '\u0262'), CharMap.single('h', '\u029c'), CharMap.single('i', '\u026a'),
                    CharMap.range('j', 0x1D0a, 2), CharMap.single('l', '\u029f'), CharMap.single('m', '\u1d0d'), CharMap.single('n', '\u0274'),
                    CharMap.single('o', '\u1D0f'), CharMap.single('p', '\u1d29'), CharMap.single('r', '\u0280'), CharMap.single('s', '\ua731'),
                    CharMap.range('t', 0x1D1b, 2), CharMap.range('v', 0x1d20, 2), CharMap.single('z', '\u1d22'),
                    CharMap.single('A', '\u1D00'), CharMap.single('B', '\u0299'), CharMap.range('C', 0x1d04, 2), CharMap.single('E', '\u1d07'),
                    CharMap.single('F', '\ua730'), CharMap.single('G', '\u0262'), CharMap.single('H', '\u029c'), CharMap.single('I', '\u026a'),
                    CharMap.range('J', 0x1D0a, 2), CharMap.single('L', '\u029f'), CharMap.single('M', '\u1d0d'), CharMap.single('N', '\u0274'),
                    CharMap.single('O', '\u1D0f'), CharMap.single('P', '\u1d29'), CharMap.single('R', '\u0280'), CharMap.single('S', '\ua731'),
                    CharMap.range('T', 0x1D1b, 2), CharMap.range('V', 0x1d20, 2), CharMap.single('z', '\u1d22')),
            new CustomFont(CharMap.range('1', 0x2461, 9), CharMap.range('A', 0x24B6, 26), CharMap.range('a', 0x24D0, 26)),
            new CustomFont(
                    CharMap.single('a', '\u039b'), CharMap.single('c', '\u1455'), CharMap.single('e', '\u03A3'), CharMap.single('h', '\u0389'),
                    CharMap.single('l', '\u14aa'), CharMap.single('n', '\u041f'), CharMap.single('o', '\u04e8'), CharMap.single('r', '\u042f'),
                    CharMap.single('s', '\u01a7'), CharMap.single('t', '\u01ac'), CharMap.single('u', '\u0426'), CharMap.single('w', '\u0429'),
                    CharMap.single('A', '\u039b'), CharMap.single('C', '\u1455'), CharMap.single('E', '\u03A3'), CharMap.single('H', '\u0389'),
                    CharMap.single('L', '\u14aa'), CharMap.single('N', '\u041f'), CharMap.single('O', '\u04e8'), CharMap.single('R', '\u042f'),
                    CharMap.single('S', '\u01a7'), CharMap.single('T', '\u01ac'), CharMap.single('U', '\u0426'), CharMap.single('W', '\u0429')),
            new CustomFont(
                    CharMap.single('a', '\u03b1'), CharMap.single('b', '\u0432'), CharMap.single('d', '\u2202'), CharMap.single('e', '\u0454'),
                    CharMap.single('f', '\u0192'), CharMap.single('h', '\u043d'), CharMap.single('i', '\u03b9'), CharMap.single('j', '\u05e0'),
                    CharMap.single('k', '\u043a'), CharMap.single('l', '\u2113'), CharMap.single('m', '\u043c'), CharMap.single('n', '\u03b7'),
                    CharMap.single('o', '\u03c3'), CharMap.single('p', '\u03c1'), CharMap.single('r', '\u044f'), CharMap.single('s', '\u0455'),
                    CharMap.single('t', '\u0442'), CharMap.single('u', '\u03c5'), CharMap.single('v', '\u03bd'), CharMap.single('w', '\u03c9'),
                    CharMap.single('x', '\u03c7'), CharMap.single('y', '\u0443'),
                    CharMap.single('A', '\u03b1'), CharMap.single('B', '\u0432'), CharMap.single('D', '\u2202'), CharMap.single('E', '\u0454'),
                    CharMap.single('F', '\u0192'), CharMap.single('H', '\u043d'), CharMap.single('I', '\u03b9'), CharMap.single('J', '\u05e0'),
                    CharMap.single('K', '\u043a'), CharMap.single('L', '\u2113'), CharMap.single('M', '\u043c'), CharMap.single('N', '\u03b7'),
                    CharMap.single('O', '\u03c3'), CharMap.single('P', '\u03c1'), CharMap.single('R', '\u044f'), CharMap.single('S', '\u0455'),
                    CharMap.single('T', '\u0442'), CharMap.single('U', '\u03c5'), CharMap.single('V', '\u03bd'), CharMap.single('W', '\u03c9'),
                    CharMap.single('X', '\u03c7'), CharMap.single('Y', '\u0443')));

    public String prefix = "";
    public String suffix = " \u01c0 \u0299\u029f\u1d07\u1d00\u1d04\u029c\u029c\u1d00\u1d04\u1d0b \u005b\uff45\uff50\uff45\uff41\uff52\uff4c \uff45\uff44\uff49\uff54\uff49\uff4f\uff4e\u005d";

    public CustomChat() {
        super("CustomChat", KEY_UNBOUND, Category.CHAT, "Customizes your chat messages, use the \"customchat\" command to edit the stuff",
                new SettingToggle("CustomFont", true).withDesc("Adds a custom font in your messages"),
                new SettingMode("Font", "\uff41\uff42\uff43\uff44\uff45", "\u1D00\u0299\u1d04\u1d05\u1d07",
                        "\u24d0\u24d1\u24d2\u24d3\u24d4", "\u039bb\u1455d\u03A3", "\u03b1\u0432c\u2202\u0454").withDesc("Custom font to use"),
                new SettingToggle("Prefix", false).withDesc("Message prepended to the message, set with \"customchat prefix [message]\""),
                new SettingToggle("Suffix", false).withDesc("Message appended to the message, set with \"customchat suffix [message]\""),
                new SettingMode("KillText", "None", "Ez", "GG"));
    }

    public void init() {
        String pfx = BleachFileHelper.readMiscSetting("customChatPrefix");
        if (pfx != null) prefix = pfx;

        String sfx = BleachFileHelper.readMiscSetting("customChatSuffix");
        if (sfx != null) suffix = sfx;
    }

    @Subscribe
    public void onPacketSend(EventSendPacket event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            String text = ((ChatMessageC2SPacket) event.getPacket()).getChatMessage();

            if (text.startsWith("/")) return;

            if (getSetting(0).asToggle().state) {
                text = fonts.get(getSetting(1).asMode().mode).replace(text);
            }

            if (getSetting(2).asToggle().state) {
                text = prefix + text;
            }

            if (getSetting(3).asToggle().state) {
                text = text + suffix;
            }

            if (!text.equals(((ChatMessageC2SPacket) event.getPacket()).getChatMessage())) {
                FabricReflect.writeField(event.getPacket(), text, "field_12764", "chatMessage");
            }
        }
    }

    @Subscribe
    public void onPacketRead(EventReadPacket event) {
        if (getSetting(4).asMode().mode != 0 && event.getPacket() instanceof GameMessageS2CPacket) {

            String msg = ((GameMessageS2CPacket) event.getPacket()).getMessage().asString();
            if (msg.contains(mc.player.getName().asString()) && msg.contains("by")) {
                for (PlayerEntity e : mc.world.getPlayers()) {
                    if (e == mc.player) continue;

                    if (mc.player.distanceTo(e) < 12 && msg.contains(e.getName().asString())
                            && !msg.contains("<" + e.getName().asString() + ">") && !msg.contains("<" + mc.player.getName().asString() + ">")) {
                        if (getSetting(4).asMode().mode == 1) {
                            mc.player.sendChatMessage(e.getName().asString() + " Just got EZed using the power of BleachHack " + BleachHack.VERSION);
                        } else {
                            mc.player.sendChatMessage("GG, " + e.getName().asString() + ", but BleachHack " + BleachHack.VERSION + " is ontop!");
                        }
                    }
                }
            }
        }
    }

    static class CustomFont {

        private final HashMap<Character, Character> allMaps = new HashMap<>();

        public CustomFont(CharMap... maps) {
            for (CharMap map : maps) {
                allMaps.putAll(map.getMap());
            }
        }

        public String replace(String startString) {
            for (Entry<Character, Character> e : allMaps.entrySet()) {
                startString = startString.replace(e.getKey(), e.getValue());
            }

            return startString;
        }

        static class CharMap {

            private final HashMap<Character, Character> map = new HashMap<>();

            private CharMap(char... mappings) {
                for (int i = 0; i < mappings.length - 1; i += 2) {
                    map.put(mappings[i], mappings[i + 1]);
                }
            }

            public static CharMap single(char from, char to) {
                return new CharMap(from, to);
            }

            public static CharMap range(char start, int start1, int amount) {
                char[] chars = new char[amount * 2];

                for (int i = 0; i < amount; i++) {
                    chars[i * 2] = (char) (start + i);
                    chars[i * 2 + 1] = (char) (start1 + i);
                }

                return new CharMap(chars);
            }

            public HashMap<Character, Character> getMap() {
                return map;
            }
        }
    }
}
