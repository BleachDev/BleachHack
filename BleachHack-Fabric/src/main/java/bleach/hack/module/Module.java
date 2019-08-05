package bleach.hack.module;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class Module {

	protected MinecraftClient mc = MinecraftClient.getInstance();
	private String name;
	private FabricKeyBinding key;
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
	public void onOverlay() {}

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

	public FabricKeyBinding getKey() {
		return key;
	}
	
	public List<? extends SettingBase> getSettings() {
		return settings;
	}

	public void setKey(FabricKeyBinding key) {
		this.key = key;
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}
	
	public void registerBind(String name, int keycode) {
		this.key = FabricKeyBinding.Builder.create(
				new Identifier("bleachhack", name.toLowerCase()), InputUtil.Type.KEYSYM, keycode, "BleachHack").build();
		KeyBindingRegistry.INSTANCE.register(key);
	}
	
}
