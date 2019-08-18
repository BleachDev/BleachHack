package bleach.hack.gui.clickgui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;

public class ModuleWindow {

	private TextRenderer font;
	
	private LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();
	private String name;
	private int len;
	private int posX;
	private int posY;
	private int height = 12;
	
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
	
	public void draw(int mX, int mY, boolean lDown, boolean rDown, int leng, boolean dragging, int keyDown) {
		mouseX = mX; mouseY = mY; lmDown = lDown; len = leng;
		height = ModuleManager.getModule(ClickGui.class).getSettings().get(0).toMode().mode == 0 ? 12 : 10;
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
			Screen.fill(posX, posY+(count*height), posX+len, posY+height+(count*height),
					mouseOver(posX, posY+(count*height), posX+len, posY+height+(count*height)) ? 0x70303070 : 0x70000000);
			font.drawWithShadow(cutText(m.getKey().getName(), len),
					posX+2, posY+(height>10?2:1)+(count*height), m.getKey().isToggled() ? 0x6060ff : 0xffffff);
			
			Screen.fill(m.getValue() ? posX+len-2 : posX+len-1, posY+(count*height), 
					posX+len, posY+height+(count*height), m.getValue() ? 0x9fffffff : 0x5fffffff);
			
			// Set which module settings show on 
			if(mouseOver(posX, posY+(count*height), posX+len, posY+10+(count*height))) {
				// Match lines to end of words 
		        Matcher mat = Pattern.compile("\\b.{1,22}\\b\\W?").matcher(m.getKey().getDesc());

		        int c2 = 0, c3 = 0;
		        while(mat.find()) { c2++; } mat.reset();
		        
		        while(mat.find()) {
		        	Screen.fill(posX+len+3, posY-1+(count*height)-(c2 * 10)+(c3 * 10),
							posX+len+6+font.getStringWidth(mat.group()), posY+(count*height)-(c2 * 10)+(c3 * 10)+9,
							0x90000000);
		        	font.drawWithShadow(mat.group(), posX+len+5, posY+(count*height)-(c2 * 10)+(c3 * 10), -1);
					c3++;
				}
				if(lDown) m.getKey().toggle();
				if(rDown) mods.replace(m.getKey(), !m.getValue());
			}
			
			// draw settings
			if(m.getValue()) {
				for(SettingBase s: m.getKey().getSettings()) {
					count++;
					if(s instanceof SettingMode) drawModeSetting(s.toMode(), posX, posY+(count*height));
					if(s instanceof SettingToggle) drawToggleSetting(s.toToggle(), posX, posY+(count*height));
					if(s instanceof SettingSlider) drawSliderSetting(s.toSlider(), posX, posY+(count*height));
					Screen.fill(posX+len-1, posY+(count*height), posX+len, posY+height+(count*height), 0x9fffffff);
				}
				count++;
				drawBindSetting(m.getKey(), keyDown, posX, posY+(count*height));
				Screen.fill(posX+len-1, posY+(count*height), posX+len, posY+height+(count*height), 0x9fffffff);
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
	
	private void drawBindSetting(Module m, int key, int x, int y) {
		Screen.fill(x, y, x+len, y+height, 0x70000000);
		
		if(key != -1 && mouseOver(x, y, x+len, y+height)) m.setKey(key != 261 ? key : -1);
		String name = InputUtil.getKeycodeName(m.getKey());
		if(name == null) name = "KEY" + m.getKey();
		if(name.isEmpty()) name = "NONE";
		
		font.drawWithShadow("Bind: " + name + (mouseOver(x, y, x+len, y+height) ? "..." : "")
				, x+2, y+(height>10?2:1), mouseOver(x, y, x+len, y+height) ? 0xffc3ff : 0xffe0ff);
	}
	
	private void drawModeSetting(SettingMode s, int x, int y) {
		Screen.fill(x, y, x+len, y+height, 0x70000000);
		font.drawWithShadow(s.text + s.modes[s.mode],x+2, y+(height>10?2:1),
				mouseOver(x, y, x+len, y+height) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len, y+height) && lmDown) s.mode = s.getNextMode();
	}
	
	public void drawToggleSetting(SettingToggle s, int x, int y) {
		String color;
		
		if(s.state) { if(mouseOver(x, y, x+len, y+height)) color = "§2"; else color = "§a";
		}else{ if(mouseOver(x, y, x+len, y+height)) color = "§4"; else color = "§c"; }
		
		Screen.fill(x, y, x+len, y+height, 0x70000000);
		font.drawWithShadow(color + s.text, x+2, y+(height>10?2:1), -1);
		
		if(mouseOver(x, y, x+len, y+height) && lmDown) s.state = !s.state;
	}
	
	public void drawSliderSetting(SettingSlider s, int x, int y) {
		int pixels = (int) Math.round(MathHelper.clamp(len*((s.getValue() - s.min) / (s.max - s.min)), 0, len));
		Screen.fill(x, y, x+len, y+height, 0x70000000);
		Screen.fill(x, y, x+pixels, y+height, 0xf03080a0);
		
		font.drawWithShadow(s.text + (s.round == 0  && s .value > 100 ? Integer.toString((int)s.value) : s.value),
				x+2, y+(height>10?2:1), mouseOver(x, y, x+len, y+height) ? 0xffc3ff : 0xffe0ff);
		
		if(mouseOver(x, y, x+len, y+height) && lmDown) {
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
