package bleach.hack.gui.window;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractWindowScreen extends Screen {

	public List<Window> windows = new ArrayList<>();
	
	public AbstractWindowScreen(Text text_1) {
		super(text_1);
	}
	
	public void render(int int_1, int int_2, float float_1) {
		boolean close = true;
		for(Window w: windows) {
			if(!w.closed) {
				close = false;
				w.render();
			}
		}
		
		if(close) this.onClose();
		
		super.render(int_1, int_2, float_1);
	}
	
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		for(Window w: windows) {
			if(w.shouldClose((int) double_1, (int) double_2)) w.closed = true;
		}
		
		return super.mouseClicked(double_1, double_2, int_1);
	}

}
