/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command;

import org.apache.commons.lang3.ArrayUtils;

import bleach.hack.util.BleachLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public abstract class Command {

	public static String PREFIX = "$";

	protected MinecraftClient mc = MinecraftClient.getInstance();

	private String[] aliases;
	private String description;
	private String syntax;
	private CommandCategory category;

	public Command(String alias, String desc, String syntax, CommandCategory category, String... moreAliases) {
		this.aliases = ArrayUtils.add(moreAliases, 0, alias);
		this.description = desc;
		this.syntax = syntax;
		this.category = category;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getDescription() {
		return description;
	}

	public String getSyntax() {
		return syntax;
	}

	public CommandCategory getCategory() {
		return category;
	}

	public boolean hasAlias(String alias) {
		for (String a: aliases) {
			if (alias.equalsIgnoreCase(a)) {
				return true;
			}
		}

		return false;
	}

	public void printSyntaxError() {
		printSyntaxError("Invalid Syntax!");
	}

	public void printSyntaxError(String reason) {
		BleachLogger.errorMessage(reason);

		MutableText text = new LiteralText("\u00a7b" + PREFIX + getAliases()[0] + " - \u00a7f" + getDescription());
		Text tooltip = new LiteralText(
				"\u00a72Category: " + getCategory()
				+ "\n\u00a7bAliases: \u00a7f" + PREFIX + String.join(" \u00a77/\u00a7f " + PREFIX, getAliases())
				+ "\n\u00a7bUsage: \u00a7f" + getSyntax()
				+ "\n\u00a7bDesc: \u00a7f" + getDescription());

		BleachLogger.infoMessage(
				text.styled(style -> style
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
	}

	public abstract void onCommand(String alias, String[] args) throws Exception;
}
