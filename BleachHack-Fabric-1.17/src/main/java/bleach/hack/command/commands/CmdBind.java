/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.Locale;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.BleachLogger;
import net.minecraft.client.util.InputUtil;

public class CmdBind extends Command {

	public CmdBind() {
		super("bind", "Binds a module.", "bind set <Module> <Key> | bind del <Module> | bind clear", CommandCategory.MODULES);
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
						BleachLogger.infoMessage("Bound " + m.getName() + " To " + args[2] + " (KEY" + key + ")");
					} else if (args[0].equalsIgnoreCase("del")) {
						m.setKey(Module.KEY_UNBOUND);
						BleachLogger.infoMessage("Removed Bind For " + m.getName());
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
