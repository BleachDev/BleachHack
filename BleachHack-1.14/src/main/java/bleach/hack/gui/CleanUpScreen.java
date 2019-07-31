package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class CleanUpScreen extends Screen {

	private MultiplayerScreen serverScreen;
	private ServerList serverList;
	private List<ServerData> servers = new ArrayList<ServerData>();
	private String result = "";
	
	private boolean cleanNoHost = true;
	private boolean cleanVersion = true;
	private boolean cleanNoPing = false;
	private boolean cleanAll = false;
	
	public CleanUpScreen(MultiplayerScreen serverScreen) {
		super(new StringTextComponent("Server Cleanup"));
		this.serverScreen = serverScreen;
	}
	
	@SuppressWarnings("unchecked")
	public void init() {
		try {
			serverList = (ServerList) ObfuscationReflectionHelper.findField(MultiplayerScreen.class, "field_146804_i").get(serverScreen);
			servers.addAll((List<ServerData>)  ObfuscationReflectionHelper.getPrivateValue(ServerList.class, serverList, "field_78858_b"));
		} catch (Exception e) {}
		
		addButton(new Button(width / 2 - 100, height / 3 - 32, 200, 20, "Unknown Host", button -> {
			cleanNoHost = !cleanNoHost;
		}));
		addButton(new Button(width / 2 - 100, height / 3 - 10, 200, 20, "Outdated Version", button -> {
			cleanVersion = !cleanVersion;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 12, 200, 20, "Failed Ping", button -> {
			cleanNoPing = !cleanNoPing;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 34, 200, 20, "Clear All", button -> {
			cleanAll = !cleanAll;
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 82, 200, 20, "Clean", button -> {
			for(ServerData s: servers) {
				try {
					if(s.serverMOTD == null) s.serverMOTD = "";
					if((cleanNoHost && s.serverMOTD.contains("Can't resolve hostname")) ||
							(cleanVersion && s.version <= 404) ||
							(cleanNoPing && (s.serverMOTD.contains("Pinging...") || s.serverMOTD.contains("Can't connect to server"))) ||
							cleanAll) {
						serverList.func_217506_a(s);
						serverList.saveServerList();
					}
				}catch(Exception e) {e.printStackTrace();}
			}
			result = "§aFinished";
		}));
		addButton(new Button(width / 2 - 100, height / 3 + 104, 200, 20, "Done", button -> {
			minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
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
		minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
	}
}