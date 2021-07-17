/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;

public class CmdWatermark extends Command {

	public CmdWatermark() {
		super("watermark", "Sets the client watermark.", "watermark reset [color/text] | watermark text <text> | watermark color <color 1> <color 2>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("reset")) {
			if (args.length == 1) {
				BleachHack.watermark.reset(true, true);
				BleachLogger.infoMessage("Reset the watermark text and colors!");
			} else if (args[1].equalsIgnoreCase("color")) {
				BleachHack.watermark.reset(false, true);
				BleachLogger.infoMessage("Reset the watermark colors!");
			} else if (args[1].equalsIgnoreCase("text")) {
				BleachHack.watermark.reset(true, false);
				BleachLogger.infoMessage("Reset the watermark text!");
			} else {
				throw new CmdSyntaxException();
			}
		} else {
			if (args.length == 1) {
				throw new CmdSyntaxException();
			}
			
			if (args[0].equalsIgnoreCase("text")) {
				if (args.length > 3) {
					throw new CmdSyntaxException("The watermark can't contain more than 2 words.");
				}
				
				if ((args.length == 2 && args[1].length() < 2) || (args.length == 3 && args[1].length() < 1 && args[2].length() < 1)) {
					throw new CmdSyntaxException("The watermark can't be less than 2 characters long.");
				}
				
				BleachHack.watermark.setStrings(args[1], args.length == 3 ? args[2] : null);
				BleachLogger.infoMessage("Set the watermark to " + BleachHack.watermark.getText());
			} else if (args[0].equalsIgnoreCase("color")) {
				if (args.length > 3) {
					throw new CmdSyntaxException("The watermark can't contain more than 2 colors.");
				}

				BleachHack.watermark.setColor(
						Integer.parseInt(args[1].replace("x", "").replace("#", ""), 16),
						args.length == 3 ? Integer.parseInt(args[2].replace("x", "").replace("#", ""), 16) : BleachHack.watermark.getColor2());
				BleachLogger.infoMessage("Set the watermark to " + BleachHack.watermark.getText());
			} else {
				throw new CmdSyntaxException();
			}
		}
	}

}
