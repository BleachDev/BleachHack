package bleach.hack.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import bleach.hack.BleachHack;
import bleach.hack.gui.particle.ParticleManager;
import bleach.hack.utils.file.BleachGithubReader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class BleachMainMenu extends Screen {
	
	private ParticleManager particleMang = new ParticleManager();
	private BleachGithubReader github = new BleachGithubReader();
	private long time = 0;
	private boolean CREEPERAWWMAN;

	private List<String> versions = new ArrayList<>();
	
	public BleachMainMenu() {
		super(new TranslatableText("narrator.screen.title"));
	}
	
	public void init() {
		this.addButton(new ButtonWidget(width / 2 - 100, height / 4 + 48, 200, 20, I18n.translate("menu.singleplayer"), button -> {
			this.minecraft.openScreen(new SelectWorldScreen(this));
	    }));
		this.addButton(new ButtonWidget(width / 2 - 100, height / 4 + 72, 200, 20, I18n.translate("menu.multiplayer"), button -> {
	        this.minecraft.openScreen(new MultiplayerScreen(this));
	    }));
		this.addButton(new ButtonWidget(this.width / 2 - 100, height / 4 + 96, 98, 20, "Creeper?", button -> {
			time = System.currentTimeMillis();
			CREEPERAWWMAN = !CREEPERAWWMAN;
        }));
		this.addButton(new ButtonWidget(width / 2 + 2, height / 4 + 96, 98, 20, "Login Manager", button -> {
	        this.minecraft.openScreen(new LoginScreen());
	    }));
		this.addButton(new ButtonWidget(width / 2 - 100, height / 4 + 129, 98, 20, I18n.translate("menu.options"), button -> {
	        this.minecraft.openScreen(new SettingsScreen(this, this.minecraft.options));
	    }));
	    this.addButton(new ButtonWidget(width / 2 + 2, height / 4 + 129, 98, 20, I18n.translate("menu.quit"), button -> {
	    	this.minecraft.close();
	    }));
	    
	    versions.clear();
	    versions.addAll(github.readFileLines("latestversion.txt"));
	}
	
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground();
		
		if(CREEPERAWWMAN) {
			if(System.currentTimeMillis() - 40000 > time) creeperAwMan("creeper9.jpg", "I couldn't care to do all of these");
			else if(System.currentTimeMillis() - 32000 > time) creeperAwMan("creeper9.jpg", "Total shock fills your body, Oh no, it's you again");
			else if(System.currentTimeMillis() - 28000 > time) creeperAwMan("creeper8.jpg", "Heads up, You hear a sound, turn around and look up");
			else if(System.currentTimeMillis() - 22000 > time) creeperAwMan("creeper7.jpg", "Hope to find some diamonds tonight, night, night, Diamonds tonight");
			else if(System.currentTimeMillis() - 18000 > time) creeperAwMan("creeper6.jpg", "This task, a grueling one");
			else if(System.currentTimeMillis() - 14000 > time) creeperAwMan("creeper5.jpg", "Side-side to side");
			else if(System.currentTimeMillis() - 11000 > time) creeperAwMan("creeper4.jpg", "So we back in the mine got our pickaxe swinging from side to side");
			else if(System.currentTimeMillis() - 8000 > time) creeperAwMan("creeper3.jpg", "AWW MAN");
			else if(System.currentTimeMillis() - 3000 > time) creeperAwMan("creeper2.jpg", "CREEPER!");
			else {creeperAwMan("creeper1.jpg", ""); }
			drawRightAlignedString(font, "§cMusic removed because of copyright stuff", width, 2, -1);
		}else{
			fill(0, 0, width, height, 0xff000000);
		}
		
		particleMang.addParticle(p_render_1_, p_render_2_);
		particleMang.renderParticles();
		
		GL11.glScaled(3, 3, 3);
		drawString(this.font, "BleachHack", (width/2 - 81)/3, (height/4 - 15)/3, 0xffc0e0);
		GL11.glScaled(1d/3d, 1d/3d, 1d/3d);
		
		GL11.glScaled(1.5, 1.5, 1.5);
		drawCenteredString(this.font, BleachHack.VERSION, (int)((width/2)/1.5), (int)((height/4 + 8)/1.5), 0xffc050);
		GL11.glScaled(1d/1.5d, 1d/1.5d, 1d/1.5d);
		
		int copyWidth = this.font.getStringWidth("Copyright Mojang AB. Do not distribute!") + 2;
		
		drawString(this.font, "Copyright Mojang AB. Do not distribute!", width - copyWidth, height - 10, -1);
		drawString(this.font, "Fabric: " + "???", 4, height - 30, -1);
		drawString(this.font, "Minecraft " + SharedConstants.getGameVersion().getName(), 4, height - 20, -1);
		drawString(this.font, "Logged in as: §a" + minecraft.getSession().getUsername(), 4, height - 10, -1);
		
		try {
			if(Integer.parseInt(versions.get(1)) > BleachHack.INTVERSION) {
				drawCenteredString(this.font, "§cOutdated BleachHack Version!", width/2, 2, -1);
				drawCenteredString(this.font,"§4[" + versions.get(0) + " > " + BleachHack.VERSION + "]", width/2, 11, -1);
			}
		}catch(Exception e) {}
		
		super.render(p_render_1_, p_render_2_, p_render_3_);
	}
	
	public void creeperAwMan(String path, String text) {
		minecraft.getTextureManager().bindTexture(new Identifier("bleachhack", path));
		blit(0, 0, 0, 0, width, height, width, height);
		drawCenteredString(this.font, text, width/2, height/4 + 25, 0x50b050);
	}
}