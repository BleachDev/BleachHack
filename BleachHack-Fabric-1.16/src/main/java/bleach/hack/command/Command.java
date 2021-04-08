/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.command;

import bleach.hack.util.BleachLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public abstract class Command {

	public static String PREFIX = "$";

	protected MinecraftClient mc = MinecraftClient.getInstance();

	public abstract String getAlias();

	public abstract String getDescription();

	public abstract String getSyntax();

	public void printSyntaxError() {
		printSyntaxError("Invalid Syntax!");
	}

	public void printSyntaxError(String reason) {
		BleachLogger.errorMessage(reason);

		MutableText text = new LiteralText("\u00a73" + PREFIX + getAlias() + " -> \u00a7b" + getSyntax());
		Text tooltip = new LiteralText("\u00a7b" + PREFIX + getAlias() + "\n\u00a73" + getSyntax() + "\n\u00a7b" + getDescription());

		BleachLogger.infoMessage(
				text.styled(style -> style
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
	}

	public abstract void onCommand(String command, String[] args) throws Exception;
}
