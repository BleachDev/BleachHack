package bleach.hack.gui.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;

public class ParticleManager {

	private List<Particle> particles = new ArrayList<>();
	
	public void addParticle(int x, int y) {
		particles.add(new Particle(x, y));
	}
	
	public void renderParticles() {
		List<Particle> tempParts = new ArrayList<>();
		
		for(Particle p: particles) {
			p.updateParticles();
			if(p.isDead()) tempParts.add(p);
		}
		
		particles.removeAll(tempParts);
		
		for(Particle p: particles) {
			for(int[] p1: p.getParticles()) {
				Screen.fill(p1[0], p1[1], p1[0]+1, p1[1]+1, 0xffffc0e0);
			}
		}
	}
}
