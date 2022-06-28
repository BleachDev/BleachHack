package org.bleachhack.command.exception;

import net.minecraft.command.CommandException;

import net.minecraft.text.Text;

import java.io.Serial;

public class CmdSyntaxException extends CommandException {

	@Serial
	private static final long serialVersionUID = 7940377774005961331L;

	public CmdSyntaxException() {
        this("Invalid Syntax!");
    }

    public CmdSyntaxException(String message) {
        this(Text.literal(message));
    }
    
    public CmdSyntaxException(Text message) {
        super(message);
    }

}
