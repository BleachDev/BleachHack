package bleach.hack.gui;

import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.utils.LoginManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class LoginGui extends Screen {

	ParticleManager particleMang = new ParticleManager();
	
	private TextFieldWidget userField;
	private TextFieldWidget passField;
	
	private String loginResult = "";
	
	public LoginGui(ITextComponent titleIn) {
		super(titleIn);
	}
	
	public void init() {
		this.addButton(new Button(width / 2 - 100, height / 3 + 84, 200, 20, "Done", (button) -> {
			minecraft.displayGuiScreen(new BleachMainMenu(null));
	    }));
		this.addButton(new Button(width / 2 - 100, height / 3 + 62, 200, 20, "Login", (button) -> {
			loginResult = LoginManager.login(userField.getText(), passField.getText());
	    }));
		
		this.userField = new TextFieldWidget(this.font, width / 2 - 98, height / 4 + 10, 196, 18, "");
		this.passField = new TextFieldWidget(this.font, width / 2 - 98, height / 4 + 40, 196, 18, "");
		
		super.init();
		
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		fill(0, 0, width, height, 0xff000000);
		
		particleMang.addParticle(p_render_1_, p_render_2_);
		particleMang.renderParticles();
		
		this.drawString(this.font, "Email: ", this.width / 2 - 130, this.height / 4 + 15, 0xffffff);
		this.drawString(this.font, "Password: ", this.width / 2 - 154, this.height / 4 + 45, 0xffffff);
		
		this.drawString(this.font, loginResult, this.width / 2 - 40, this.height / 4 + 63, 0xffffff);
		
		drawString(this.font, "Logged in as: §a" + this.minecraft.getSession().getUsername(), 4, height - 10, -1);
		
		userField.render(p_render_1_, p_render_2_, p_render_3_);
		passField.render(p_render_1_, p_render_2_, p_render_3_);
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(userField.isFocused()) userField.charTyped(p_charTyped_1_, p_charTyped_2_);
		if(passField.isFocused()) passField.charTyped(p_charTyped_1_, p_charTyped_2_);
		
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}
	
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		double mX = p_mouseClicked_1_, mY = p_mouseClicked_3_;
		
		if(mX>userField.x && mX<userField.x+userField.getWidth() && mY>userField.y && mY<userField.y+userField.getHeight()) {
			userField.setFocused2(true);
			passField.setFocused2(false);
			userField.setEnabled(true);
		}
		
		if(mX>passField.x && mX<passField.x+passField.getWidth() && mY>passField.y && mY<passField.y+passField.getHeight()) {
			passField.setFocused2(true);
			userField.setFocused2(false);
		}
		
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
	
	public void tick() {
		userField.tick();
		passField.tick();
		super.tick();
	}
	
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if(userField.isFocused()) userField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if(passField.isFocused()) passField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

}
