package bleach.hack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.gui.BleachMainMenu;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
	
	protected MixinTitleScreen(Text text_1) {
		super(text_1);
	}
	
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		if(BleachMainMenu.customTitleScreen) {
			MinecraftClient.getInstance().openScreen(new BleachMainMenu());
		}else {
			addButton(new ButtonWidget(width / 2 - 124, height / 4 + 96, 20, 20, "BH", button -> {
				BleachMainMenu.customTitleScreen = !BleachMainMenu.customTitleScreen;
				minecraft.openScreen(new TitleScreen(false));
			}));
		}
	}
}
