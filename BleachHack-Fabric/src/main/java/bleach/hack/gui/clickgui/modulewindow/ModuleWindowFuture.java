package bleach.hack.gui.clickgui.modulewindow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class ModuleWindowFuture extends ModuleWindow {
	
	public ModuleWindowFuture(List<Module> mods, String name, int len, int posX, int posY) {
		super(mods, name, len, posX, posY);
	}
	
	public void draw(int mX, int mY, int leng) {
		mouseX = mX; mouseY = mY; len = leng;
		font = MinecraftClient.getInstance().textRenderer;
		
		Screen.fill(posX, posY-12, posX+len, posY, 
				mouseOver(posX, posY-12, posX+len, posY) ? 0x70cf3030 : 0x70ff3030);
		font.drawWithShadow(name, posX+3, posY-10, -1);
		
		/* window dragging */
		if(mouseOver(posX, posY-12, posX+len, posY)) {
			if(rmDown) hiding = !hiding;
			if(lmDown) dragging = true;
		}
		
		if(!lmHeld) dragging = false;
		if(dragging) {
			posX = mouseX-(prevmX - posX);
			posY = mouseY-(prevmY - posY);
		}
		
		prevmX = mouseX;
		prevmY = mouseY;
		
		/* Draw Modules */
		if(hiding) {
			font.drawWithShadow("-", posX+len-8, posY-10, -1);
			return;
		}else font.drawWithShadow("+", posX+len-8, posY-10, -1);
			
		int count = 0;
		for(Entry<Module, Boolean> m: new LinkedHashMap<>(mods).entrySet()) {
			if(m.getKey().isToggled()) Screen.fill(posX, posY+(count*14), posX+len, posY+14+(count*14), 0xffff0000);
			Screen.fill(posX, posY+(count*14), posX+len, posY+14+(count*14),
					mouseOver(posX, posY+(count*14), posX+len, posY+14+(count*14)) ? 0x70303030 : 0x70000000);
			font.drawWithShadow(cutText(m.getKey().getName(), len),
					posX+2, posY+3+(count*14), 0xffffff);
			
			Screen.fill(posX+len-1, posY+(count*14), posX+len, posY+14+(count*14), m.getValue() ? 0xafffffff : 0x5fffffff);
			
			/* Set which module settings show on */
			if(mouseOver(posX, posY+(count*14), posX+len, posY+14+(count*14))) {
				/* Match lines to end of words */
		        Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getKey().getDesc());

		        int c2 = 0, c3 = 0;
		        while(mat.find()) { c2++; } mat.reset();
		        
		        while(mat.find()) {
		        	Screen.fill(posX+len+3, posY-1+(count*14)-(c2 * 10)+(c3 * 10),
							posX+len+6+font.getStringWidth(mat.group().trim()), posY+(count*14)-(c2 * 10)+(c3 * 10)+9,
							0x90000000);
		        	font.drawWithShadow(mat.group(), posX+len+5, posY+(count*14)-(c2 * 10)+(c3 * 10), -1);
					c3++;
				}
				if(lmDown) m.getKey().toggle();
				if(rmDown) mods.replace(m.getKey(), !m.getValue());
				if(lmDown || rmDown) MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			
			/* draw settings */
			if(m.getValue()) {
				for(SettingBase s: m.getKey().getSettings()) {
					count++;
					if(s instanceof SettingMode) drawModeSetting(s.toMode(), posX, posY+(count*14));
					if(s instanceof SettingToggle) drawToggleSetting(s.toToggle(), posX, posY+(count*14));
					if(s instanceof SettingSlider) drawSliderSetting(s.toSlider(), posX, posY+(count*14));
				}
				count++;
				drawBindSetting(m.getKey(), keyDown, posX, posY+(count*14));
			}
			count++;
		}
		
		lmDown = false;
		rmDown = false;
		keyDown = -1;
	}
	
	public void drawBindSetting(Module m, int key, int x, int y) {
		Screen.fill(x, y, x+len, y+14, mouseOver(x, y, x+len, y+14) ? 0x70303030 : 0x70000000);
		
		if(key != -1 && mouseOver(x, y, x+len, y+14)) m.setKey((key != 261 && key != 256) ? key : -1);
		String name = InputUtil.getKeycodeName(m.getKey());
		if(name == null) name = "KEY" + m.getKey();
		if(name.isEmpty()) name = "NONE";
		
		font.drawWithShadow("Bind §7" + name + (mouseOver(x, y, x+len, y+14) ? "..." : "")
				, x+2, y+3, -1);
	}
	
	public void drawModeSetting(SettingMode s, int x, int y) {
		Screen.fill(x, y, x+len, y+14, mouseOver(x, y, x+len, y+14) ? 0x70303030 : 0x70000000);
		font.drawWithShadow(s.text + "§7" + s.modes[s.mode],x+2, y+3, -1);
		
		if(mouseOver(x, y, x+len, y+14) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		Screen.fill(x, y, x+len, y+14, mouseOver(x, y, x+len, y+14) ? 0x70303030 : 0x70000000);
		if(s.state) Screen.fill(x+1, y, x+len-1, y+14, mouseOver(x, y, x+len, y+14) ? 0x90ff0000 : 0x70ff0000);
		font.drawWithShadow(s.text, x+3, y+3, -1);
		
		if(mouseOver(x, y, x+len, y+14) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp((len-2)*((s.getValue() - s.min) / (s.max - s.min)), 0, len-2));
		Screen.fill(x, y, x+len, y+14, mouseOver(x, y, x+len, y+14) ? 0x70303030 : 0x70000000);
		Screen.fill(x+1, y, x+1+pixels, y+14, mouseOver(x, y, x+len, y+14) ? 0x90ff0000 : 0x70ff0000);
		
		font.drawWithShadow(s.text + "§7" + (s.round == 0  && s .value > 100 ? Integer.toString((int)s.value) : s.value),
				x+2, y+3, -1);
		
		if(mouseOver(x, y, x+len, y+14) && lmHeld) {
			int percent = ((mouseX - x) * 100) / (len-2);
				
			s.value = s.round(percent*((s.max - s.min) / 100) + s.min, s.round);
		}
	}
}
