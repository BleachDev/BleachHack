package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.module.Category;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class ClickGuiScreen extends Screen {
	private int len;
	
	public List<ModuleWindow> tabs = new ArrayList<>();
	
	public ClickGuiScreen() {
		super(new LiteralText("ClickGui"));
	}
	
	public void initWindows() {
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.EXPLOITS), "Exploits", len, 30, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.MISC), "Misc", len, 100, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.COMBAT), "Combat", len, 170, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.PLAYER), "Player", len, 240, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.MOVEMENT), "Movement", len, 310, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.RENDER), "Render", len, 380, 35));
		tabs.add(new ModuleWindow(ModuleManager.getModulesInCat(Category.WORLD), "World", len, 450, 35));
	}
	
	public boolean isPauseScreen() {
	      return false;
	}
	
	public void onClose() {
		ModuleManager.getModule(ClickGui.class).setToggled(false);
		this.minecraft.openScreen(null);
	}
	
	public void render(int int_1, int int_2, float float_1) {
		this.renderBackground();
		font.draw("BleachHack-1.14-" + BleachHack.VERSION, 3, 3, 0x305090);
		font.draw("BleachHack-1.14-" + BleachHack.VERSION, 2, 2, 0x6090d0);
		font.drawWithShadow("Hover over a bind setting and press a key to change a bind" , 2, height-10, 0xff9999);
		font.drawWithShadow("Use .guireset to reset the gui" , 2, height-20, 0x9999ff);
		for(ModuleWindow w: tabs) w.draw(int_1, int_2, len);
		
		len = (int) Math.round(ModuleManager.getModule(ClickGui.class).getSettings().get(1).toSlider().getValue());
		super.render(int_1, int_2, float_1);
	}
	
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if(p_mouseClicked_5_ == 0) for(ModuleWindow w: tabs) w.onLmPressed();
		else if(p_mouseClicked_5_ == 1) for(ModuleWindow w: tabs) w.onRmPressed();
		
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
	
	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		if(int_1 == 0) for(ModuleWindow w: tabs) w.onLmReleased();
		return super.mouseReleased(double_1, double_2, int_1);
	}
	
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		for(ModuleWindow w: tabs) w.onKeyPressed(int_1);
		return super.keyPressed(int_1, int_2, int_3);
	}
	
	public void resetGui() {
		tabs.get(0).setPos(30, 35);
		tabs.get(1).setPos(100, 35);
		tabs.get(2).setPos(170, 35);
		tabs.get(3).setPos(240, 35);
		tabs.get(4).setPos(310, 35);
		tabs.get(5).setPos(380, 35);
		tabs.get(6).setPos(450, 35);
	}
}
