package bleach.hack.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.ContainerProvider;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Peek;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen<T extends Container> extends Screen implements ContainerProvider<T> {

	public MixinContainerScreen(Container container_1, PlayerInventory playerInventory_1, Text text_1) {
		super(text_1);
	}

	@Inject(at = @At("RETURN"), method = "render(IIF)V")
	public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
		if(ModuleManager.getModule(Peek.class).isToggled()) {
			((Peek) ModuleManager.getModule(Peek.class)).drawTooltip(int_1, int_2);
		}
	}
}
