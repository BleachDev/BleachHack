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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ServerScraperScreen extends Screen {

	private TextFieldWidget ipField;
	private MultiplayerScreen serverScreen;
	private Thread scrapeThread;
	
	private int checked;
	private int working;
	private boolean abort = false;
	private List<BleachServerPinger> pingers = new ArrayList<>();
	private String result = "§7Idle...";
	
	public ServerScraperScreen(MultiplayerScreen serverScreen) {
		super(new StringTextComponent("Server Scraper"));
		this.serverScreen = serverScreen;
	}
	
	public void init() {
		addButton(new Button(width / 2 - 100, height / 3 + 82, 200, 20, "Scrape", button -> {
			try {
				if(pingers.size() > 0) return;
				if(ipField.getText().split(":")[0].trim().isEmpty()) throw new Exception();
				System.out.println("Starting scraper...");
				InetAddress ip = InetAddress.getByName(ipField.getText().split(":")[0].trim());
				checked = 0;
				working = 0;
				scrapeIp(ip);
			} catch (Exception e) {
				result = "§cFailed Scraping!";
				e.printStackTrace();
				return;
			}
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 104, 200, 20, "Done", button -> {
			if(!abort) {
				abort = true;
				//minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
			}
		}));
		
		ipField = new TextFieldWidget(font, width / 2 - 98, height / 4 + 30, 196, 18, "");
		ipField.setFocused2(true);
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		renderBackground();
		drawCenteredString(font, "§7IP:", this.width / 2 - 91, this.height / 4 + 18, -1);
		drawCenteredString(font, "§7" + checked + " / 1792 [§a" + working + "§7]", this.width / 2, this.height / 4 + 58, -1);
		drawCenteredString(font, result, this.width / 2, this.height / 4 + 70, -1);
		ipField.render(p_render_1_, p_render_2_, p_render_3_);
		
		if(abort) {
			result = "§7Aborting.. [" + pingers.size() + "] Left";
			if(pingers.size() == 0) minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
		}
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public void onClose() {
		abort = true;
	}
	
	public void scrapeIp(InetAddress ip) {
		result = "§eScraping...";
		scrapeThread = new Thread(() -> {
			for(int change : new int[] {0, 1, -1, 2, -2, 3, -3}) {
				for(int i = 0; i <= 255; i++) {
					String newIp = (ip.getAddress()[0] & 255) + "." + (ip.getAddress()[1] & 255)
							+ "." + (ip.getAddress()[2] + change & 255) + "." + i;
							
					BleachServerPinger ping = new BleachServerPinger();
					ping.ping(newIp, 25565);
					pingers.add(ping);
					
					while(pingers.size() >= 128 && !abort) updatePingers();
				}
			}
			
			while(pingers.size() > 0) updatePingers();
			result = "§aDone!";
		});
		scrapeThread.start();
	}
	
	public void updatePingers() {
		for(BleachServerPinger ping: new ArrayList<>(pingers)) {
			if(ping.done) {
				checked++;
				if(!ping.failed && !abort) {
					working++;
					try {
						ServerList list = (ServerList) ObfuscationReflectionHelper.findField(MultiplayerScreen.class, "field_146804_i").get(serverScreen);
						list.addServerData(ping.server);
						list.saveServerList();
					} catch (Exception e) {}
				}
				pingers.remove(ping);
			}
		}
	}
	
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(ipField.isFocused()) ipField.charTyped(p_charTyped_1_, p_charTyped_2_);
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}
	
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if(ipField.isFocused()) ipField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	public void tick() {
		ipField.tick();
		super.tick();
	}
}

class BleachServerPinger {
	
	public ServerData server;
	public boolean done = false;
	public boolean failed = false;
	
	public void ping(String ip, int port) {
		server = new ServerData(ip, ip + ":" + port, false);
		System.out.println("Starting Ping " + ip + ":" + port);
		new Thread(() -> {
			ServerPinger pinger = new ServerPinger();
			try {
				pinger.ping(server);
			}catch(Exception e) {
				failed = true;
			}
			pinger.clearPendingNetworks();
			done = true;
			System.out.println("Finished Ping " + ip + ":" + port + " > " + failed);
		}).start();
	}
}
