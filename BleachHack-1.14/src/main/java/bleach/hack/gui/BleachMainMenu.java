package bleach.hack.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BleachMainMenu extends Screen {
	
	public BleachMainMenu(ITextComponent titleIn) {
		super(new TranslationTextComponent("narrator.screen.title"));
	}
	
	public void init() {
		this.addButton(new Button(width / 2 - 100, height / 4 + 48, 200, 20, I18n.format("menu.singleplayer"), (p_213089_1_) -> {
			this.minecraft.displayGuiScreen(new WorldSelectionScreen(this));
	    }));
		this.addButton(new Button(width / 2 - 100, height / 4 + 72, 200, 20, I18n.format("menu.multiplayer"), (p_213086_1_) -> {
	        this.minecraft.displayGuiScreen(new MultiplayerScreen(this));
	    }));
		this.addButton(new Button(width / 2 - 100, height / 4 + 96, 200, 20, "Login Manager", (button) -> {
	        this.minecraft.displayGuiScreen(new LoginGui(new StringTextComponent("LoginManager")));
	    }));
		this.addButton(new Button(this.width / 2 - 100, height / 4 + 129, 98, 20, I18n.format("menu.options"), (p_213096_1_) -> {
	        this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings));
	    }));
	    this.addButton(new Button(this.width / 2 + 2, height / 4 + 129, 98, 20, I18n.format("menu.quit"), (p_213094_1_) -> {
	    	this.minecraft.shutdown();
	    }));
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		
		fill(0, 0, width, height, 0xff000000);
		
		GL11.glScaled(3, 3, 3);
		drawString(this.font, "BleachHack", (width/2 - 80)/3, (height/4 - 15)/3, 0xffc0e0);
		GL11.glScaled(1d/3d, 1d/3d, 1d/3d);
		
		int copyWidth = this.font.getStringWidth("Copyright Mojang AB. Do not distribute!") + 2;
		
		drawString(this.font, "Copyright Mojang AB. Do not distribute!", width - copyWidth, height - 10, -1);
		drawString(this.font, "Minecraft " + SharedConstants.getVersion().getName(), 4, height - 20, -1);
		drawString(this.font, "Logged in as: �a" + this.minecraft.getSession().getUsername(), 4, height - 10, -1);
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
}