/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventOpenScreen;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.text.Text;


public class AutoReconnect extends Module {

	public ServerInfo server;

	public AutoReconnect() {
		super("AutoReconnect", KEY_UNBOUND, ModuleCategory.MISC, "Shows reconnect options when disconnecting from a server.",
				new SettingToggle("Auto", true).withDesc("Automatically reconnects.").withChildren(
						new SettingSlider("Delay", 0.2, 10, 5, 2).withDesc("How long to wait before reconnecting (in seconds).")));
	}

	@BleachSubscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (event.getScreen() instanceof DisconnectedScreen
				&& !(event.getScreen() instanceof NewDisconnectScreen)) {
			mc.setScreen(new NewDisconnectScreen((DisconnectedScreen) event.getScreen()));
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void sendPacket(EventPacket.Send event) {
		if (event.getPacket() instanceof HandshakeC2SPacket) {
			HandshakeC2SPacket packet = (HandshakeC2SPacket) event.getPacket();
			server = new ServerInfo("Server", packet.getAddress() + ":" + packet.getPort(), false);
		}
	}

	public class NewDisconnectScreen extends DisconnectedScreen {

		public long reconnectTime = Long.MAX_VALUE - 1000000L;

		private ButtonWidget reconnectButton;

		public NewDisconnectScreen(DisconnectedScreen screen) {
			super(screen.parent, screen.getTitle(), screen.reason);
		}

		public void init() {
			super.init();

			reconnectTime = System.currentTimeMillis();
			int buttonH = Math.min(height / 2 + this.reasonHeight / 2 + 9, height - 30);

			addDrawableChild(new ButtonWidget(width / 2 - 100, buttonH + 22, 200, 20, Text.literal("Reconnect"), button -> {
				if (server != null)
					ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(server.address), server);
			}));
			reconnectButton = addDrawableChild(new ButtonWidget(width / 2 - 100, buttonH + 44, 200, 20, Text.empty(), button -> {
				getSetting(0).asToggle().setValue(!getSetting(0).asToggle().getState());
				reconnectTime = System.currentTimeMillis();
			}));
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			super.render(matrices, mouseX, mouseY, delta);

			int startTime = (int) (getSetting(0).asToggle().getChild(0).asSlider().getValue() * 1000);
			reconnectButton.setMessage(Text.literal(
					getSetting(0).asToggle().getState()
					? "\u00a7aAutoReconnect [" + (reconnectTime + startTime - System.currentTimeMillis()) + "]"
							: "\u00a7cAutoReconnect [" + startTime + "]"));

			if (reconnectTime + startTime < System.currentTimeMillis() && getSetting(0).asToggle().getState()) {
				if (server != null)
					ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(server.address), server);
			}
		}

	}

}
