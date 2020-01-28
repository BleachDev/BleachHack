package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class Augh extends Module {

	public static Identifier AUGH_ID = new Identifier("bleachhack:augh");
	public static SoundEvent AUGH_EVENT = new SoundEvent(AUGH_ID);
	
	public Augh() {
		super("ÆÜGH", -1, Category.MISC, "rated best hack 2019 by ign");
	}

}
