package bleach.hack.module;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class Module {

	protected Minecraft mc = Minecraft.getInstance();
	private String name;
	private KeyBinding key;
	private boolean toggled;
	private Category category;
	private String desc;
	private List<SettingBase> settings = new ArrayList<>();
	
	public Module(String nm, int k, Category c, String d, List<SettingBase> s) {
		name = nm;
		registerBind(nm, k);
		category = c;
		desc = d;
		if(s != null) settings = s;
		toggled = false;
	}
	
	
	public void toggle() {
		toggled = !toggled;
		if(toggled) {
			onEnable();
		}else {
			onDisable();
		}
	}
	
	public void onEnable() {}
	public void onDisable() {}
	public void onUpdate() {}
	public void onRender() {}

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

	public KeyBinding getKey() {
		return key;
	}
	
	public List<? extends SettingBase> getSettings() {
		return settings;
	}

	public void setKey(KeyBinding key) {
		this.key = key;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}
	
	/* Ripped from rusherhack because keybindings were the only way i got input to work */
	public void registerBind(String name, int keycode) {
		this.key = new KeyBinding(name, keycode, "BleachHack");
		ClientRegistry.registerKeyBinding(this.key);
	}
	
}
