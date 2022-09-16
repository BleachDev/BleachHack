package org.bleachhack.command.commands;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.module.mods.SignRestorer;
import org.bleachhack.util.BleachLogger;

import java.io.IOException;

public class CmdReloadSigns extends Command {

    public CmdReloadSigns() {
        super("reloadsigns", "Reloads signs for SignRestorer", "reloadsigns", CommandCategory.MODULES,
                "signsreload");
    }

    @Override
    public void onCommand(String command, String[] args) {
        try {
            SignRestorer.reloadSignData();
            BleachLogger.info("SignRestorer data reloaded!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
