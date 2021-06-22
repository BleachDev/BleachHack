/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
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
import net.minecraft.client.option.ServerList;
import net.minecraft.client.util.math.MatrixStack;
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
		super.init();

		try {
			serverList = serverScreen.getServerList();
			for (int i = 0; i < serverList.size(); i++)
				servers.add(serverList.get(i));
		} catch (Exception e) {
		}

		addButton(new ButtonWidget(width / 2 - 100, height / 3 - 32, 200, 20, new LiteralText("Unknown Host"), button -> {
			cleanNoHost = !cleanNoHost;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 - 10, 200, 20, new LiteralText("Outdated Version"), button -> {
			cleanVersion = !cleanVersion;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 12, 200, 20, new LiteralText("Failed Ping"), button -> {
			cleanNoPing = !cleanNoPing;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 34, 200, 20, new LiteralText("Clear All"), button -> {
			cleanAll = !cleanAll;
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 82, 200, 20, new LiteralText("Clean"), button -> {
			for (ServerInfo s : servers) {
				try {
					if (s.label == null)
						s.label = LiteralText.EMPTY;
					if ((cleanNoHost && s.label.getString().contains("Can't resolve hostname")) ||
							(cleanVersion && s.protocolVersion < SharedConstants.getGameVersion().getProtocolVersion()) ||
							(cleanNoPing && (s.label.getString().contains("Pinging...") || s.label.getString().contains("Can't connect to server"))) ||
							cleanAll) {
						serverList.remove(s);
						serverList.saveFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			result = "\u00a7aFinished";
		}));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 104, 200, 20, new LiteralText("Done"), button -> {
			client.openScreen(new MultiplayerScreen(new TitleScreen(false)));
		}));

	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		buttons.get(0).setMessage(new LiteralText((cleanNoHost ? "\u00a7a" : "\u00a7c") + "Unknown Host"));
		buttons.get(1).setMessage(new LiteralText((cleanVersion ? "\u00a7a" : "\u00a7c") + "Wrong Version"));
		buttons.get(2).setMessage(new LiteralText((cleanNoPing ? "\u00a7a" : "\u00a7c") + "Failed Ping"));
		buttons.get(3).setMessage(new LiteralText((cleanAll ? "\u00a7a" : "\u00a7c") + "Clear All"));
		drawCenteredText(matrices, textRenderer, result, width / 2, height / 3 + 58, -1);

		super.render(matrices, mouseX, mouseY, delta);
	}

	public void onClose() {
		client.openScreen(new MultiplayerScreen(new TitleScreen(false)));
	}
}