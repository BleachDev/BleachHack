package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.gui.widget.BleachCheckbox;
import bleach.hack.utils.BleachCipher;
import bleach.hack.utils.LoginManager;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class LoginScreen extends Screen {

	private ParticleManager particleMang = new ParticleManager();
	private BleachFileMang fileMang = new BleachFileMang();
	private BleachCipher cipher = new BleachCipher();
	
	private TextFieldWidget userField;
	private TextFieldWidget passField;
	private BleachCheckbox checkBox;
	
	List<String[]> entries;
	
	private String loginResult = "";
	
	public LoginScreen() {
		super(new LiteralText("Login Screen"));
	}
	
	public void init() {
		entries = readEntries();
		
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 84, 200, 20, "Done", (button) -> {
			minecraft.openScreen(new BleachMainMenu());
	    }));
		addButton(new ButtonWidget(width / 2 - 100, height / 3 + 62, 200, 20, "Login", (button) -> {
			loginResult = LoginManager.login(userField.getText(), passField.getText());
			if(checkBox.checked && loginResult == "§aLogin Successful"
					&& !entries.contains(new String[] {userField.getText(), passField.getText()})) {
				entries.add(new String[] {userField.getText(), passField.getText()});
				writeEntry(userField.getText() + ":" + passField.getText());
			}
	    }));
		
		userField = new TextFieldWidget(font, width / 2 - 98, height / 4 + 10, 196, 18, "");
		passField = new TextFieldWidget(font, width / 2 - 98, height / 4 + 40, 196, 18, "");
		checkBox = new BleachCheckbox(width / 2 - 99, height / 4 + 63, width / 2 - 89, height / 4 + 64, "Save Login", false);
		userField.setMaxLength(32767);
		passField.setMaxLength(32767);
		
		super.init();
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		renderBackground();
		fill(0, 0, width, height, 0xff000000);
		
		particleMang.addParticle(p_render_1_, p_render_2_);
		particleMang.renderParticles();
		
		drawString(font, "Email: ", width / 2 - 130, height / 4 + 15, 0xC0C0C0);
		drawString(font, "Password: ", width / 2 - 154, height / 4 + 45, 0xC0C0C0);
		
		drawString(font, loginResult == "" ? "" : "|  " + loginResult, width / 2 - 24, height / 4 + 65, 0xC0C0C0);
		
		drawString(font, "Logged in as: §a" + minecraft.getSession().getUsername(), 4, height - 10, -1);
		
		userField.render(p_render_1_, p_render_2_, p_render_3_);
		passField.render(p_render_1_, p_render_2_, p_render_3_);
		checkBox.render(p_render_1_, p_render_2_, p_render_3_);
		
		int count = 10;
		int mX = p_render_1_; int mY = p_render_2_;
		for(String[] s: entries) {
			drawString(font, s[0] + "§f : §r***", width / 2 + 110, height / 4 + count, 
					mX>width / 2 + 110 && mX<width / 2 + 200 && mY>height / 4 + count && mY<height / 4 + count + 11 ? 0xE0E0E0 : 0xC0C0C0);
			count += 10;
		}
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public void writeEntry(String email_colon_pass) {
		fileMang.createFile("logins.txt");
		fileMang.appendFile(cipher.encrypt(email_colon_pass), "logins.txt");
	}
	
	public List<String[]> readEntries() {
		fileMang.createFile("logins.txt");
		List<String[]> entries = new ArrayList<>();
		for(String s: fileMang.readFileLines("logins.txt")) entries.add(cipher.decrypt(s).split(":"));
		return entries;
	}
	
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(userField.isFocused()) userField.charTyped(p_charTyped_1_, p_charTyped_2_);
		if(passField.isFocused()) passField.charTyped(p_charTyped_1_, p_charTyped_2_);
		
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}
	
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		double mX = p_mouseClicked_1_, mY = p_mouseClicked_3_;
		
		if(mX>userField.x && mX<userField.x+userField.getWidth() && mY>userField.y && mY<userField.y+20) {
			userField.changeFocus(true);
			if(passField.isFocused()) passField.changeFocus(true);
		}
		
		if(mX>passField.x && mX<passField.x+passField.getWidth() && mY>passField.y && mY<passField.y+20) {
			passField.changeFocus(true);
			if(userField.isFocused()) userField.changeFocus(true);
		}
		
		if(mX>checkBox.x && mX<checkBox.x+10 && mY>checkBox.y && mY<checkBox.y+10) {
			checkBox.checked = !checkBox.checked;
		}
		
		int count = 10;
		for(String[] s: entries) {
			if(mX>width / 2 + 110 && mX<width / 2 + 200 && mY>height / 4 + count && mY<height / 4 + count + 11) {
				userField.setText(s[0]);
				passField.setText(s[1]);
			}
			count += 10;
		}
		
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
	
	public void tick() {
		userField.tick();
		passField.tick();
	}
	
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if(userField.isFocused()) userField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if(passField.isFocused()) passField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

}
