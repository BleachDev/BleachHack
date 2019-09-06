package bleach.hack.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.utils.BleachCipher;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class AltManagerScreen extends Screen {

	private ParticleManager particleMang = new ParticleManager();
	private BleachFileMang fileMang = new BleachFileMang();
	private BleachCipher cipher = new BleachCipher();
	
	private LoginScreen loginScreen;
	
	private List<List<String>> entries = new ArrayList<>();
	
	protected AltManagerScreen(LoginScreen loginScreen) {
		super(new LiteralText("Alt Manager"));
		this.loginScreen = loginScreen;
	}
	
	public void init() {
		fileMang.createFile("logins.txt");
		entries.clear();
		for(String s: fileMang.readFileLines("logins.txt")) entries.add(new ArrayList<>(Arrays.asList(cipher.decrypt(s).split(":"))));
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		renderBackground();
		fill(0, 0, width, height, 0xff000000);
		
		particleMang.addParticle(p_render_1_, p_render_2_);
		particleMang.renderParticles();
		
		drawString(font, "Logged in as: §a" + minecraft.getSession().getUsername(), 4, height - 10, -1);
		drawCenteredString(font, "§cTemprary™ alt manager", width / 2, height / 4 - 30, -1);
		drawCenteredString(font, "§cits broken, lets hope i can fix it before release", width / 2, height / 4 - 20, -1);
		
		int c = 0;
		for(List<String> e: entries) {
			String text = "§a" + e.get(0) + ":***";
			int lenght = minecraft.textRenderer.getStringWidth(text);
			
			fill(width / 2 - lenght / 2 - 1, height / 4 + c - 2, width / 2 + lenght / 2 + 1, height / 4 + c - 1, 0xFF303030);
			fill(width / 2 - lenght / 2 - 1, height / 4 + c + 9, width / 2 + lenght / 2 + 1, height / 4 + c + 10, 0xFF303030);
			fill(width / 2 - lenght / 2 - 2, height / 4 + c - 2, width / 2 - lenght / 2 - 1, height / 4 + c + 10, 0xFF303030);
			fill(width / 2 + lenght / 2 + 1, height / 4 + c - 2, width / 2 + lenght / 2 + 2, height / 4 + c + 10, 0xFF303030);
			drawCenteredString(font, "§cx", width / 2 + lenght / 2 + 9, height / 4 + c, -1);
			drawCenteredString(font, text, width / 2, height / 4 + c, -1);
			c += 14;
		}
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		double mX = p_mouseClicked_1_, mY = p_mouseClicked_3_;
		
		int c = 0;
		for(List<String> e: new ArrayList<>(entries)) {
			String text = "§a" + e.get(0) + ":***";
			int lenght = minecraft.textRenderer.getStringWidth(text);
			
			if(mX>width/2-lenght/2-1 && mX<width/2+lenght/2+1 && mY>height/4+c*14-2 && mY<height/4+c*14+11) {
				try{ loginScreen.userField.setText(e.get(0)); }catch(Exception e1) {}
				try{ loginScreen.passField.setText(e.get(1)); }catch(Exception e1) {}
				onClose();
			}
			if(mX>width/2+lenght/2+4 && mX<width/2+lenght/2+14 && mY>height/4+c*14-2 && mY<height/4+c*14+11) {
				int c1 = 0;
				String lines = "";
				for(String l: fileMang.readFileLines("logins.txt")) {
					if(l.trim().replace("\r", "").replace("\n", "") == "") continue;
					if (c1 != c) lines += l + "\r\n";
					c1++;
				}
				fileMang.createEmptyFile("logins.txt");
				fileMang.appendFile(lines, "logins.txt");
				minecraft.openScreen(new AltManagerScreen(loginScreen));
				break;
			}
			c++;
		}
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}
	
	public void onClose() {
		minecraft.openScreen(loginScreen);
	}
}
