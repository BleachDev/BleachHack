package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class CmdTeleport extends Command {

	public static Thread nWordThread;
	
	public String getAlias() {
		return "tp";
	}

	public String getDescription() {
		return "Auto moves you to the specified block";
	}

	public String getSyntax() {
		return ".tp x y z bps | .tp stop";
	}

	@SuppressWarnings("deprecation")
	public void onCommand(String command, String[] args) throws Exception {
		if (args[0].equalsIgnoreCase("stop")) {
			if (nWordThread != null && nWordThread.isAlive()) {
				nWordThread.stop(); // fuck it, stop moment
				nWordThread = null;
			}
		} else {
			if (args.length != 4) {
				BleachLogger.errorMessage("Invalid coords!");
				BleachLogger.infoMessage(getSyntax());
				return;
			}
			
			try {
				nWordThread = new Thread(() -> {
					try {
						double x = Double.parseDouble(args[0]);
						double y = Double.parseDouble(args[1]);
						double z = Double.parseDouble(args[2]);
						double bps = Double.parseDouble(args[3]);
						
						int totalTime = (int) ((Math.abs(mc.player.getPos().x - x)
								+ Math.abs(mc.player.getPos().y - y) + Math.abs(mc.player.getPos().z - z)) / bps);
						Vec3d moveVec = new Vec3d((x - mc.player.getPos().x) / totalTime,
								(y - mc.player.getPos().y) / totalTime, (z - mc.player.getPos().z) / totalTime);
						int tick = 0;
						
						while (tick <= totalTime) {
								Thread.sleep(50);
								Vec3d newPos = mc.player.getPos().add(moveVec);
								//mc.player.setPos(newPos.x, newPos.y, newPos.z);
								//mc.player.setPos(newPos.x, newPos.y, newPos.z);
								mc.player.setVelocity(0, 0, 0);
								mc.player.updatePosition(newPos.x, newPos.y, newPos.z);
								mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(newPos.x, newPos.y, newPos.z, false));
								tick++;
						}
					} catch (Exception e) {
						System.out.println("Exception teleporting");
					}
					
				});
				
				nWordThread.start();
			} catch (Exception e) {
				
			}
		}
	}

}
