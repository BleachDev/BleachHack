package bleach.hack.command.exception;

import net.minecraft.command.CommandException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class CmdSyntaxException extends CommandException {

	private static final long serialVersionUID = 7940377774005961331L;

	public CmdSyntaxException() {
        this("Invalid Syntax!");
    }

    public CmdSyntaxException(String message) {
        this(new LiteralText(message));
    }
    
    public CmdSyntaxException(Text message) {
        super(message);
    }

}
