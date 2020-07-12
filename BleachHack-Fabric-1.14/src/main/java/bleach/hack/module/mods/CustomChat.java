/*
 By _Jasuu and x79
 		UwU
 */

package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingMode;
//import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting;


import com.google.common.eventbus.Subscribe;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import bleach.hack.mixin.IChatMessageC2SPacket;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;


public class CustomChat extends Module{
    
	//private static final String KEY_UNBOUND = null;
	private final String Bleach_SUFFIX = " \u23D0 \u1D47\u02E1\u1D49\u1D43\u1D9C\u02B0 \u02B0\u1D43\u1D43\u1D9C\u1D4F";
	
    private boolean commands = false;

    public CustomChat() {
        super("Custom Chat", KEY_UNBOUND, Category.PLAYER, "custom chat uwu");
    }
    
    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            String s = ((IChatMessageC2SPacket) event.getPacket()).getChatMessage();
            if (s.startsWith("/") && !commands) return;
            s += Bleach_SUFFIX;
            if (s.length() >= 256) s = s.substring(0, 256);
            ((IChatMessageC2SPacket) event.getPacket()).setChatMessage(s);
            }
        }
}