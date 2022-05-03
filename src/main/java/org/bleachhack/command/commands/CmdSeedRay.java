package org.bleachhack.command.commands;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.SeedRay;

import net.minecraft.text.LiteralText;

public class CmdSeedRay extends Command {

	public CmdSeedRay() {
		super("seedray", "Sets the seedray world seed", "seedray <seed>", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args[0].isEmpty()) {
			throw new CmdSyntaxException("Seed Cannot Be Empty");
		}
		
		SeedRay seedRay = (SeedRay) ModuleManager.getModule("SeedRay");
		seedRay.worldSeed = Long.valueOf(args[0]);
		 mc.inGameHud.getChatHud().addMessage(new LiteralText("Set SeedRay seed to: " + args[0]));
	}

}
