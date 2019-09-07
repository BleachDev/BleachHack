package bleach.hack.module;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.gui.clickgui.SettingBase;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;

public class Module {

	protected MinecraftClient mc = MinecraftClient.getInstance();
	private String name;
	private int key;
	private boolean toggled;
	private Category category;
	private String desc;
	private List<SettingBase> settings = new ArrayList<>();
	
	public Module(String nm, int k, Category c, String d, List<SettingBase> s) {
		name = nm;
		setKey(k);
		category = c;
		desc = d;
		if(s != null) settings = s;
		toggled = false;
	}
	
	
	public void toggle() {
		toggled = !toggled;
		if(toggled) onEnable();
		else onDisable();
	}
	
	public void onEnable() {
		Method[] methods = this.getClass().getMethods();
		for(Method method : methods) {
			if (method.isAnnotationPresent(Subscribe.class)) {
				BleachHack.eventBus.register(this);
				break;
			}
		}
	}

	public void onDisable() {
		Method[] methods = this.getClass().getMethods();
		for(Method method : methods) {
			if (method.isAnnotationPresent(Subscribe.class)) {
				BleachHack.eventBus.unregister(this);
				break;
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getKey() {
		return key;
	}
	
	public List<SettingBase> getSettings() {
		return settings;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
		if(toggled) onEnable();
		else onDisable();
	}
	
}
