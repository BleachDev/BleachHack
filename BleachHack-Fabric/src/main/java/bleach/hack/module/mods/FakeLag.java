package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class FakeLag extends Module {
	
	public List<PlayerMoveC2SPacket> queue = new ArrayList<>();
	public long startTime = 0;
	
	public FakeLag() {
		super("FakeLag", -1, Category.MOVEMENT, "Stores up movement packets",
				new SettingMode("Mode: ", "Always", "Pulse"),
				new SettingToggle("Limit", false),
				new SettingSlider("Limit: ", 0, 15, 5, 1),
				new SettingSlider("Pulse: ", 0, 5, 1, 1));
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
    public void sendPacket(EventSendPacket event) {
		if(!(event.getPacket() instanceof PlayerMoveC2SPacket
				|| event.getPacket() instanceof PlayerMoveC2SPacket.PositionOnly
				|| event.getPacket() instanceof PlayerMoveC2SPacket.LookOnly
				|| event.getPacket() instanceof PlayerMoveC2SPacket.Both)) return;
		queue.add((PlayerMoveC2SPacket) event.getPacket());
		event.setCancelled(true);
	}
	
	@Subscribe
	public void onTick(EventTick event) {
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
