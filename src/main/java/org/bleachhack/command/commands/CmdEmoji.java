/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;

import org.bleachhack.command.exception.CmdSyntaxException;

public class CmdEmoji extends Command {

    public CmdEmoji() {
        super("emoji", "Sends emoji to chat.", "emoji shrug | emoji smirk | emoji angry | emoji wtf | emoji bruh | emoji dealwithit | emoji scared | emoji happy | emoji yay | emoji blush | emoji vibe | emoji wink |  emoji doggo | emoji kitty | emoji kiss |  emoji party | emoji tableflip | emoji angryflip | emoji dontflip | emoji unflip | emoji zombie", CommandCategory.MISC,
                "e");
    }

    @Override
    public void onCommand(String alias, String[] args) throws Exception {
        if (args.length == 0) {
            throw new CmdSyntaxException();
        }

        if (args[0].equalsIgnoreCase("shrug")) {
            mc.player.sendChatMessage("¯\\_(ツ)_/¯", null);
        } else if (args[0].equalsIgnoreCase("smirk")) {
            mc.player.sendChatMessage("(´°‿ʖ´°)", null);
        } else if (args[0].equalsIgnoreCase("angry")) {
            mc.player.sendChatMessage("(ಠ益ಠ)", null);
        } else if (args[0].equalsIgnoreCase("wtf")) {
            mc.player.sendChatMessage("(゜-゜)", null);
        } else if (args[0].equalsIgnoreCase("bruh")) {
            mc.player.sendChatMessage("(¬_¬)", null);
        } else if (args[0].equalsIgnoreCase("dealwithit")) {
            mc.player.sendChatMessage("(▀ˉL▀ˉ)", null);
        } else if (args[0].equalsIgnoreCase("scared")) {
            mc.player.sendChatMessage("(ಠ_ಠ)", null);
        } else if (args[0].equalsIgnoreCase("happy")) {
            mc.player.sendChatMessage("(ᵔ ‿ʖ ᵔ)", null);
        } else if (args[0].equalsIgnoreCase("yay")) {
            mc.player.sendChatMessage("(｡◕‿◕｡)", null);
        } else if (args[0].equalsIgnoreCase("blush")) {
            mc.player.sendChatMessage("(✿◠‿◠)", null);
        } else if (args[0].equalsIgnoreCase("vibe")) {
            mc.player.sendChatMessage("~(˘▾˘~)", null);
        } else if (args[0].equalsIgnoreCase("wink")) {
            mc.player.sendChatMessage("(◕‿↼)", null);
        } else if (args[0].equalsIgnoreCase("doggo")) {
            mc.player.sendChatMessage("(ᵔᴥᵔ)", null);
        } else if (args[0].equalsIgnoreCase("kitty")) {
            mc.player.sendChatMessage("(^•ﻌ•^)", null);
        } else if (args[0].equalsIgnoreCase("kiss")) {
            mc.player.sendChatMessage("(￣3￣)", null);
        } else if (args[0].equalsIgnoreCase("party")) {
            mc.player.sendChatMessage("♪ ┗( ･o･)┓ ♪", null);
        } else if (args[0].equalsIgnoreCase("tableflip")) {
            mc.player.sendChatMessage("(╯°□°）╯︵ ┻━┻", null);
        } else if (args[0].equalsIgnoreCase("angryflip")) {
            mc.player.sendChatMessage("┻━┻ ︵ヽ(`Д´)ﾉ︵ ┻━┻", null);
        } else if (args[0].equalsIgnoreCase("dontflip")) {
            mc.player.sendChatMessage("┳━┳ ¯\\_(ツ)", null);
        } else if (args[0].equalsIgnoreCase("unflip")) {
            mc.player.sendChatMessage("┳━┳ ノ(°-°ノ)", null);
        } else if (args[0].equalsIgnoreCase("zombie")) {
            mc.player.sendChatMessage("[¬°-°]¬", null);
        } else {
            throw new CmdSyntaxException();
        }
    }

}
