package bleach.hack.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.module.mods.*;
import net.minecraft.client.MinecraftClient;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList(
			new AutoTotem(),
			new ChestESP(),
			new ClickGui(),
			new CrystalAura(),
			new ElytraFly(),
			new EntitySpeed(),
			new ESP(),
			new FastUse(),
			new Flight(),
			new Freecam(),
			new Fullbright(),
			new Jesus(),
			new Killaura(),
			new Nametags(),
			new Nofall(),
			new NoSlow(),
			new Notebot(),
			new OffhandCrash(),
			new PacketFly(),
			new Peek(),
			new Scaffold(),
			new Spammer(),
			new Speed(),
			new SpeedMine(),
			new Sprint(),
			new StarGithubPls(),
			new Step(),
			new Tracers(),
			new UI());
	
	public static List<Module> getModules() {
		return mods;
	}
	
	public static Module getModuleByName(String name) {
	    for (Module m: mods) {
	        if (name.equals(m.getName())) return m;
	    }
	    return null;
	}
	
	public static List<Module> getModulesInCat(Category cat) {
		List<Module> mds = new ArrayList<>();
	    for (Module m: mods) {
	        if (m.getCategory().equals(cat)) mds.add(m);
	    }
	    return mds;
	}
	
	public static void onUpdate() {
		for(Module m: mods) {
			if(m.isToggled()) m.onUpdate();
		}
	}
	
	public static void onRender() {
		for(Module m: mods) {
			if(m.isToggled()) m.onRender();
		}
	}
	
	public static void updateKeys() {
		for(Module m: mods) {
			if(m.getKey().wasPressed() && MinecraftClient.getInstance().currentScreen == null) {
				m.toggle();
			}
		}
	}
}
