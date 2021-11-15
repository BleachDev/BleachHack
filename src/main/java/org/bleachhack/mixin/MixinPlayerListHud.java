package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.gui.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
