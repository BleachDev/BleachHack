package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class UI extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Arraylist"),
			new SettingToggle(true, "Watermark"),
			new SettingToggle(false, "FPS"),
			new SettingToggle(false, "Ping"),
			new SettingToggle(false, "Coords"),
			new SettingToggle(false, "Server"));
	
	public List<String> bottomLeftList = new ArrayList<>();
	
	public UI() {
		super("UI", -1, Category.RENDER, "Shows stuff onscreen.", settings);
	}
	
	public void onOverlay() {
		bottomLeftList.clear();
		if(getSettings().get(0).toToggle().state) drawArrayList();
		if(getSettings().get(4).toToggle().state) {
			addNetherCoords();
			addCoords();
		}
		if(getSettings().get(2).toToggle().state) addFPS();
		try{ if(getSettings().get(3).toToggle().state) addPing(); }catch(Exception e) {}
		try{ if(getSettings().get(5).toToggle().state) drawServerInfo(); }catch(Exception e) {}
		drawBottomLeft();
	}
	
	/*--------------------------------- Array List ---------------------------------*/
	public void drawArrayList() {
		if(mc.options.debugEnabled) return;
		List<String> lines = new ArrayList<>();
		
		for(Module m: ModuleManager.getModules()) if(m.isToggled()) lines.add(m.getName());
		
		lines.sort((a, b) -> Integer.compare(mc.textRenderer.getStringWidth(b), mc.textRenderer.getStringWidth(a)));
		if(getSettings().get(1).toToggle().state) lines.add(0, "§a> BleachHack " + BleachHack.VERSION);
		
		int count = 0;
		int color = 0xff40bbff;
		for(String s: lines) {
			InGameHud.fill(0, (count*10), mc.textRenderer.getStringWidth(s)+4, 10+(count*10), 0x70003030);
			InGameHud.fill(0, (count*10), 1, 10+(count*10), color);
			InGameHud.fill(mc.textRenderer.getStringWidth(s)+4, (count*10), mc.textRenderer.getStringWidth(s)+5, 10+(count*10), color);
			if(count + 1 < lines.size()) {
				InGameHud.fill(mc.textRenderer.getStringWidth(lines.get(count + 1))+5, 10+(count*10),
						mc.textRenderer.getStringWidth(s)+5, 11+(count*10), color);
			}
			mc.textRenderer.drawWithShadow(s, 3, 1+(count*10), color);
			color -= 200/lines.size();
			count++;
		}
		InGameHud.fill(0, (count*10), mc.textRenderer.getStringWidth(lines.get(count-1))+5, 1+(count*10), color);
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*--------------------------------- Bottom Left ---------------------------------*/
	public void addFPS() {
		int fps = MinecraftClient.getCurrentFps();
		bottomLeftList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
	}
	
	public void addPing() {
		int ping = mc.getNetworkHandler().getPlayerListEntry(
				mc.player.getGameProfile().getId()).getLatency();
		bottomLeftList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
	}
	
	public void addCoords() {
		boolean nether = mc.player.dimension == DimensionType.THE_NETHER;
		BlockPos pos = mc.player.getBlockPos();
				
		bottomLeftList.add("XYZ: " + (nether ? "§4" : "§b") + pos.getX() + " " + pos.getY() + " " + pos.getZ());
	}
	
	public void addNetherCoords() {
		boolean nether = mc.player.dimension == DimensionType.THE_NETHER;
		Vec3d vec = mc.player.getPos();
		BlockPos pos = new BlockPos(vec.getX()/8, vec.getY(), vec.getZ()/8);
		if(nether) pos = new BlockPos(vec.getX()*8, vec.getY(), vec.getZ()*8);
				
		bottomLeftList.add("XYZ: " + (nether ? "§b" : "§4") + pos.getX() + " " + pos.getY() + " " + pos.getZ());
	}
	
	public void drawBottomLeft() {
		//bottomLeftList.sort((a, b) -> Integer.compare(font.getStringWidth(b), font.getStringWidth(a)));
		
		int count = 0;
		for(String s: bottomLeftList) {
			mc.textRenderer.drawWithShadow(s, 2, mc.window.getScaledHeight()-9-(count*10), 0xa0a0a0);
			count++;
		}
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*-------------------------------Top Right---------------------------------------*/
	public void drawServerInfo() {
		String server = mc.isInSingleplayer() ? "" : mc.getCurrentServerEntry().address;
		InGameHud.fill(mc.window.getScaledWidth() - mc.textRenderer.getStringWidth(server) - 4, 2, mc.window.getScaledWidth() - 3, 12, 0xa0000000);
		mc.textRenderer.drawWithShadow(server, mc.window.getScaledWidth() - mc.textRenderer.getStringWidth(server) - 3, 3, 0xb0b0b0);
	}
	/*-------------------------------------------------------------------------------*/
	
	public String getColorString(int value, int best, int good, int mid, int bad, int worst, boolean rev) {
		if(!rev ? value > best : value < best) return "§2";
		else if(!rev ? value > good : value < good) return "§a";
		else if(!rev ? value > mid : value < mid) return "§e";
		else if(!rev ? value > bad : value < bad) return "§6";
		else if(!rev ? value > worst : value < worst) return "§c";
		else return "§4";
	}

}
