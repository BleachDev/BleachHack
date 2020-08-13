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
package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.options.ServerList;
import net.minecraft.text.LiteralText;

public class CleanUpScreen extends Screen {

	private MultiplayerScreen serverScreen;
	private ServerList serverList;
	private List<ServerInfo> servers = new ArrayList<>();
	private String result = "";

	private boolean cleanNoHost = true;
	private boolean cleanVersion = true;
	private boolean cleanNoPing = false;
	private boolean cleanAll = false;

	public CleanUpScreen(MultiplayerScreen serverScreen) {
		super(new LiteralText("Server Cleanup"));
		this.serverScreen = serverScreen;
	}

	public void init() {
		try {
			serverList = serverScreen.getServerList();
			for (int i = 0; i < serverList.size(); i++)
				servers.add(serverList.get(i));
		} catch (Exception e) {
		}

		addButton(new ButtonWidget(width / 2 - 100, height / 3 - 32, 200, 20, "Unknown Host", button -> {
			cleanNoHost = !cleanNoHost;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 - 10, 200, 20, "Outdated Version", button -> {
			cleanVersion = !cleanVersion;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 12, 200, 20, "Failed Ping", button -> {
			cleanNoPing = !cleanNoPing;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 34, 200, 20, "Clear All", button -> {
			cleanAll = !cleanAll;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 82, 200, 20, "Clean", button -> {
			for (ServerInfo s : servers) {
				try {
					if (s.label == null)
						s.label = "";
					if ((cleanNoHost && s.label.contains("Can't resolve hostname"))
							|| (cleanVersion && s.protocolVersion < SharedConstants.getGameVersion().getProtocolVersion())
							|| (cleanNoPing && (s.label.contains("Pinging...") || s.label.contains("Can't connect to server"))) || cleanAll) {
						serverList.remove(s);
						serverList.saveFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result = "§aFinished";
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 104, 200, 20, "Done", button -> {
			minecraft.openScreen(new MultiplayerScreen(new TitleScreen(false)));
		}));

	}

	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		renderBackground();
		buttons.get(0).setMessage((cleanNoHost ? "§a" : "§c") + "Unknown Host");
		buttons.get(1).setMessage((cleanVersion ? "§a" : "§c") + "Wrong Version");
		buttons.get(2).setMessage((cleanNoPing ? "§a" : "§c") + "Failed Ping");
		buttons.get(3).setMessage((cleanAll ? "§a" : "§c") + "Clear All");
		drawCenteredString(font, result, width / 2, height / 3 + 58, -1);

		super.render(p_render_1_, p_render_2_, p_render_3_);
	}

	public void onClose() {
		minecraft.openScreen(new MultiplayerScreen(new TitleScreen(false)));
	}
}