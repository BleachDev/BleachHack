package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.BleachHack;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindowDark;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindowFuture;
import bleach.hack.gui.clickgui.modulewindow.ModuleWindowLight;
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
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.EXPLOITS), "Exploits", len, 30, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.MISC), "Misc", len, 100, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.COMBAT), "Combat", len, 170, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.PLAYER), "Player", len, 240, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.MOVEMENT), "Movement", len, 310, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.RENDER), "Render", len, 380, 35));
		tabs.add(new ModuleWindowDark(ModuleManager.getModulesInCat(Category.WORLD), "World", len, 450, 35));
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
		
		/* Change Themes */
		int mode = ModuleManager.getModule(ClickGui.class).getSettings().get(0).toMode().mode;
		List<ModuleWindow> tempTabs = new ArrayList<>();
		for(ModuleWindow m: tabs) {
			if(mode == 0 && !(m instanceof ModuleWindowLight)) tempTabs.add(new ModuleWindowLight(m.modList, m.name, len, m.posX, m.posY));
			else if(mode == 1 && !(m instanceof ModuleWindowDark)) tempTabs.add(new ModuleWindowDark(m.modList, m.name, len, m.posX, m.posY));
			else if(mode == 2 && !(m instanceof ModuleWindowFuture)) tempTabs.add(new ModuleWindowFuture(m.modList, m.name, len, m.posX, m.posY));
			
			if(!tempTabs.isEmpty()) tempTabs.get(tempTabs.size() -1).mods = m.mods;
		}
		if(!tempTabs.isEmpty()) tabs = tempTabs;
		
		len = (int) Math.round(ModuleManager.getModule(ClickGui.class).getSettings().get(1).toSlider().getValue());
		for(ModuleWindow w: tabs) w.draw(int_1, int_2, len);
		
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
		int x = 30;
		for(ModuleWindow m: tabs) {
			m.setPos(x, 35);
			x += len + 5;
		}
	}
}
