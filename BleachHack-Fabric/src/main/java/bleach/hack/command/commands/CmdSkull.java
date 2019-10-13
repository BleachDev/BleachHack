package bleach.hack.command.commands;

import java.util.Base64;
import java.util.UUID;
import bleach.hack.command.Command;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;

public class CmdSkull extends Command {

	@Override
	public String getAlias() {
		return "skull";
	}

	@Override
	public String getDescription() {
		return "Gives you a player skull";
	}

	@Override
	public String getSyntax() {
		return ".skull [Player] | .skull img [Image url]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);
		
		if(args.length < 2) {
			item.setTag(StringNbtReader.parse("{SkullOwner:{Name:\"" + args[0] + "\"}}"));
		}else if(args[0].equalsIgnoreCase("img")) {
			item.setTag(StringNbtReader.parse("{SkullOwner:{Id:\"" + UUID.randomUUID() + "\",Properties:{textures:[{Value:\""
					+ Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + args[1] + "\"}}}").getBytes())
					+ "\"}]}}}"));
		}
		
		mc.player.inventory.addPickBlock(item);
	}

}
