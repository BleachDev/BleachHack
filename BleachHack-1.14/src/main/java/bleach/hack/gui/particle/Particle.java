package bleach.hack.gui.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Particle {

	private Random rand = new Random();
	private int tick = 0;
	private int x;
	private int y;
	private int lifespan;
	private boolean dead = false;
	
	private List<int[]> particles = new ArrayList<>();
	
	public Particle(int x, int y) {
		this.x = x;
		this.y = y;
		genParticles();
		lifespan = rand.nextInt(25);
	}
	
	public void genParticles() {
		for(int j = 0; j < rand.nextInt(10); j++) {
			particles.add(new int[] {x + rand.nextInt(5)-2, y + rand.nextInt(5)-2});
		}
	}
	
	public void updateParticles() {
		tick++;
		
		if(tick > lifespan) {
			dead = true;
			particles.clear();
		}
		
		for(int i = 0; i < particles.size()-1; i++) {
			int[] pos = particles.get(i);
			int diffx = pos[0] - x;
			int diffy = pos[1] - y;
			
			particles.set(i, new int[] {pos[0] + (diffx / tick), pos[1] + (diffy / tick)});
		}
	}
	
	public List<int[]> getParticles() {
		return particles;
	}
	
	public boolean isDead() {
		return dead;
	}
}
