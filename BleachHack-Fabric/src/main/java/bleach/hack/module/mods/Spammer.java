package bleach.hack.module.mods;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;

public class Spammer extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Random", "Order"}, "Read: "),
			new SettingSlider(1, 120, 20, 0, "Delay: "));
	
	private BleachFileMang fileMang = new BleachFileMang();
	private Random rand = new Random();
	private List<String> lines = new ArrayList<>();
	private int lineCount = 0;
	
	public Spammer() {
		super("Spammer", -1, Category.MISC, "Spams chat with messagees you set (edit in spammer.txt)", settings);
	}

	@Override
	public void onEnable() {
		BleachHack.getEventBus().register(this);
		fileMang.createFile(Paths.get("spammer.txt"), "");
		lines = fileMang.readFileLines(Paths.get("spammer.txt"));
		lineCount = 0;
	}

	@Override
	public void onDisable() {
		BleachHack.getEventBus().unregister(this);
	}

	@Subscribe
	public void onTick(EventTick eventTick) {
		if(lines.isEmpty()) return;
		
		if(mc.player.age % (int) (getSettings().get(1).toSlider().getValue() * 20) == 0) {
			if(getSettings().get(0).toMode().mode == 0) {
				mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())));
			}else if(getSettings().get(0).toMode().mode == 1) {
				mc.player.sendChatMessage(lines.get(lineCount));
			}
			
			if(lineCount >= lines.size() -1) lineCount = 0;
			else lineCount++;
		}
	}

}
