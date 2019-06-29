package bleach.hack.gui.clickgui;

import java.util.List;

import bleach.hack.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ClickGuiParts extends Screen {
	/* Its ya boi, spaghetti code ahoy */
	
	protected Minecraft mc = Minecraft.getInstance();
	protected boolean lMousePressed = false;
	protected boolean rMousePressed = false;
	protected int mX = 0;
	protected int mY = 0;
	
	protected int len = 50; /* Length of modules */
	protected int setLen = 70;  /* Length of settings on modules */
	
	protected String selectedMod;
		
	protected ClickGuiParts(ITextComponent titleIn) {
		super(new StringTextComponent(""));
	}
	
	public boolean doesHoverOver(int minX, int minY, int maxX, int maxY) {
		if(mX > minX && mX < maxX && mY > minY && mY < maxY) {
			return true;
		}
		return false;
	}

	public void drawWindow(List<Module> mods, String title, int x, int y) {
		fill(x, y-10, x+len, y, 0x704040ff);
		drawCenteredString(mc.fontRenderer, title, x+(len/2), y-9, 0xffaaff);
		
		int count = 0;
		for(Module m: mods) {
			fill(x, y+(count*10), x+len, y+10+(count*10),
					doesHoverOver(x, y+(count*10), x+len, y+10+(count*10)) ? 0x70303070 : 0x70000000);
			drawString(mc.fontRenderer, m.getName(), x+2, y+1+(count*10), m.isToggled() ? 0x6060ff : 0xffffff);
			
			int count1 = 0;
			if(selectedMod == m.getName()) {
				for(SettingBase s: m.getSettings()) {
					if(s instanceof SettingMode) {
						drawModeSetting((SettingMode)s, x+len, y+(count*10)+(count1*10));
					}
					if(s instanceof SettingToggle) {
						drawToggleSetting((SettingToggle)s, x+len, y+(count*10)+(count1*10));
					}
					if(s instanceof SettingSlider) {
						drawSliderSetting((SettingSlider)s, x+len, y+(count*10)+(count1*10));
					}
					count1++;
				}
			}
			
			if(!m.getSettings().isEmpty()) fill(x+len-1, y+(count*10), x+len, y+10+(count*10), 0x5fffffff);
			
			if(doesHoverOver(x, y+(count*10), x+52, y+10+(count*10))) {
				if(lMousePressed) m.toggle();
				if(rMousePressed) selectedMod = m.getName();
			}
			
			count++;
		}
	}
	
	public void drawModeSetting(SettingMode s, int x, int y) {
		fill(x, y, x+setLen, y+10, 0x70000000);
		drawString(mc.fontRenderer, "| " + s.text + s.modes[s.mode], x+2, y+1,
				doesHoverOver(x, y, x+setLen, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(doesHoverOver(x, y, x+setLen, y+10)) {
			if(lMousePressed) s.mode = s.getNextMode();
		}
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		String color;
		
		if(s.state) { if(doesHoverOver(x, y, x+setLen, y+10)) color = "§2"; else color = "§a";
		}else{ if(doesHoverOver(x, y, x+setLen, y+10)) color = "§4"; else color = "§c"; }
		
		fill(x, y, x+setLen, y+10, 0x70000000);
		drawString(mc.fontRenderer, "| " + color + s.text, x+2, y+1,
				doesHoverOver(x, y, x+setLen, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(doesHoverOver(x, y, x+setLen, y+10)) {
			if(lMousePressed) s.state = !s.state;
		}
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(setLen*((s.getValue() - s.min) / (s.max - s.min)));
		fill(x, y, x+setLen, y+10, 0x70000000);
		fill(x, y, x+pixels, y+10, 0xf03080a0);
		
		drawString(mc.fontRenderer, "| " + s.text + s.value, x+2, y+1,
				doesHoverOver(x, y, x+setLen, y+10) ? 0xffc3ff : 0xffe0ff);
		
		if(doesHoverOver(x, y, x+setLen, y+10)) {
			if(lMousePressed) {
				int percent = ((mX - x) * 100) / setLen;
				
				s.value = s.round(percent*((s.max - s.min) / 100) + s.min, s.round);
			}
		}
	}

}
