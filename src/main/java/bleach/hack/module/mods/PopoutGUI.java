package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.Event;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ClientChatListener;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.UIManager.*;



public class PopoutGUI extends Module {

    public PopoutGUI() {
        super("PopoutGUI", KEY_UNBOUND, Category.PLAYER, "",
                new SettingToggle("Suppress Chat", false)
                );
    }
    public void onEnable() {
        new b();
        super.onEnable();
    }
    private long prevTime = 0;
    private double tps = 20;
    private long lastPacket = 0;
    private final long time = System.currentTimeMillis();
    private String radar_player_list = "";

    public static JFrame popout_window;
    public static JFrame hud_popout_window;
    public static JTextArea module_list;
    public static JTextArea chat_window;
    public static JTextPane hud_input;
    public static JTextField text_input;
    public static JPanel main_panel;
    public static JPanel hud_panel;
    public static JSplitPane popout_window_splitter;
    public static JPanel module_panel;
    public static JPanel chat_panel;
    public static JScrollPane module_scroller;
    public static JScrollPane chat_scroller;

    public class b {

        public b() {
            c();
            popout_window = new JFrame("BleachHack epearl Edition | Chat and module HUD");
            popout_window.setPreferredSize(new Dimension(1000, 500));
            popout_window.setContentPane(main_panel);
            popout_window.pack();
            popout_window.setVisible(true);


            hud_popout_window = new JFrame("BleachHack epearl Edition | UI HUD");
            hud_panel = new JPanel(new BorderLayout());
            hud_popout_window.setContentPane(hud_panel);
            hud_input = new JTextPane();
            hud_panel.add(hud_input, BorderLayout.CENTER);
            hud_panel.setPreferredSize(new Dimension(400, 250));
            hud_input.setEditable(false);
            hud_popout_window.pack();
            hud_popout_window.setVisible(true);

            popout_window.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    PopoutGUI.super.toggle();
                }
            });

            hud_popout_window.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    PopoutGUI.super.toggle();
                }
            });

            text_input.addActionListener(p0 -> {
                mc.player.sendChatMessage(text_input.getText());
                text_input.setText("");
                return;
            });
        }

        public void c() {
            (main_panel = new JPanel()).setLayout(new BorderLayout(0, 0));
            main_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
            popout_window_splitter = new JSplitPane();
            main_panel.add(popout_window_splitter, "Center");
            (module_panel = new JPanel()).setLayout(new BorderLayout(0, 0));
            popout_window_splitter.setLeftComponent(module_panel);
            (module_scroller = new JScrollPane()).setHorizontalScrollBarPolicy(31);
            module_scroller.setVerticalScrollBarPolicy(20);
            module_panel.add(module_scroller, "Center");
            (module_list = new JTextArea()).setLineWrap(true);
            module_list.setEditable(false);
            module_list.setText("");
            module_scroller.setViewportView(module_list);
            (chat_panel = new JPanel()).setLayout(new BorderLayout(0, 0));
            popout_window_splitter.setRightComponent(chat_panel);
            text_input = new JTextField();
            chat_panel.add(text_input, "South");
            chat_window = new JTextArea();
            chat_window.setEditable(false);
            chat_panel.add(chat_window, "Center");
            (chat_scroller = new JScrollPane()).setHorizontalScrollBarPolicy(31);
            chat_scroller.setVerticalScrollBarPolicy(20);
            chat_panel.add(chat_scroller, "Center");
            chat_scroller.setViewportView(chat_window);
        }

        public JComponent d() {
            return main_panel;
        }


            private /* synthetic */ void a ( final ActionEvent actionEvent){
                mc.player.sendChatMessage(text_input.getText());
                text_input.setText("");
            }

    }

    @Subscribe
    public void onDrawOverlay(EventDrawOverlay event) {
        List<String> modules_lines = new ArrayList<>();
        for (Module m : ModuleManager.getModules())
            if (m.isToggled() && m.isDrawn() && !m.getName().equals("UI")) modules_lines.add(m.getName() + "\n");
        StringBuilder modules_string = new StringBuilder();
        for (String s : modules_lines)
        {
            modules_string.append(s);
        }
        module_list.setText(modules_string.toString());

        String watermark = "BleachHack epearl edition" + BleachHack.VERSION;
        String welcome = "Welcome, " + mc.player.getName().asString();
        String biome = mc.world.getBiome(mc.player.getBlockPos()).getCategory().getName();
        String biome1 = "Biome: " + biome.substring(0, 1).toUpperCase() + biome.substring(1);
        String playercount = "Online: " + mc.player.networkHandler.getPlayerList().size();
        boolean nether = mc.world.getRegistryKey().getValue().getPath().equalsIgnoreCase("the_nether");
        BlockPos pos = mc.player.getBlockPos();
        Vec3d vec = mc.player.getPos();
        BlockPos pos2 = nether ? new BlockPos(vec.getX() * 8, vec.getY(), vec.getZ() * 8)
                : new BlockPos(vec.getX() / 8, vec.getY(), vec.getZ() / 8);
        String coords = "XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " [" + pos2.getX() + " " + pos2.getY() + " " + pos2.getZ() + "]";
        String server = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;
        String server1 = "IP: " + server;
        int fps = (int) FabricReflect.getFieldValue(MinecraftClient.getInstance(), "field_1738", "currentFps");
        String fps1 = "FPS: " + fps;
        PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getGameProfile().getId());
        int ping = playerEntry == null ? 0 : playerEntry.getLatency();
        String ping1 = "Ping: " + ping;
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        final double deltaX = Math.abs(mc.player.getPos().getX() - mc.player.prevX);
        final double deltaZ = Math.abs(mc.player.getPos().getZ() - mc.player.prevZ);
        String bps = decimalFormat.format((deltaX + deltaZ) * 20);
        String bps1 = "BPS: " + bps;
        String suffix = "";
        if (lastPacket + 7500 < System.currentTimeMillis()) suffix += "....";
        else if (lastPacket + 5000 < System.currentTimeMillis()) suffix += "...";
        else if (lastPacket + 2500 < System.currentTimeMillis()) suffix += "..";
        else if (lastPacket + 1200 < System.currentTimeMillis()) suffix += ".";
        String tps1 = "TPS: " + tps + suffix;
        String radar_title = "Player Radar: ";
        for (Entity e : mc.world.getPlayers().stream().sorted(
                (a, b) -> Double.compare(mc.player.getPos().distanceTo(a.getPos()), mc.player.getPos().distanceTo(b.getPos())
                ))
                .collect(Collectors.toList())) {
            if (e == mc.player) continue;
            int dist = (int) Math.round(mc.player.getPos().distanceTo(e.getPos()));
            radar_player_list = (BleachHack.friendMang.has(e.getDisplayName().getString()) ? "[F] " : "[E] ") + e.getDisplayName().getString() +
                    " | " +
                    " " + e.getBlockPos().getX() +
                    " " + e.getBlockPos().getY() +
                    " " + e.getBlockPos().getZ() +
                    " (" + dist + "m)";
        }

        List<String> ui_lines = new ArrayList<>();
        ui_lines.add(watermark + "\n");
        ui_lines.add(welcome + "\n");
        ui_lines.add(coords + "\n");
        ui_lines.add(server1 + "\n");
        ui_lines.add(fps1 + "\n");
        ui_lines.add(ping1 + "\n");
        ui_lines.add(bps1 + "\n");
        ui_lines.add(tps1 + "\n");
        ui_lines.add(biome1 + "\n");
        ui_lines.add(playercount + "\n\n");
        ui_lines.add(radar_title + "\n");
        ui_lines.add(radar_player_list + "\n");
        StringBuilder ui_string = new StringBuilder();
        for (String s : ui_lines)
        {
            ui_string.append(s);
        }
        hud_input.setText(ui_string.toString());
    }

    @Subscribe
    public void onPacketRead(EventReadPacket event) {
        if (event.getPacket() instanceof GameMessageS2CPacket) {
            List<String> allMatches = new ArrayList<String>();
            String text = ((GameMessageS2CPacket) event.getPacket()).getMessage().toString();
            Pattern p = Pattern.compile("text='(.*?)'");   // the pattern to search for
            Matcher m = p.matcher(text);
            while (m.find()) {
                allMatches.add(m.group(1));
            }
            StringBuilder parsed = new StringBuilder();
            for (String s : allMatches)
            {
                parsed.append(s);
            }
            if (!mc.isInSingleplayer()) {
                chat_window.append(parsed + "\n");
            }
            if (getSetting(0).asToggle().state) {
                event.setCancelled(true);
            }
            int height = (int)chat_window.getPreferredSize().getHeight();
            chat_scroller.getVerticalScrollBar().setValue(height);
        } else if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            lastPacket = System.currentTimeMillis();
            if (time < 500) return;
            long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
            tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
            prevTime = time;
        }
    }

    @Override
    public void onDisable() {
        popout_window.setVisible(false);
        hud_popout_window.setVisible(false);
        popout_window.dispose();
        hud_popout_window.dispose();
        super.onDisable();
    }

}
