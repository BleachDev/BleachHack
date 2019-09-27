package bleach.hack.gui.clickgui.modulewindow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import bleach.hack.module.Module;
import net.minecraft.client.font.TextRenderer;

public abstract class ModuleWindow {
	
	public TextRenderer font;
	
	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();
	public String name;
	public int len;
	public int posX;
	public int posY;
	
	public int mouseX;
	public int mouseY;
	public int prevmX;
	public int prevmY;
	public boolean hiding;
	
	public int keyDown;
	public boolean lmDown;
	public boolean rmDown;
	public boolean lmHeld;
	public boolean dragging;
	
	public ModuleWindow(List<Module> mods, String name, int len, int posX, int posY) {
		modList = mods;
		for(Module m: mods) this.mods.put(m, false);
		this.name = name;
		this.len = len;
		this.posX = posX;
		this.posY = posY;
	}
	
	public abstract void draw(int mX, int mY, int leng);
	
	public void setPos(int x, int y) {
		this.posX = x;
		this.posY = y;
	}
	
	public int[] getPos() {
		return new int[] {posX, posY};
	}
	
	public void onLmPressed() { lmDown = true; lmHeld = true; }
	
	public void onLmReleased() { lmHeld = false; }
	
	public void onRmPressed() { rmDown = true; }
	
	public void onKeyPressed(int key) { keyDown = key; }
	
	protected boolean mouseOver(int minX, int minY, int maxX, int maxY) {
        return mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY;
    }
	
	protected String cutText(String text, int leng) {
		String text1 = text;
		for(int i = 0; i < text.length(); i++) {
			if(font.getStringWidth(text1) < len-2) return text1;
			text1 = text1.replaceAll(".$", "");
		}
		return "";
	}
}
