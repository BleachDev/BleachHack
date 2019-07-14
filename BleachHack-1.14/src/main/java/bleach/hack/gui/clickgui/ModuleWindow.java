package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;

public class ModuleWindow {

	/* thats a lot of fields */
	private FontRenderer font = Minecraft.getInstance().fontRenderer;
	private Screen screen;
	
	private List<Module> mods = new ArrayList<>();
	private String name;
	private int len;
	private int len2;
	private int posX;
	private int posY;
	
	private int mouseX;
	private int mouseY;
	private int prevmX;
	private int prevmY;
	private boolean lmDown;
	private String selected;
	private boolean continueDrag;
	private boolean hiding;
	
	public ModuleWindow(Screen screen, List<Module> mods, String name, int len, int len2, int posX, int posY) {
		this.screen = screen;
		this.mods = mods;
		this.name = name;
		this.len = len;
		this.len2 = len2;
		this.posX = posX;
		this.posY = posY;
	}
	
	public void draw(int mX, int mY, boolean lDown, boolean rDown, int leng, int leng2, boolean dragging) {
		mouseX = mX; mouseY = mY; lmDown = lDown; len = leng; len2 = leng2;
		
		if(rDown) selected = "";
		
		Screen.fill(posX, posY-10, posX+len, posY, 
				mouseOver(posX, posY-10, posX+len, posY) ? 0x704060cf : 0x704040ff);
		screen.drawCenteredString(font, name, posX+(len/2), posY-9, 0xffaaff);
		
		/* window dragging */
		if(mouseOver(posX, posY-10, posX+len, posY)) {
			if(rDown) hiding = !hiding;
			if(dragging || continueDrag) {
				continueDrag = true;
				posX = mouseX-(prevmX - posX);
				posY = mouseY-(prevmY - posY);
			}continueDrag = false;
		}
		prevmX = mouseX;
		prevmY = mouseY;
		
		/* Draw Modules */
		if(hiding) {
			font.drawString("«", posX+len-8, posY-9, 0xffaaff);
			return;
		}else font.drawString("»", posX+len-8, posY-9, 0xffaaff);
			
		int count = 0;
		for(Module m: mods) {
			Screen.fill(posX, posY+(count*10), posX+len, posY+10+(count*10),
					mouseOver(posX, posY+(count*10), posX+len, posY+10+(count*10)) ? 0x70303070 : 0x70000000);
			screen.drawCenteredString(font, cutText(m.getName(), len),
					posX+(len/2), posY+1+(count*10), m.isToggled() ? 0x6060ff : 0xffffff);
			
			/* draw settings */
			int count1 = 0;
			if(selected == m.getName()) {
				for(SettingBase s: m.getSettings()) {
					if(s instanceof SettingMode) {
						drawModeSetting(s.toMode(), posX+len, posY+(count*10)+(count1*10));
					}
					if(s instanceof SettingToggle) {
						drawToggleSetting(s.toToggle(), posX+len, posY+(count*10)+(count1*10));
					}
					if(s instanceof SettingSlider) {
						drawSliderSetting(s.toSlider(), posX+len, posY+(count*10)+(count1*10));
					}
					count1++;
				}
			}
			
			/* draw setting indicator */
			if(!m.getSettings().isEmpty()) {
				Screen.fill(posX+len-1, posY+(count*10), posX+len, posY+10+(count*10), 0x5fffffff);
			}
			
			/* Set which module settings show on */
			if(mouseOver(posX, posY+(count*10), posX+len, posY+10+(count*10))) {
				Screen.fill(0, screen.height-13, font.getStringWidth(m.getDesc())+5, screen.height, 0xe0dd99ff);
				Screen.fill(0, screen.height-12, font.getStringWidth(m.getDesc())+4, screen.height, 0xe0000000);
				screen.drawString(font, m.getDesc(), 2, screen.height-10, 0xffc3ff);
				if(lDown) m.toggle();
				if(rDown) selected = m.getName();
			}
			count++;
		}
	}
	
	public void setPos(int x, int y) {
		this.posX = x;
		this.posY = y;
	}
	
	private void drawModeSetting(SettingMode s, int x, int y) {
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		screen.drawString(font, "| " + s.text + s.modes[s.mode],x+2, y+1,
				mouseOver(x, y, x+len2, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len2, y+10) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		String color;
		
		if(s.state) { if(mouseOver(x, y, x+len2, y+10)) color = "§2"; else color = "§a";
		}else{ if(mouseOver(x, y, x+len2, y+10)) color = "§4"; else color = "§c"; }
		
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		screen.drawString(font, "| " + color + s.text, x+2, y+1, mouseOver(x, y, x+len2, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len2, y+10) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(len2*((s.getValue() - s.min) / (s.max - s.min)));
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		Screen.fill(x, y, x+pixels, y+10, 0xf03080a0);
		
		screen.drawString(font, "| " + s.text + s.value, x+2, y+1,
				mouseOver(x, y, x+len2, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len2, y+10) && lmDown) {
			int percent = ((mouseX - x) * 100) / len2;
				
			s.value = s.round(percent*((s.max - s.min) / 100) + s.min, s.round);
		}
	}
	
	private boolean mouseOver(int minX, int minY, int maxX, int maxY) {
		if(mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY) return true;
		return false;
	}
	
	private String cutText(String text, int leng) {
		String text1 = text;
		for(int i = 0; i < text.length(); i++) {
			if(font.getStringWidth(text1) < len-2) return text1;
			text1 = text1.replaceAll(".$", "");
		}
		return "";
	}
}
