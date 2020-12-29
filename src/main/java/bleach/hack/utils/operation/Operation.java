package bleach.hack.utils.operation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public abstract class Operation {

	protected static MinecraftClient mc = MinecraftClient.getInstance();

	public BlockPos pos;
	public abstract boolean canExecute();
	public abstract boolean execute();
	public abstract boolean verify();
}
