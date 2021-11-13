package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.BleachHack;
import bleach.hack.gui.option.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

	@Shadow private MinecraftClient client;

	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	public void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> callback) {
		if (Option.PLAYERLIST_SHOW_FRIENDS.getValue() && BleachHack.friendMang.has(entry.getProfile().getName())) {
			callback.setReturnValue(((MutableText) callback.getReturnValue()).styled(s -> s.withColor(Formatting.AQUA)));
		}
		else if (Option.PLAYERLIST_SHOW_SELF.getValue() && entry.getProfile().getId().toString().equals(MinecraftClient.getInstance().player.getGameProfile().getId().toString())) {
			callback.setReturnValue(((MutableText) callback.getReturnValue()).styled(s -> s.withColor(Formatting.GOLD)));
		}
		else if (Option.PLAYERLIST_SHOW_PING.getValue()) {
			MinecraftClient.getInstance().player.networkHandler.getPlayerListEntry(entry.getProfile().getName()).setDisplayName(Text.of("\u00A7f" + entry.getProfile().getName() + " \u00A77[\u00A7f" + entry.getLatency()+"ms\u00A77]"));
		}
	}

	@Inject(method = "renderLatencyIcon", at = @At("RETURN"))
	public void renderLatencyIcon(MatrixStack matrices, int width, int x, int y, PlayerListEntry entry, CallbackInfo callback) {
		if (Option.PLAYERLIST_SHOW_BH_USERS.getValue() && BleachHack.playerMang.getPlayers().contains(entry.getProfile().getId())) {
			matrices.push();
			matrices.translate(x + width - 21, y + 1.5, 0);
			matrices.scale(0.67f, 0.7f, 1f);
			client.textRenderer.drawWithShadow(matrices, BleachHack.watermark.getShortText(), 0, 0, -1);
			matrices.pop();
		}
	}
}
