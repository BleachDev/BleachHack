/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.client.util.InputUtil;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.util.BleachLogger;

import java.util.Locale;

public class CmdBind extends Command {

	public CmdBind() {
		super("bind", "Binds a module.", "bind set <module> <key> | bind del <module> | bind clear", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("clear")) {
			int c = 0;
			for (Module m : ModuleManager.getModules()) {
				if (m.getKey() != Module.KEY_UNBOUND) {
					m.setKey(Module.KEY_UNBOUND);
					c++;
				}
			}

			BleachLogger.info("Cleared " + c + " Binds");
		} else if (args.length >= 2 && (args.length >= 3 || !args[1].equalsIgnoreCase("set"))) {
			for (Module m : ModuleManager.getModules()) {
				if (m.getName().equalsIgnoreCase(args[1])) {
					if (args[0].equalsIgnoreCase("set")) {
						int key = Module.KEY_UNBOUND;

						// Special cases for rshift/rcontrol and that shit
						try {
							key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH)).getCode();
						} catch (IllegalArgumentException e) {
							if (args[2].toLowerCase(Locale.ENGLISH).startsWith("right")) {
								try {
									key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("right", "right.")).getCode();
								} catch (IllegalArgumentException e1) {
									throw new CmdSyntaxException("Unknown key: " + args[2] + " / " + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("right", "right."));
								}
							} else if (args[2].toLowerCase(Locale.ENGLISH).startsWith("r")) {
								try {
									key = InputUtil.fromTranslationKey("key.keyboard." + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("r", "right.")).getCode();
								} catch (IllegalArgumentException e1) {
									throw new CmdSyntaxException("Unknown key: " + args[2] + " / " + args[2].toLowerCase(Locale.ENGLISH).replaceFirst("r", "right."));
								}
							} else {
								throw new CmdSyntaxException("Unknown key: " + args[2]);
							}
						}

						m.setKey(key);
						BleachLogger.info("Bound " + m.getName() + " To " + args[2] + " (KEY" + key + ")");
					} else if (args[0].equalsIgnoreCase("del")) {
						m.setKey(Module.KEY_UNBOUND);
						BleachLogger.info("Removed Bind For " + m.getName());
					}

					return;
				}
			}

			throw new CmdSyntaxException("Could Not Find Module \"" + args[1] + "\"");
		} else {
			throw new CmdSyntaxException();
		}
	}

}
