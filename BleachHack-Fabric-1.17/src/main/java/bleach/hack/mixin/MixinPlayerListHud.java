package bleach.hack.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Ordering;

import bleach.hack.BleachHack;
import bleach.hack.gui.window.Window;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {

	@Shadow private static Ordering<PlayerListEntry> ENTRY_ORDERING;
	@Shadow private MinecraftClient client;

	@Unique private List<PlayerListEntry> players;
	@Unique private int currentPlayer;

	@Inject(method = "render", at = @At("HEAD"))
	public void render(MatrixStack matrices, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo callback) {
		currentPlayer = 0;
		players = ENTRY_ORDERING.sortedCopy(client.player.networkHandler.getPlayerList());
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 2))
	public void render_fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
		PlayerListEntry entry = players.size() > currentPlayer ? players.get(currentPlayer) : null;

		if (entry != null && BleachHack.playerMang.getPlayers().contains(entry.getProfile().getId())) {
			Window.horizontalGradient(matrices, x1, y1, x2, y2, 0x50e4bf47, 0x60ebafcc);
		} else {
			DrawableHelper.fill(matrices, x1, y1, x2, y2, color);
		}

		currentPlayer++;
	}
}
