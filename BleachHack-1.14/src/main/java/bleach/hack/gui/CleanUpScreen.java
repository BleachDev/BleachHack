package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CleanUpScreen extends Screen {

	private MultiplayerScreen serverScreen;
	private ServerList serverList;
	private List<ServerData> servers;
	
	private boolean cleanNoHost = true;
	private boolean cleanVersion = true;
	private boolean cleanNoPing;
	private boolean cleanAll;
	
	public CleanUpScreen(MultiplayerScreen serverScreen) {
		super(new StringTextComponent("Server Cleanup"));
		this.serverScreen = serverScreen;
	}
	
	@SuppressWarnings("unchecked")
	public void init() {
		try {
			serverList = (ServerList) FieldUtils.getField(MultiplayerScreen.class, "savedServerList", true).get(serverScreen);
			servers = (List<ServerData>) FieldUtils.getField(ServerList.class, "servers", true).get(serverList);
		} catch (Exception e) {}
		
		addButton(new Button(width / 2 - 100, height / 3 - 22, 200, 20, "Unknown Host", button -> {
			cleanNoHost = !cleanNoHost;
		}));
		addButton(new Button(width / 2 - 100, height / 3, 200, 20, "Wrong Version", button -> {
			cleanVersion = !cleanVersion;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 22, 200, 20, "Failed Ping", button -> {
			cleanNoPing = !cleanNoPing;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 44, 200, 20, "Clear All", button -> {
			cleanAll = !cleanAll;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 82, 200, 20, "Clean", button -> {
			for(ServerData s: new ArrayList<>(servers)) {
				if((cleanNoHost && s.serverMOTD.equals(TextFormatting.DARK_RED + "Can\'t resolve hostname")) ||
						(cleanVersion && s.version <= 404) ||
						(cleanNoPing && s.pingToServer != -2L && s.pingToServer < 0L) || cleanAll) {
					//servers.remove(s);
					System.out.println(s.version);
				}
			}
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 104, 200, 20, "Done", button -> {
			minecraft.displayGuiScreen(serverScreen);
		}));
	
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		renderBackground();
		buttons.get(0).setMessage((cleanNoHost ? "§a" : "§c") + "Unknown Host");
		buttons.get(1).setMessage((cleanVersion ? "§a" : "§c") + "Wrong Version");
		buttons.get(2).setMessage((cleanNoPing ? "§a" : "§c") + "Failed Ping");
		buttons.get(3).setMessage((cleanAll ? "§a" : "§c") + "Clear All");
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public void onClose() {
		minecraft.displayGuiScreen(serverScreen);
	}
}