package bleach.hack.module.mods;

import bleach.hack.event.Event;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import com.mojang.brigadier.Command;
import net.minecraft.client.gui.ClientChatListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static JFrame popout_window;
    public static JTextArea module_list;
    //public static JTextArea hud_modules;
    public static JTextArea chat_window;
    public JTextField text_input;
    public JPanel xu;
    public JSplitPane xv;
    public JPanel xw;
    public JPanel chat_panel;
    public JScrollPane module_scroller;
    public JScrollPane chat_scroller;

    public class b {

        public b() {
            c();
            popout_window = new JFrame("BleachHack epearl Edition");
            popout_window.setPreferredSize(new Dimension(1000, 500));
            popout_window.setContentPane(xu);
            popout_window.pack();
            popout_window.setVisible(true);

            popout_window.addWindowListener(new WindowAdapter() {
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
            (xu = new JPanel()).setLayout(new BorderLayout(0, 0));
            xu.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
            xv = new JSplitPane();
            xu.add(xv, "Center");
            (xw = new JPanel()).setLayout(new BorderLayout(0, 0));
            xv.setLeftComponent(xw);
            (module_scroller = new JScrollPane()).setHorizontalScrollBarPolicy(31);
            module_scroller.setVerticalScrollBarPolicy(20);
            xw.add(module_scroller, "Center");
            (module_list = new JTextArea()).setLineWrap(true);
            module_list.setEditable(false);
            module_list.setText("");
            module_scroller.setViewportView(module_list);
            (chat_panel = new JPanel()).setLayout(new BorderLayout(0, 0));
            xv.setRightComponent(chat_panel);
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
            return xu;
        }


            private /* synthetic */ void a ( final ActionEvent actionEvent){
                mc.player.sendChatMessage(text_input.getText());
                text_input.setText("");
            }

    }

    @Subscribe
    public void onDrawOverlay(EventDrawOverlay event) {
        List<String> lines = new ArrayList<>();
        for (Module m : ModuleManager.getModules())
            if (m.isToggled() && m.isDrawn() && !m.getName().equals("UI")) lines.add(m.getName() + "\n");
        StringBuilder modules = new StringBuilder();
        for (String s : lines)
        {
            modules.append(s);
        }
        module_list.setText(modules.toString());
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
        }
    }

    @Override
    public void onDisable() {
        popout_window.setVisible(false);
        popout_window.dispose();
        super.onDisable();
    }

}
