package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(BeaconScreen.class)
public abstract class MixinBeaconScreen extends HandledScreen<BeaconScreenHandler> {
	
	private boolean unlocked = false;
	
	public MixinBeaconScreen(BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	
	@Inject(method = "init", at = @At("RETURN"))
	protected void init(CallbackInfo ci) {
		addButton(new ButtonWidget((width - backgroundWidth) / 2 + 2, (height - backgroundHeight) / 2 - 15, 46, 14, new LiteralText("Unlock"), button -> unlocked = true));
	}
	
	@Inject(method = "tick", at = @At("RETURN"))
	public void tick(CallbackInfo ci) {
		if (unlocked) {
			for (AbstractButtonWidget b: buttons) {
				b.active = true;
			}
		}
	}
}
