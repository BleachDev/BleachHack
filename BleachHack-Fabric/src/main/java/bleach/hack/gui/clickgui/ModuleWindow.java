package bleach.hack.gui.clickgui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bleach.hack.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;

public class ModuleWindow {

	private TextRenderer font;
	
	private LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();
	private String name;
	private int len;
	private int posX;
	private int posY;
	
	private int mouseX;
	private int mouseY;
	private int prevmX;
	private int prevmY;
	private boolean lmDown;
	private boolean continueDrag;
	private boolean hiding;
	
	public ModuleWindow(List<Module> mods, String name, int len, int posX, int posY) {
		for(Module m: mods) this.mods.put(m, false);
		this.name = name;
		this.len = len;
		this.posX = posX;
		this.posY = posY;
	}
	
	public void draw(int mX, int mY, boolean lDown, boolean rDown, int leng, boolean dragging) {
		mouseX = mX; mouseY = mY; lmDown = lDown; len = leng;
		font = MinecraftClient.getInstance().textRenderer;
		
		Screen.fill(posX, posY-10, posX+len, posY, 
				mouseOver(posX, posY-10, posX+len, posY) ? 0x704060cf : 0x704040ff);
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
			font.drawWithShadow("-", posX+len-8, posY-9, 0xffaaff);
			return;
		}else font.drawWithShadow("+", posX+len-8, posY-9, 0xffaaff);
			
		int count = 0;
		for(Entry<Module, Boolean> m: new LinkedHashMap<>(mods).entrySet()) {
			if(m.getKey().getName().equals("ClickGui")) continue;

			Screen.fill(posX, posY+(count*12), posX+len, posY+12+(count*12),
					mouseOver(posX, posY+(count*12), posX+len, posY+10+(count*12)) ? 0x70303070 : 0x70000000);
			font.drawWithShadow(cutText(m.getKey().getName(), len),
					posX+2, posY+2+(count*12), m.getKey().isToggled() ? 0x6060ff : 0xffffff);
			
			// draw setting indicator 
			if(!m.getKey().getSettings().isEmpty()) {
				Screen.fill(m.getValue() ? posX+len-2 : posX+len-1, posY+(count*12), 
						posX+len, posY+12+(count*12), m.getValue() ? 0x9fffffff : 0x5fffffff);
			}
			
			// Set which module settings show on 
			if(mouseOver(posX, posY+(count*12), posX+len, posY+10+(count*12))) {
				// Match lines to end of words 
		        Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getKey().getDesc());

		        int c2 = 0, c3 = 0;
		        while(mat.find()) { c2++; } mat.reset();
		        
		        while(mat.find()) {
		        	Screen.fill(posX+len+3, posY-1+(count*12)-(c2 * 10)+(c3 * 10),
							posX+len+6+font.getStringWidth(mat.group()), posY+(count*12)-(c2 * 10)+(c3 * 10)+9,
							0x90000000);
		        	font.drawWithShadow(mat.group(), posX+len+5, posY+(count*12)-(c2 * 10)+(c3 * 10), -1);
					c3++;
				}
				if(lDown) m.getKey().toggle();
				if(rDown) mods.replace(m.getKey(), !m.getValue());
			}
			
			// draw settings
			if(m.getValue()) {
				for(SettingBase s: m.getKey().getSettings()) {
					count++;
					if(s instanceof SettingMode) drawModeSetting(s.toMode(), posX, posY+(count*12));
					if(s instanceof SettingToggle) drawToggleSetting(s.toToggle(), posX, posY+(count*12));
					if(s instanceof SettingSlider) drawSliderSetting(s.toSlider(), posX, posY+(count*12));
					Screen.fill(posX+len-1, posY+(count*12), posX+len, posY+12+(count*12), 0x9fffffff);
				}
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
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		font.drawWithShadow(s.text + s.modes[s.mode],x+2, y+2,
				mouseOver(x, y, x+len, y+12) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len, y+12) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		String color;
		
		if(s.state) { if(mouseOver(x, y, x+len, y+12)) color = "§2"; else color = "§a";
		}else{ if(mouseOver(x, y, x+len, y+12)) color = "§4"; else color = "§c"; }
		
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		font.drawWithShadow(color + s.text, x+2, y+2, -1);
		
		if(mouseOver(x, y, x+len, y+12) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp(len*((s.getValue() - s.min) / (s.max - s.min)), 0, len));
		Screen.fill(x, y, x+len, y+12, 0x70000000);
		Screen.fill(x, y, x+pixels, y+12, 0xf03080a0);
		
		font.drawWithShadow(s.text + s.value, x+2, y+2,
				mouseOver(x, y, x+len, y+12) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len, y+12) && lmDown) {
			int percent = ((mouseX - x) * 100) / len;
				
			s.value = s.round(percent*((s.max - s.min) / 100) + s.min, s.round);
		}
	}
	
	private boolean mouseOver(int minX, int minY, int maxX, int maxY) {
        return mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY;
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
