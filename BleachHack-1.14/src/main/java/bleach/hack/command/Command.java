package bleach.hack.command;

public abstract class Command {

	public abstract String getAlias();
	public abstract String getDescription();
	public abstract String getSyntax();
	public abstract void onCommand(String command,String[] args) throws Exception;
	
}
