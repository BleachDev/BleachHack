package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class IngameOverlay extends AbstractGui {
	
	private MainWindow window = Minecraft.getInstance().mainWindow;
	private FontRenderer font = Minecraft.getInstance().fontRenderer;
	
	public List<String> bottomLeftList = new ArrayList<>();
	
	public IngameOverlay() {
	}
	
	/*--------------------------------- Array List ---------------------------------*/
	public void drawArrayList() {
		if(Minecraft.getInstance().gameSettings.showDebugInfo) return;
		List<String> lines = new ArrayList<>();
		
		for(Module m: ModuleManager.getModules()) if(m.isToggled()) lines.add(m.getName());
		
		lines.sort((a, b) -> Integer.compare(font.getStringWidth(b), font.getStringWidth(a)));
		
		int count = 0;
		int color = 0x40bbff;
		for(String s: lines) {
			fill(0, 1+(count*10),font.getStringWidth(s)+3, 11+(count*10), 0x50000000);
			font.drawStringWithShadow(s, 2, 2+(count*10), color);
			color -= 200/lines.size();
			count++;
		}
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*--------------------------------- Bottom Left ---------------------------------*/
	public void addFPS() {
		int fps = Minecraft.getDebugFPS();
		bottomLeftList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
	}
	
	public void addPing() {
		int ping = Minecraft.getInstance().getConnection().getPlayerInfo(
				Minecraft.getInstance().player.getGameProfile().getId()).getResponseTime();
		bottomLeftList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
	}
	
	public void addCoords() {
		boolean nether = Minecraft.getInstance().player.dimension == DimensionType.THE_NETHER;
		BlockPos pos = Minecraft.getInstance().player.getPosition();
				
		bottomLeftList.add("XYZ: " + (nether ? "§4" : "§b") + pos.getX() + " " + pos.getY() + " " + pos.getZ());
	}
	
	public void addNetherCoords() {
		boolean nether = Minecraft.getInstance().player.dimension == DimensionType.THE_NETHER;
		Vec3d vec = Minecraft.getInstance().player.getPositionVec();
		BlockPos pos = new BlockPos(vec.getX()/8, vec.getY(), vec.getZ()/8);
		if(nether) pos = new BlockPos(vec.getX()*8, vec.getY(), vec.getZ()*8);
				
		bottomLeftList.add("XYZ: " + (nether ? "§b" : "§4") + pos.getX() + " " + pos.getY() + " " + pos.getZ());
	}
	
	public void drawBottomLeft() {
		//bottomLeftList.sort((a, b) -> Integer.compare(font.getStringWidth(b), font.getStringWidth(a)));
		
		int count = 0;
		for(String s: bottomLeftList) {
			font.drawStringWithShadow(s, 2, window.getScaledHeight()-9-(count*10), 0xa0a0a0);
			count++;
		}
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*-------------------------------Top Right---------------------------------------*/
	public void drawServerInfo() {
		String server = Minecraft.getInstance().isSingleplayer() ? "" : Minecraft.getInstance().getCurrentServerData().serverIP;
		fill(window.getScaledWidth() - font.getStringWidth(server) - 4, 2, window.getScaledWidth() - 3, 12, 0xa0000000);
		font.drawStringWithShadow(server, window.getScaledWidth() - font.getStringWidth(server) - 3, 3, 0xb0b0b0);
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
