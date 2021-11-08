/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command;

import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import bleach.hack.gui.option.Option;
import bleach.hack.util.BleachLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public abstract class Command {

	/**
	 * A MinecraftClient instance
	 */
	protected final MinecraftClient mc = MinecraftClient.getInstance();

	private String[] aliases;
	private String description;
	private String syntax;
	private CommandCategory category;

	/**
	 * Instantiate a new command
	 * @param alias Alias
	 * @param desc Description
	 * @param syntax Syntax
	 * @param category Category
	 * @param moreAliases Other Aliases
	 */
	public Command(String alias, String desc, String syntax, CommandCategory category, String... moreAliases) {
		this.aliases = ArrayUtils.add(moreAliases, 0, alias);
		this.description = desc;
		this.syntax = syntax;
		this.category = category;
	}

	/**
	 * Gets commands' prefix
	 * @return Aliases
	 */
	public static final String getPrefix() {
		return Option.CHAT_COMMAND_PREFIX.getValue();
	}

	/**
	 * Gets aliases for the command
	 * @return Aliases
	 */
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Gets the description of the command
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the syntax of the command
	 * @return Syntax
	 */
	public String getSyntax() {
		return syntax;
	}

	/**
	 * Gets command's category
	 * @return Category
	 */
	public CommandCategory getCategory() {
		return category;
	}

	/**
	 * Checks if string matches an alias
	 * @param alias Alias
	 * @return Self-explanatory
	 */
	public boolean hasAlias(String alias) {
		return Stream.of(aliases).anyMatch(alias::equalsIgnoreCase);
	}

	/**
	 * Gets the tooltip for help
	 * @return Tooltip
	 */
	public Text getHelpTooltip() {
		return new LiteralText("\u00a77Category: " + getCategory() + "\n")
				.append("Aliases: \u00a7f" + getPrefix() + String.join(" \u00a77/\u00a7f " + getPrefix(), getAliases()) + "\n").styled(s -> s.withColor(BleachLogger.INFO_COLOR))
				.append("Usage: \u00a7f" + getSyntax() + "\n").styled(s -> s.withColor(BleachLogger.INFO_COLOR))
				.append("Description: \u00a7f" + getDescription()).styled(s -> s.withColor(BleachLogger.INFO_COLOR));
	}

	/**
	 * Main command method
	 * @param alias Alias used
	 * @param args Arguments
	 * @throws Exception
	 */
	public abstract void onCommand(String alias, String[] args) throws Exception;
}
