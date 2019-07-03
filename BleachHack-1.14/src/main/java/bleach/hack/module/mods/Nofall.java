package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.network.play.client.CPlayerPacket;

public class Nofall extends Module{

	public Nofall() {
		super("Nofall", -1, Category.PLAYER, "Prevents you from taking fall damage.", null);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(mc.player.fallDistance > 2f) {
				mc.player.connection.sendPacket(new CPlayerPacket(true));
			}
		}
	}

}
