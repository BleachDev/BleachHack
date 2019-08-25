package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

public class UI extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Arraylist"),
			new SettingToggle(false, "Extra Line"),
			new SettingToggle(true, "Watermark"),
			new SettingToggle(false, "FPS"),
			new SettingToggle(false, "Ping"),
			new SettingToggle(false, "Coords"),
			new SettingToggle(false, "TPS"),
			new SettingToggle(false, "Server"),
			new SettingToggle(false, "Players"));
	
	public List<String> bottomLeftList = new ArrayList<>();
	private int count = 0;
	private long prevTime = 0;
	private double tps = 20;
	
	public UI() {
		super("UI", -1, Category.RENDER, "Shows stuff onscreen.", settings);
	}
	
	@Subscribe
	public void onDrawOverlay(EventDrawOverlay overlayEvent) {
		bottomLeftList.clear();
		if(getSettings().get(0).toToggle().state) drawArrayList();
		if(getSettings().get(8).toToggle().state) drawPlayerList();
		if(getSettings().get(5).toToggle().state) {
			addNetherCoords();
			addCoords();
		}
		if(getSettings().get(3).toToggle().state) addFPS();
		try{ if(getSettings().get(4).toToggle().state) addPing(); }catch(Exception e) {}
		if(getSettings().get(6).toToggle().state) addTPS();
		try{ if(getSettings().get(7).toToggle().state) drawServerInfo(); }catch(Exception e) {}
		drawBottomLeft();
	}
	
	@Subscribe
	public void readPacket(EventReadPacket eventReadPacket) {
		if(eventReadPacket.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			long time = System.currentTimeMillis();
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			//System.out.println((double) timeOffset / 1000);
			tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
			prevTime = time;
		}
	}
	
	/*--------------------------------- Array List ---------------------------------*/
	public void drawArrayList() {
		if(mc.options.debugEnabled) return;
		List<String> lines = new ArrayList<>();
		
		for(Module m: ModuleManager.getModules()) if(m.isToggled()) lines.add(m.getName());
		
		lines.sort((a, b) -> Integer.compare(mc.textRenderer.getStringWidth(b), mc.textRenderer.getStringWidth(a)));
		if(getSettings().get(2).toToggle().state) lines.add(0, "§a> BleachHack " + BleachHack.VERSION);
		
		int color = 0xff40bbff;
		count = 0;
		int extra = getSettings().get(1).toToggle().state ? 1 : 0;
		for(String s: lines) {
			InGameHud.fill(0, count*10, mc.textRenderer.getStringWidth(s)+3+extra, 10+(count*10), 0x70003030);
			InGameHud.fill(0, count*10, extra, 10+(count*10), color);
			InGameHud.fill(mc.textRenderer.getStringWidth(s)+3+extra, (count*10), mc.textRenderer.getStringWidth(s)+4+extra, 10+(count*10), color);
			if(count + 1 < lines.size()) {
				InGameHud.fill(mc.textRenderer.getStringWidth(lines.get(count + 1))+4+extra, 10+(count*10),
						mc.textRenderer.getStringWidth(s)+4+extra, 11+(count*10), color);
			}
			mc.textRenderer.drawWithShadow(s, 2+extra, 1+(count*10), color);
			color -= 200/lines.size();
			count++;
		}
		InGameHud.fill(0, (count*10), mc.textRenderer.getStringWidth(lines.get(count-1))+4+extra, 1+(count*10), color);
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*--------------------------------- Player List ---------------------------------*/
	public void drawPlayerList() {
		if(mc.options.debugEnabled) return;
		
		InGameHud.fill(0, 3+(count*10), mc.textRenderer.getStringWidth("Players:")+4, 13+(count*10), 0x40000000);
		mc.textRenderer.drawWithShadow("Players:", 2, 4+count*10, 0xff0000);
		count++;
		
		for(Entity e: mc.world.getPlayers().stream().sorted(
				(a,b) -> Double.compare(mc.player.getPos().distanceTo(a.getPos()), mc.player.getPos().distanceTo(b.getPos())))
				.collect(Collectors.toList())) {
			if(e == mc.player) continue;
			
			String text = e.getDisplayName().asFormattedString() + " | " + 
					e.getBlockPos().getX() + " " + e.getBlockPos().getY() + " " + e.getBlockPos().getZ()
					+ " (" + Math.round(mc.player.getPos().distanceTo(e.getPos())) + "m)";
			
			InGameHud.fill(0, 3+(count*10), mc.textRenderer.getStringWidth(text)+4, 13+(count*10), 0x40000000);
			mc.textRenderer.drawWithShadow(text, 2, 4+count*10,
					0xf00000 + (int) Math.min(mc.player.getPos().distanceTo(e.getPos()) * 3, 255));
			count++;
		}
	}
	/*-------------------------------------------------------------------------------*/
	
	
	
	/*--------------------------------- Bottom Left ---------------------------------*/
	public void addFPS() {
		int fps = MinecraftClient.getCurrentFps();
		bottomLeftList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
	}
	
	public void addTPS() {
		bottomLeftList.add("TPS: " + getColorString((int) tps, 18, 15, 12, 8, 4, false) + tps);
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
