package bleach.hack.utils;

import java.util.ArrayList;
import java.util.List;

public class BleachQueue {

	public static List<Runnable> queue = new ArrayList<>();
	
	public static void nextQueue() {
		if(queue.isEmpty()) return;
		queue.get(0).run();
		queue.remove(0);
	}
}
