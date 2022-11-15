/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command;

import net.minecraft.client.MinecraftClient;

import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.setting.option.Option;
import org.bleachhack.util.BleachLogger;

import java.util.stream.Stream;

public abstract class Command {

	protected final MinecraftClient mc = MinecraftClient.getInstance();

	private String[] aliases;
	private String description;
	private String syntax;
	private CommandCategory category;

	public Command(String alias, String desc, String syntax, CommandCategory category, String... moreAliases) {
		this.aliases = ArrayUtils.addFirst(moreAliases, alias);
		this.description = desc;
		this.syntax = syntax;
		this.category = category;
	}

	public static String getPrefix() {
		return Option.CHAT_COMMAND_PREFIX.getValue();
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
		return Stream.of(aliases).anyMatch(alias::equalsIgnoreCase);
	}

	public Text getHelpTooltip() {
		return Text.literal("\u00a77Category: " + getCategory() + "\n")
				.append("Aliases: \u00a7f" + getPrefix() + String.join(" \u00a77/\u00a7f " + getPrefix(), getAliases()) + "\n").styled(s -> s.withColor(BleachLogger.INFO_COLOR))
				.append("Usage: \u00a7f" + getSyntax() + "\n").styled(s -> s.withColor(BleachLogger.INFO_COLOR))
				.append("Description: \u00a7f" + getDescription()).styled(s -> s.withColor(BleachLogger.INFO_COLOR));
	}

	public abstract void onCommand(String alias, String[] args) throws Exception;
}
