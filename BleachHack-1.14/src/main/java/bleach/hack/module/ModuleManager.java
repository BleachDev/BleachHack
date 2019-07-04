package bleach.hack.module;

import java.util.Arrays;
import java.util.List;

import bleach.hack.module.mods.*;

public class ModuleManager {

	private static List<Module> mods = Arrays.asList( // new ArrayList<Module>();
			new ChestESP(),
			new ClickGui(),
			new ESP(),
			new Flight(),
			new Fullbright(),
			new Jesus(),
			new Killaura(),
			new Nofall(),
			new Sprint(),
			new Step(),
			new Tracers());
	
	public static List<Module> getModules() {
		return mods;
	}
	
	public static Module getModuleByName(String name) {
	    for (Module m: mods) {
	        if (name.equals(m.getName())) {
	            return m;
	        }
	    }
	    return null;
	}
	
	public static void onUpdate() {
		for(Module m: mods) {
			m.onUpdate();
		}
	}
	
	public static void onRender() {
		for(Module m: mods) {
			m.onRender();
		}
	}
	
	public static void onKeyPressed(int k) {
		for(Module m: mods) {
			if(m.getKey().isPressed()) {
				m.toggle();
			}
		}
	}
}
