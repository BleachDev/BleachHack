package bleach.hack.command.commands;

import org.apache.commons.lang3.StringUtils;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileHelper;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;

public class CmdFriends extends Command {

	@Override
	public String getAlias() {
		return "friends";
	}

	@Override
	public String getDescription() {
		return "Manage friends";
	}

	@Override
	public String getSyntax() {
		return "friends add [user] | friends remove [user] | friends list | friends clear";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args[0].equalsIgnoreCase("add")) {
			if (args.length < 2) {
				printSyntaxError("No username selected");
				return;
			}

			BleachHack.friendMang.add(args[1]);
			BleachLogger.infoMessage("Added \"" + args[1] + "\" to the friend list");
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length < 2) {
				printSyntaxError("No username selected");
				return;
			}

			BleachHack.friendMang.remove(args[1].toLowerCase());
			BleachLogger.infoMessage("Removed \"" + args[1] + "\" from the friend list");
		} else if (args[0].equalsIgnoreCase("list")) {
			MutableText text = new LiteralText(
					BleachHack.friendMang.getFriends().isEmpty() ? "You don't have any friends :(" : "Friends:");

			if (!BleachHack.friendMang.getFriends().isEmpty()) {
				int len = BleachHack.friendMang.getFriends().stream()
						.sorted((f1, f2) -> f2.length() - f1.length()).findFirst().get().length() + 3;

				for (String f : BleachHack.friendMang.getFriends()) {
					text = text.append("\n\u00a7b> " + f + StringUtils.repeat(' ', len - f.length()))
							.append(new LiteralText("\u00a7c[Del]").
									setStyle(Style.EMPTY.withHoverEvent(
											new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Remove " + f + " from your friendlist")))
											.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, PREFIX + "friends remove " + f)))
									.append("   ")
									.append(new LiteralText("\u00a73[NameMC]").
											setStyle(Style.EMPTY.withHoverEvent(
													new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Open NameMC page of " + f)))
													.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://namemc.com/profile/" + f)))));
				}
			}

			BleachLogger.infoMessage(text);
		} else if (args[0].equalsIgnoreCase("clear")) {
			BleachHack.friendMang.getFriends().clear();

			BleachLogger.infoMessage("Cleared Friend list");
		} else {
			printSyntaxError();
			return;
		}

		BleachFileHelper.SCHEDULE_SAVE_FRIENDS = true;
	}

}
