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
package bleach.hack.command.commands;

import java.util.Locale;

import bleach.hack.command.Command;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.BleachLogger;
import net.minecraft.client.util.InputUtil;

public class CmdBind extends Command {

	@Override
	public String getAlias() {
		return "bind";
	}

	@Override
	public String getDescription() {
		return "Binds a module";
	}

	@Override
	public String getSyntax() {
		return "bind set <Module> <Key> | bind del <Module> | bind clear";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		if (args[0].equalsIgnoreCase("clear")) {
			int c = 0;
			for (Module m : ModuleManager.getModules()) {
				if (m.getKey() != Module.KEY_UNBOUND) {
					m.setKey(Module.KEY_UNBOUND);
					c++;
				}
			}

			BleachLogger.infoMessage("Cleared " + c + " Binds");
		} else if (args.length >= 2 && (args.length >= 3 || !args[1].equalsIgnoreCase("set"))) {
			for (Module m : ModuleManager.getModules()) {
				if (m.getName().equalsIgnoreCase(args[1])) {
					if (args[0].equalsIgnoreCase("set")) {
						int key = -1;

						// Special cases for rshift/rcontrol and that shit
						try {
							key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH)).getCode();
						} catch (IllegalArgumentException e) {
							if (args[2].toLowerCase(Locale.ENGLISH).startsWith("right")) {
								try {
									key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("right", "right.")).getCode();
								} catch (IllegalArgumentException e1) {
									printSyntaxError("Unknown key: " + args[2] + " / " + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("right", "right."));
									return;
								}
							} else if (args[2].toLowerCase(Locale.ENGLISH).startsWith("r")) {
								try {
									key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("r", "right.")).getCode();
								} catch (IllegalArgumentException e1) {
									printSyntaxError("Unknown key: " + args[2] + " / " + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("r", "right."));
									return;
								}
							} else {
								printSyntaxError("Unknown key: " + args[2]);
								return;
							}
						}

						m.setKey(key);
						BleachLogger.infoMessage("Bound " + m.getName() + " To " + args[2] + " (KEY" + key + ")");
					} else if (args[0].equalsIgnoreCase("del")) {
						m.setKey(Module.KEY_UNBOUND);
						BleachLogger.infoMessage("Removed Bind For " + m.getName());
					}

					return;
				}
			}

			printSyntaxError("Could Not Find Module \"" + args[1] + "\"");
		} else {
			printSyntaxError();
		}
	}

}
