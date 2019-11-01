package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.client.network.packet.PlaySoundS2CPacket;
import net.minecraft.client.network.packet.PlayerRespawnS2CPacket;
import net.minecraft.client.network.packet.StopSoundS2CPacket;
import net.minecraft.server.network.packet.*;

public class CancelPacket extends Module {

    public CancelPacket() {
        super("Cancel Pkts", -1, Category.MISC, "Cancel Packets.",
            new SettingToggle("Respawn S2C", false), //0
            new SettingToggle("Stop Sound S2C", false), //1
            new SettingToggle("Play Sound S2C", false), //2
            new SettingToggle("Disconnect S2C", false), //3
            new SettingToggle("Chat C2S", false), //4
            new SettingToggle("CraftReq C2S", false), //5
            new SettingToggle("VehicleMove C2S", false), //6
            new SettingToggle("Move C2S", false), //7
            new SettingToggle("Use C2S", false), //8
            new SettingToggle("UseEnt C2S", false), //9
            new SettingToggle("TP Confirm C2S", false), //10
            new SettingToggle("All", false), //11
            new SettingMode("Method: ", "Null Pkt", "Set Cancelled", "Both"));
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (getSettings().get(11).toToggle().state) {
            if (getSettings().get(12).toMode().mode == 0 || getSettings().get(11).toMode().mode == 2)
                event.setPacket(null);
            if (getSettings().get(12).toMode().mode == 1 || getSettings().get(11).toMode().mode == 2)
                event.setCancelled(true);
        } else {
            if (getSettings().get(12).toMode().mode == 0 || getSettings().get(11).toMode().mode == 2) {
                if (getSettings().get(0).toToggle().state && event.getPacket() instanceof PlayerRespawnS2CPacket)
                    event.setPacket(null);
                if (getSettings().get(1).toToggle().state && event.getPacket() instanceof StopSoundS2CPacket)
                    event.setPacket(null);
                if (getSettings().get(2).toToggle().state && event.getPacket() instanceof PlaySoundS2CPacket)
                    event.setPacket(null);
                if (getSettings().get(3).toToggle().state && event.getPacket() instanceof DisconnectS2CPacket)
                    event.setPacket(null);
            }
            if (getSettings().get(12).toMode().mode == 1 || getSettings().get(11).toMode().mode == 2) {
                if (getSettings().get(0).toToggle().state && event.getPacket() instanceof PlayerRespawnS2CPacket)
                    event.setCancelled(true);
                if (getSettings().get(1).toToggle().state && event.getPacket() instanceof StopSoundS2CPacket)
                    event.setCancelled(true);
                if (getSettings().get(2).toToggle().state && event.getPacket() instanceof PlaySoundS2CPacket)
                    event.setCancelled(true);
                if (getSettings().get(3).toToggle().state && event.getPacket() instanceof DisconnectS2CPacket)
                    event.setCancelled(true);
            }
        }
    }
    
    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (getSettings().get(11).toToggle().state) {
            if (getSettings().get(12).toMode().mode == 0 || getSettings().get(11).toMode().mode == 2)
                event.setPacket(null);
            if (getSettings().get(12).toMode().mode == 1 || getSettings().get(11).toMode().mode == 2)
                event.setCancelled(true);
        } else {
            if (getSettings().get(12).toMode().mode == 0 || getSettings().get(11).toMode().mode == 2) {
                if (getSettings().get(4).toToggle().state && event.getPacket() instanceof ChatMessageC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(5).toToggle().state && event.getPacket() instanceof CraftRequestC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(6).toToggle().state && event.getPacket() instanceof VehicleMoveC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(7).toToggle().state && event.getPacket() instanceof PlayerMoveC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(8).toToggle().state && event.getPacket() instanceof PlayerInteractBlockC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(9).toToggle().state && event.getPacket() instanceof PlayerInteractEntityC2SPacket)
                    event.setPacket(null);
                if (getSettings().get(10).toToggle().state && event.getPacket() instanceof TeleportConfirmC2SPacket)
                    event.setPacket(null);
            }
            if (getSettings().get(12).toMode().mode == 1 || getSettings().get(11).toMode().mode == 2) {
                if (getSettings().get(4).toToggle().state && event.getPacket() instanceof ChatMessageC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(5).toToggle().state && event.getPacket() instanceof CraftRequestC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(6).toToggle().state && event.getPacket() instanceof VehicleMoveC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(7).toToggle().state && event.getPacket() instanceof PlayerMoveC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(8).toToggle().state && event.getPacket() instanceof PlayerInteractBlockC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(9).toToggle().state && event.getPacket() instanceof PlayerInteractEntityC2SPacket)
                    event.setCancelled(true);
                if (getSettings().get(10).toToggle().state && event.getPacket() instanceof TeleportConfirmC2SPacket)
                    event.setCancelled(true);
            }
        }
    }

}
