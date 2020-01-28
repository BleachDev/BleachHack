package bleach.hack.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.gui.CleanUpScreen;
import bleach.hack.gui.ProtocolScreen;
import bleach.hack.gui.ServerScraperScreen;

@Mixin(MultiplayerScreen.class)
public class MixinServerScreen extends Screen {
	
	protected MixinServerScreen(Text text_1) {
		super(text_1);
	}

	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		addButton(new ButtonWidget(5, 7, 50, 18, "Scraper", button -> {
			minecraft.openScreen(new ServerScraperScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
		addButton(new ButtonWidget(58, 7, 50, 18, "Cleanup", button -> {
			minecraft.openScreen(new CleanUpScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
		addButton(new ButtonWidget(111, 7, 50, 18, "Protocol", button -> {
			minecraft.openScreen(new ProtocolScreen((MultiplayerScreen) minecraft.currentScreen));
		}));
	}
}
