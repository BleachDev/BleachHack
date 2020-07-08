/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.client.options.ServerEntry;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import net.minecraft.text.Text;

public class AutoReconnect extends Module {
	
	public ServerEntry server;
	
	public AutoReconnect() {
		super("AutoReconnect", KEY_UNBOUND, Category.MISC, "Shows reconnect options when disconnecting from a server",
				new SettingToggle("Auto", true),
				new SettingSlider("Time: ", 0.2, 10, 5, 2));
	}
	
	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (event.getScreen() instanceof DisconnectedScreen
				&& !(event.getScreen() instanceof newDisconnectScreen)) {
			mc.openScreen(new newDisconnectScreen((DisconnectedScreen) event.getScreen()));
			event.setCancelled(true);
		}
	}
	
	@Subscribe
	public void readPacket(EventReadPacket event) {
		if (event.getPacket() instanceof DisconnectS2CPacket) {
			try { server = mc.getCurrentServerEntry(); } catch (Exception e) {}
		}
	}
	
	@Subscribe
	public void sendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof HandshakeC2SPacket) {
			try { server = new ServerEntry("Server", 
					(String) FabricReflect.getFieldValue(event.getPacket(), "field_13159", "address") + ":"
					+ (int) FabricReflect.getFieldValue(event.getPacket(), "field_13157", "port"), false); } catch (Exception e) {}
		}
	}
	
	public class newDisconnectScreen extends DisconnectedScreen {

		public long reconnectTime = Long.MAX_VALUE - 1000000L;
		public int reasonH = 0;
		
		public newDisconnectScreen(DisconnectedScreen screen) {
			super((Screen) FabricReflect.getFieldValue(screen, "field_2456", "parent"), "Disconnect",
					(Text) FabricReflect.getFieldValue(screen, "field_2457", "reason"));
			reasonH = (int) FabricReflect.getFieldValue(screen, "field_2454", "reasonHeight");
		}
		
		public void init() {
			super.init();
			reconnectTime = System.currentTimeMillis();
			addButton(new ButtonWidget(width / 2 - 100, height / 2 + reasonH / 2 + 35, 200, 20, "Reconnect", (button) -> {
				if (server != null) minecraft.openScreen(new ConnectScreen(new MultiplayerScreen(new TitleScreen()), minecraft, server));
		    }));
			addButton(new ButtonWidget(width / 2 - 100, height / 2 + reasonH / 2 + 57, 200, 20,
					(getSettings().get(0).toToggle().state ? "§a" : "§c") + "AutoReconnect ["
							+ ((reconnectTime + getSettings().get(1).toSlider().getValue() * 1000) - System.currentTimeMillis())
							+ "]", (button) -> {
		        getSettings().get(0).toToggle().state = !getSettings().get(0).toToggle().state;
		        reconnectTime = System.currentTimeMillis();
		    }));
		}
		
		public void render(int int_1, int int_2, float float_1) {
			super.render(int_1, int_2, float_1);
			
			buttons.get(2).setMessage((getSettings().get(0).toToggle().state ? "§aAutoReconnect ["
					+ ((reconnectTime + getSettings().get(1).toSlider().getValue() * 1000) - System.currentTimeMillis())
					+ "]" : "§cAutoReconnect [" + getSettings().get(1).toSlider().getValue() * 1000 + "]"));
			
			if (reconnectTime + getSettings().get(1).toSlider().getValue() * 1000 < System.currentTimeMillis() && getSettings().get(0).toToggle().state) {
				if (server != null) minecraft.openScreen(new ConnectScreen(new MultiplayerScreen(new TitleScreen()), minecraft, server));
				reconnectTime = System.currentTimeMillis();
			}
		}
		
	}

}
