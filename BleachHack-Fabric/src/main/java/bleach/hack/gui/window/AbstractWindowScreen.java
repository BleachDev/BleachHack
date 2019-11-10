package bleach.hack.gui.window;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractWindowScreen extends Screen {

	public List<Window> windows = new ArrayList<>();
	private List<Window> renderedWindows = new ArrayList<>();
	
	public AbstractWindowScreen(Text text_1) {
		super(text_1);
	}
	
	public void render(int int_1, int int_2, float float_1) {
		renderedWindows.clear();
		
		boolean close = true;
		boolean noneSelected = true;
		for(Window w: windows) {
			if(!w.closed) {
				close = false;
				if(!w.selected) {
					w.render(int_1, int_2);
					renderedWindows.add(w);
				}
			}
			
			if(w.selected) {
				noneSelected = false;
			}
		}
		
		if(noneSelected && !windows.isEmpty()) windows.get(windows.size() - 1).selected = true;
		if(close) this.onClose();
		
		super.render(int_1, int_2, float_1);
	}
	
	public void renderWindow(int window, int mX, int mY) {
		if(!renderedWindows.contains(windows.get(window)) && !windows.get(window).closed) {
			windows.get(window).render(mX, mY);
			renderedWindows.add(windows.get(window));
		}
	}
	
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		/* Handle what window will be selected when clicking */
		int count = 0;
		int nextSelected = -1;
		for(Window w: windows) {
			w.onMousePressed((int) double_1, (int) double_2);
			
			if(w.shouldClose((int) double_1, (int) double_2)) w.closed = true;
			
			if(double_1 > w.x1 && double_1 < w.x2 && double_2 > w.y1 && double_2 < w.y2 && !w.closed) {
				if(w.selected) {
					nextSelected = -1;
					break;
				}else {
					nextSelected = count;
				}
			}
			count++;
		}
		
		if(nextSelected >= 0) {
			for(Window w: windows) w.selected = false;
			windows.get(nextSelected).selected = true;
		}
		
		return super.mouseClicked(double_1, double_2, int_1);
	}
	
	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		for(Window w: windows) {
			w.onMouseReleased((int) double_1, (int) double_2);
		}
		
		return super.mouseReleased(double_1, double_2, int_1);
	}

}
