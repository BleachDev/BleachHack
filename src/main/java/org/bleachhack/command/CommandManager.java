/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command;

import com.google.gson.annotations.SerializedName;
import net.minecraft.text.HoverEvent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.collections.NameableStorage;
import org.bleachhack.util.io.BleachJsonHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class CommandManager {

	public static boolean allowNextMsg = false;

	private static final NameableStorage<Command> COMMANDS = new NameableStorage<Command>(c -> c.getAliases()[0]);
	private static CommandSuggestionProvider suggestionProvider = new CommandSuggestionProvider();

	public static CommandSuggestionProvider getSuggestionProvider() {
		return suggestionProvider;
	}

	public static void loadCommands(InputStream jsonInputStream) {
		InputStreamReader inputReader = new InputStreamReader(jsonInputStream, StandardCharsets.UTF_8);

		try {
			CommandListJson json = BleachJsonHelper.GSON.fromJson(inputReader, CommandListJson.class);

			for (String commandString : json.getCommands()) {
				try {
					Class<?> commandClass = Class.forName(String.format("%s.%s", json.getPackage(), commandString));

					if (Command.class.isAssignableFrom(commandClass)) {
						try {
							Command command = (Command) commandClass.getConstructor().newInstance();

							loadCommand(command);
						} catch (Exception exception) {
							BleachLogger.logger.error("Failed to load command %s: could not instantiate.", commandClass);
							exception.printStackTrace();
						}
					} else {
						BleachLogger.logger.error("Failed to load command %s: not a descendant of Command.", commandClass);
					}
				} catch (Exception exception) {
					BleachLogger.logger.error("Failed to load command %s.", commandString);
					exception.printStackTrace();
				}
			}
		} finally {
			IOUtils.closeQuietly(inputReader);
		}
	}

	public static void loadCommand(Command command) {
		if (!COMMANDS.add(command)) {
			BleachLogger.logger.error("Failed to load module %s: a module with this name is already loaded.", command.getAliases()[0]);
			return;
		}

		COMMANDS.add(command);

		try {
			suggestionProvider.addSuggestion(command.getSyntax());
		} catch (Exception e) {
			BleachLogger.logger.error(String.format("Error trying to load suggestsions for $%s (Syntax: %s)", command.getAliases()[0], command.getSyntax()) , e);
			suggestionProvider.addSuggestion(command.getAliases()[0]);
		}
	}
	
	public static Iterable<Command> getCommands() {
		return COMMANDS.values();
	}

	/*public static Command getCommand(String name) {
		return COMMANDS.get(name);
	}*/
	
	public static <C extends Command> C getCommand(Class<C> class_) {
		return COMMANDS.get(class_);
	}

	public static void callCommand(String input) {
		String[] split = input.split(" ");
		BleachLogger.logger.info("Running command: " + Arrays.toString(split));

		for (Command c : getCommands()) {
			if (c.hasAlias(split[0])) {
				try {
					c.onCommand(split[0], ArrayUtils.subarray(split, 1, split.length));
				} catch (CmdSyntaxException e) {
					BleachLogger.error(e.getTextMessage());

					MutableText text = Text.literal(Command.getPrefix() + c.getAliases()[0] + ": \u00a7f" + c.getDescription())
							.styled(s -> s.withColor(BleachLogger.INFO_COLOR));

					BleachLogger.info(
							text.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c.getHelpTooltip()))));
				} catch (Exception e) {
					e.printStackTrace();

					BleachLogger.error("\u00a7l> " + e.getClass().getSimpleName());
					BleachLogger.error("\u00a7l> \u00a7c" + e.getMessage());

					int i = 0;
					for (StackTraceElement st: e.getStackTrace()) {
						if (i >= 8) break;

						String[] bruh = st.getClassName().split("\\.");
						BleachLogger.error(bruh[bruh.length - 1] + "." + st.getMethodName() + "():" + st.getLineNumber());
						i++;
					}
				}

				return;
			}
		}

		BleachLogger.error("Command Not Found, Maybe Try " + Command.getPrefix() + "help");
	}

	private static class CommandListJson {

		@SerializedName("package")
		private String packageName;

		@SerializedName("commands")
		private List<String> commands;

		public String getPackage() {
			return this.packageName;
		}

		public List<String> getCommands() {
			return this.commands;
		}
	}
}
