package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class FakeLag extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Always", "Pulse"}, "Mode: "),
			new SettingToggle(false, "Limit"),
			new SettingSlider(0, 15, 5, 1, "Limit: "),
			new SettingSlider(0, 5, 1, 1, "Pulse: "));
	
	public List<PlayerMoveC2SPacket> queue = new ArrayList<>();
	public long startTime = 0;
	public boolean catchup = false;
	
	public FakeLag() {
		super("FakeLag", -1, Category.MOVEMENT, "Stores up movement packets", settings);
	}
	
	@Override
	public void onEnable() {
		startTime = System.currentTimeMillis();
		queue.clear();
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
		sendPackets();
		super.onDisable();
	}
	
	@Subscribe
    public void sendPacket(EventSendPacket eventSendPacket) {
		if(!(eventSendPacket.getPacket() instanceof PlayerMoveC2SPacket
				|| eventSendPacket.getPacket() instanceof PlayerMoveC2SPacket.PositionOnly
				|| eventSendPacket.getPacket() instanceof PlayerMoveC2SPacket.LookOnly
				|| eventSendPacket.getPacket() instanceof PlayerMoveC2SPacket.PositionOnly)) return;
		queue.add((PlayerMoveC2SPacket) eventSendPacket.getPacket());
		eventSendPacket.setCancelled(true);
	}
	
	@Subscribe
	public void onTick(EventTick eventTick) {
		if(getSettings().get(0).toMode().mode == 0) {
			if(getSettings().get(1).toToggle().state &&
					System.currentTimeMillis() - startTime > getSettings().get(2).toSlider().getValue() * 1000) setToggled(false);
		}else if(getSettings().get(0).toMode().mode == 1) {
			if(System.currentTimeMillis() - startTime > getSettings().get(3).toSlider().getValue() * 1000) {
				setToggled(false);
				setToggled(true);
			}
		}
	}
	
	public void sendPackets() {
		for(PlayerMoveC2SPacket p: new ArrayList<>(queue)) {
			if(p instanceof PlayerMoveC2SPacket.LookOnly) continue;
			mc.player.networkHandler.sendPacket(p);
		}
		queue.clear();
	}
}
