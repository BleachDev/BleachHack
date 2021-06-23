/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import bleach.hack.util.BleachLogger;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class ServerScraperScreen extends Screen {

	private TextFieldWidget ipField;
	private MultiplayerScreen serverScreen;
	private Thread scrapeThread;

	private int checked;
	private int working;
	private boolean abort = false;
	private List<BleachServerPinger> pingers = new ArrayList<>();
	private String result = "\u00a77Idle...";

	public ServerScraperScreen(MultiplayerScreen serverScreen) {
		super(new LiteralText("Server Scraper"));
		this.serverScreen = serverScreen;
	}

	public void init() {
		super.init();

		addDrawableChild(new ButtonWidget(width / 2 - 100, height / 3 + 82, 200, 20, new LiteralText("Scrape"), button -> {
			try {
				if (pingers.size() > 0)
					return;
				if (ipField.getText().split(":")[0].trim().isEmpty())
					throw new Exception();
				BleachLogger.logger.info("Starting scraper...");
				InetAddress ip = InetAddress.getByName(ipField.getText().split(":")[0].trim());
				checked = 0;
				working = 0;
				scrapeIp(ip);
			} catch (Exception e) {
				result = "\u00a7cFailed Scraping!";
				e.printStackTrace();
				return;
			}
		}));
		addDrawableChild(new ButtonWidget(width / 2 - 100, height / 3 + 104, 200, 20, new LiteralText("Done"), button -> {
			if (!abort) {
				abort = true;
			}
		}));

		ipField = new TextFieldWidget(textRenderer, width / 2 - 98, height / 4 + 30, 196, 18, LiteralText.EMPTY);
		ipField.changeFocus(true);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		drawCenteredText(matrices, textRenderer, "\u00a77IP:", this.width / 2 - 91, this.height / 4 + 18, -1);
		drawCenteredText(matrices, textRenderer, "\u00a77" + checked + " / 1792 [\u00a7a" + working + "\u00a77]", this.width / 2, this.height / 4 + 58, -1);
		drawCenteredText(matrices, textRenderer, result, this.width / 2, this.height / 4 + 70, -1);
		ipField.render(matrices, mouseX, mouseY, delta);

		if (abort) {
			result = "\u00a77Aborting.. [" + pingers.size() + "] Left";
			if (pingers.size() == 0)
				client.openScreen(new MultiplayerScreen(new TitleScreen()));
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	public void onClose() {
		abort = true;
	}

	public void scrapeIp(InetAddress ip) {
		result = "\u00a7eScraping...";
		scrapeThread = new Thread(() -> {
			for (int change : new int[] { 0, -1, 1, -2, 2, -3, 3 }) {
				for (int i = 0; i <= 255; i++) {
					String newIp = (ip.getAddress()[0] & 255) + "." + (ip.getAddress()[1] & 255)
							+ "." + (ip.getAddress()[2] + change & 255) + "." + i;

					BleachServerPinger ping = new BleachServerPinger();
					ping.ping(newIp, 25565);
					pingers.add(ping);

					while (pingers.size() >= 128 && !abort)
						updatePingers();
				}
			}

			while (pingers.size() > 0)
				updatePingers();
			result = "\u00a7aDone!";
		});
		scrapeThread.start();
	}

	public void updatePingers() {
		for (BleachServerPinger ping : new ArrayList<>(pingers)) {
			if (ping.done) {
				checked++;
				if (!ping.failed && !abort) {
					working++;
					try {
						ServerList list = serverScreen.getServerList();
						list.add(ping.server);
						list.saveFile();
					} catch (Exception e) {
					}
				}
				pingers.remove(ping);
			}
		}
	}

	public boolean charTyped(char chr, int modifiers) {
		if (ipField.isFocused()) {
			ipField.charTyped(chr, modifiers);
		}

		return super.charTyped(chr, modifiers);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (ipField.isFocused()) {
			ipField.keyPressed(keyCode, scanCode, modifiers);
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void tick() {
		ipField.tick();
		super.tick();
	}
}

class BleachServerPinger {

	public ServerInfo server;
	public boolean done = false;
	public boolean failed = false;

	public void ping(String ip, int port) {
		server = new ServerInfo(ip, ip + ":" + port, false);
		BleachLogger.logger.info("Starting Ping " + ip + ":" + port);
		new Thread(() -> {
			MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
			try {
				pinger.add(server, () -> {
				});
			} catch (Exception e) {
				failed = true;
			}
			pinger.cancel();
			done = true;
			BleachLogger.logger.info("Finished Ping " + ip + ":" + port + " > " + failed);
		}).start();
	}
}
