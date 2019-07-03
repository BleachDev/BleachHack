package bleach.hack.gui.clickgui;

import bleach.hack.BleachHack;
import bleach.hack.module.ModuleManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ClickGuiScreen extends ClickGuiParts {

	private int posX = 35;
	private int posY = 30;
	
	public ClickGuiScreen(ITextComponent titleIn) {
		super(new StringTextComponent("ClickGui"));
	}
	
	public void onClose() {
		ModuleManager.getModuleByName("ClickGui").setToggled(false);
		this.minecraft.displayGuiScreen(null);
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		mc.fontRenderer.drawString("BleachHack-1.14-" + BleachHack.VERSION, 3, 3, 0x305090);
		mc.fontRenderer.drawString("BleachHack-1.14-" + BleachHack.VERSION, 2, 2, 0x6090d0);
		drawWindow(ModuleManager.getModules(), "Modules", posX, posY);
		
		mX = p_render_1_;
		mY = p_render_2_;
		lMousePressed = false;
		rMousePressed = false;
		len = (int) Math.round(ModuleManager.getModuleByName("ClickGui")
				.getSettings().get(0).toSlider().getValue());
		setLen = (int) Math.round(ModuleManager.getModuleByName("ClickGui")
				.getSettings().get(1).toSlider().getValue());
	}
	
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if(p_mouseClicked_5_ == 0) lMousePressed = true;
		else if(p_mouseClicked_5_ == 1) {
			rMousePressed = true;
			selectedMod = "";
		}
		
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
}
