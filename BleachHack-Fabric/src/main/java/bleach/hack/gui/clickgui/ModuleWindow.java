package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bleach.hack.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;

public class ModuleWindow {

	private TextRenderer font;
	
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
	
	public ModuleWindow(List<Module> mods, String name, int len, int len2, int posX, int posY) {
		this.mods = mods;
		this.name = name;
		this.len = len;
		this.len2 = len2;
		this.posX = posX;
		this.posY = posY;
	}
	
	public void draw(int mX, int mY, boolean lDown, boolean rDown, int leng, int leng2, boolean dragging) {
		mouseX = mX; mouseY = mY; lmDown = lDown; len = leng; len2 = leng2;
		font = MinecraftClient.getInstance().textRenderer;
		
		if(rDown) selected = "";
		
		Screen.fill(posX, posY-10, posX+len, posY, 
				mouseOver(posX, posY-10, posX+len, posY) ? 0x704060cf : 0x704040ff);
		//return;
		font.drawWithShadow(name, posX+(len/2)-(font.getStringWidth(name)/2), posY-9, 0xffaaff);
		
		// window dragging
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
		
		// Draw Modules
		if(hiding) {
			font.drawWithShadow("«", posX+len-8, posY-9, 0xffaaff);
			return;
		}else font.drawWithShadow("»", posX+len-8, posY-9, 0xffaaff);
			
		int count = 0;
		for(Module m: mods) {
			Screen.fill(posX, posY+(count*10), posX+len, posY+10+(count*10),
					mouseOver(posX, posY+(count*10), posX+len, posY+10+(count*10)) ? 0x70303070 : 0x70000000);
			font.drawWithShadow(cutText(m.getName(), len),
					posX+(len/2)-(font.getStringWidth(cutText(m.getName(), len))/2), posY+1+(count*10),
					m.isToggled() ? 0x6060ff : 0xffffff);
			
			// draw settings 
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
			
			// draw setting indicator 
			if(!m.getSettings().isEmpty()) {
				Screen.fill(posX+len-1, posY+(count*10), posX+len, posY+10+(count*10), 0x5fffffff);
			}
			
			// Set which module settings show on 
			if(mouseOver(posX, posY+(count*10), posX+len, posY+10+(count*10))) {
				// Match lines to end of words 
		        Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getDesc());

		        int c2 = 0, c3 = 0;
		        while(mat.find()) { c2++; } mat.reset();
		        
		        while(mat.find()) {
		        	Screen.fill(posX+len+3, posY-1+(count*10)-(c2 * 10)+(c3 * 10),
							posX+len+6+font.getStringWidth(mat.group()), posY+(count*10)-(c2 * 10)+(c3 * 10)+9,
							0x90000000);
		        	font.drawWithShadow(mat.group(), posX+len+5, posY+(count*10)-(c2 * 10)+(c3 * 10), -1);
					c3++;
				}
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
	
	public int[] getPos() {
		return new int[] {posX, posY};
	}
	
	private void drawModeSetting(SettingMode s, int x, int y) {
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		font.drawWithShadow("| " + s.text + s.modes[s.mode],x+2, y+1,
				mouseOver(x, y, x+len2, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len2, y+10) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		String color;
		
		if(s.state) { if(mouseOver(x, y, x+len2, y+10)) color = "§2"; else color = "§a";
		}else{ if(mouseOver(x, y, x+len2, y+10)) color = "§4"; else color = "§c"; }
		
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		font.drawWithShadow("| " + color + s.text, x+2, y+1, mouseOver(x, y, x+len2, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len2, y+10) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp(len2*((s.getValue() - s.min) / (s.max - s.min)), 0, len2));
		Screen.fill(x, y, x+len2, y+10, 0x70000000);
		Screen.fill(x, y, x+pixels, y+10, 0xf03080a0);
		
		font.drawWithShadow("| " + s.text + s.value, x+2, y+1,
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
