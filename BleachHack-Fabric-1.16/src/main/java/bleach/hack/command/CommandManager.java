/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public class CommandManager {

	public static boolean allowNextMsg = false;

	private static final Gson commandGson = new Gson();
	private static final Map<String, Command> commands = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static CommandSuggestionProvider suggestionProvider = new CommandSuggestionProvider();

	public static Map<String, Command> getCommandMap() {
		return commands;
	}

	public static Iterable<Command> getCommands() {
		return commands.values();
	}

	public static CommandSuggestionProvider getSuggestionProvider() {
		return suggestionProvider;
	}

	public static void loadCommands(InputStream jsonInputStream) {
		CommandListJson json = commandGson.fromJson(new InputStreamReader(jsonInputStream), CommandListJson.class);

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
	}

	public static void loadCommand(Command command) {
		if (commands.containsValue(command)) {
			BleachLogger.logger.error("Failed to load module %s: a module with this name is already loaded.", command.getAliases()[0]);
		} else {
			commands.put(command.getAliases()[0], command);

			try {
				suggestionProvider.addSuggestion(command.getSyntax());
			} catch (Exception e) {
				BleachLogger.logger.error(String.format("Error trying to load suggestsions for $%s (Syntax: %s)", command.getAliases()[0], command.getSyntax()) , e);
				suggestionProvider.addSuggestion(command.getAliases()[0]);
			}
		}
	}

	public static void readPrefix() {
		JsonElement prefix = BleachFileHelper.readMiscSetting("prefix");

		if (prefix != null) {
			Command.PREFIX = prefix.getAsString();
		}
	}

	public static void callCommand(String input) {
		String[] split = input.split(" ");
		BleachLogger.logger.info("Running command: " + Arrays.toString(split));

		for (Command c : getCommands()) {
			if (c.hasAlias(split[0])) {
				try {
					c.onCommand(split[0], ArrayUtils.subarray(split, 1, split.length));
				} catch (CmdSyntaxException e) {
					BleachLogger.errorMessage((MutableText) e.getTextMessage());

					MutableText text = new LiteralText("\u00a7b" + Command.PREFIX + c.getAliases()[0] + " - \u00a7f" + c.getDescription());

					BleachLogger.infoMessage(
							text.styled(style -> style
									.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c.getHelpTooltip()))));
				} catch (Exception e) {
					e.printStackTrace();

					BleachLogger.errorMessage("\u00a7l> " + e.getClass().getSimpleName());
					BleachLogger.errorMessage("\u00a7l> \u00a7c" + e.getMessage());

					int i = 0;
					for (StackTraceElement st: e.getStackTrace()) {
						if (i >= 8) break;

						String[] bruh = st.getClassName().split("\\.");
						BleachLogger.errorMessage(bruh[bruh.length - 1] + "." + st.getMethodName() + "():" + st.getLineNumber());
						i++;
					}
				}

				return;
			}
		}

		BleachLogger.errorMessage("Command Not Found, Maybe Try " + Command.PREFIX + "help");
	}

	private class CommandListJson {

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
