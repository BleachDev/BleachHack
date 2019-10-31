package bleach.hack.module.mods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingMode;
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
	
	public List<String> infoList = new ArrayList<>();
	private int count = 0;
	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;
	
	public UI() {
		super("UI", -1, Category.RENDER, "Shows stuff onscreen.",
				new SettingToggle("Arraylist", true),
				new SettingToggle("Extra Line", false),
				new SettingToggle("Watermark", true),
				new SettingToggle("FPS", true),
				new SettingToggle("Ping", true),
				new SettingToggle("Coords", true),
				new SettingToggle("TPS", true),
				new SettingToggle("Lag-Meter", true),
				new SettingToggle("Server", false),
				new SettingToggle("Players", false),
				new SettingMode("Info: ", "Down Left", "Top Right", "Down Right"));
	}
	
	@Subscribe
	public void onDrawOverlay(EventDrawOverlay event) {
		infoList.clear();
		
		if(getSettings().get(0).toToggle().state && !mc.options.debugEnabled) {
			List<String> lines = new ArrayList<>();
			
			for(Module m: ModuleManager.getModules()) if(m.isToggled()) lines.add(m.getName());
			
			lines.sort((a, b) -> Integer.compare(mc.textRenderer.getStringWidth(b), mc.textRenderer.getStringWidth(a)));
			if(getSettings().get(2).toToggle().state) lines.add(0, "§a> BleachHack " + BleachHack.VERSION);
			
			int age = mc.player.age % 510;
			Color clr = new Color(255, (age > 255 ? 510 - age : age), 255 - (age > 255 ? 510 - age : age));
			int color = clr.getRGB();
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
				clr = new Color(clr.getRed() - 250/lines.size(), Math.min(255, clr.getGreen() + 255/lines.size()), clr.getBlue());
				color = clr.getRGB();
				count++;
			}
			InGameHud.fill(0, (count*10), mc.textRenderer.getStringWidth(lines.get(count-1))+4+extra, 1+(count*10), color);
		}
		
		if(getSettings().get(5).toToggle().state) {
			boolean nether = mc.player.dimension == DimensionType.THE_NETHER;
			BlockPos pos = mc.player.getBlockPos();
			Vec3d vec = mc.player.getPos();
			BlockPos pos2 = nether ? new BlockPos(vec.getX()*8, vec.getY(), vec.getZ()*8)
					: new BlockPos(vec.getX()/8, vec.getY(), vec.getZ()/8);
			
			infoList.add("XYZ: " + (nether ? "§4" : "§b") + pos.getX() + " " + pos.getY() + " " + pos.getZ()
			+ " §7[" + (nether ? "§b" : "§4") + pos2.getX() + " " + pos2.getY() + " " + pos2.getZ() + "§7]");
		}
		
		if(getSettings().get(3).toToggle().state) {
			int fps = MinecraftClient.getCurrentFps();
			infoList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
		}
		
		if(getSettings().get(4).toToggle().state) {
			int ping = 0;
			try{ mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId()).getLatency(); }catch(Exception e) {}
			infoList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
		}
		
		if(getSettings().get(6).toToggle().state) {
			String suffix = "§7";
			if(lastPacket + 7500 < System.currentTimeMillis()) suffix += "....";
			else if(lastPacket + 5000 < System.currentTimeMillis()) suffix += "...";
			else if(lastPacket + 2500 < System.currentTimeMillis()) suffix += "..";
			else if(lastPacket + 1200 < System.currentTimeMillis()) suffix += ".";
			
			infoList.add("TPS: " + getColorString((int) tps, 18, 15, 12, 8, 4, false) + tps + suffix);
		}
		
		if(getSettings().get(7).toToggle().state) {
			long time = System.currentTimeMillis();
			if(time - lastPacket > 500) {
				String text = "Server Lagging For: " + ((time - lastPacket) / 1000d) + "s";
				mc.textRenderer.drawWithShadow(text, mc.window.getScaledWidth() / 2 - mc.textRenderer.getStringWidth(text) / 2,
						Math.min((time - lastPacket - 500) / 20 - 20, 10), 0xd0d0d0);
			}
		}
		
		if(getSettings().get(8).toToggle().state) {
			String server = "";
			try{ server = mc.getCurrentServerEntry().address; }catch(Exception e) {}
			InGameHud.fill(mc.window.getScaledWidth() - mc.textRenderer.getStringWidth(server) - 4, 2, mc.window.getScaledWidth() - 3, 12, 0xa0000000);
			mc.textRenderer.drawWithShadow(server, mc.window.getScaledWidth() - mc.textRenderer.getStringWidth(server) - 3, 3, 0xb0b0b0);
		}
		
		if(getSettings().get(9).toToggle().state && !mc.options.debugEnabled) {
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
		
		int count2 = 0;
		int infoMode = getSettings().get(10).toMode().mode;
		for(String s: infoList) {
			mc.textRenderer.drawWithShadow(s, 
					infoMode == 0 ? 2 : mc.window.getScaledWidth() - mc.textRenderer.getStringWidth(s) - 2,
					infoMode == 1 ? 2+(count2*10) : mc.window.getScaledHeight()-9-(count2*10), 0xa0a0a0);
			count2++;
		}
	}
	
	@Subscribe
	public void readPacket(EventReadPacket event) {
		lastPacket = System.currentTimeMillis();
		if(event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			long time = System.currentTimeMillis();
			if(time < 500) return;
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
			prevTime = time;
		}
	}
	
	public String getColorString(int value, int best, int good, int mid, int bad, int worst, boolean rev) {
		if(!rev ? value > best : value < best) return "§2";
		else if(!rev ? value > good : value < good) return "§a";
		else if(!rev ? value > mid : value < mid) return "§e";
		else if(!rev ? value > bad : value < bad) return "§6";
		else if(!rev ? value > worst : value < worst) return "§c";
		else return "§4";
	}

}
