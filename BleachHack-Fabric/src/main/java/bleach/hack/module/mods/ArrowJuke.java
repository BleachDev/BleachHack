package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ArrowJuke extends Module {

	public ArrowJuke() {
		super("ArrowJuke", -1, Category.COMBAT, "Tries to dodge arrows coming at you", null);
	}
	
	@Subscribe
	public void onTick(EventTick enventTick) {
		for(Entity e: mc.world.getEntities()) {
			if(!(e instanceof ArrowEntity) || e.age > 50) continue;
			
			Box pBox = mc.player.getBoundingBox().expand(0.4);
			
			for(int i = 0; i < 150; i++) {
				Vec3d nextPos = e.getPos().add(e.getVelocity().multiply(i/5));
				Box nextBox = new Box(
						nextPos.subtract(e.getBoundingBox().getXSize()/2, 0, e.getBoundingBox().getZSize()/2), 
						nextPos.add(e.getBoundingBox().getXSize()/2, e.getBoundingBox().getYSize(), e.getBoundingBox().getZSize()/2));
				
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.x+0.5, mc.player.y, mc.player.z, true));
				if(pBox.intersects(nextBox)) {
					if(!moveBox(pBox, 0.7, 0, 0).intersects(nextBox)) mc.player.addVelocity(0.7, 0, 0);
					else if(!moveBox(pBox, -0.7, 0, 0).intersects(nextBox)) mc.player.addVelocity(-0.7, 0, 0);
					else if(!moveBox(pBox, 0, 0, 0.7).intersects(nextBox)) mc.player.addVelocity(0, 0, 0.7);
					//else if(!moveBox(pBox, 0, 0, -0.5).intersects(nextBox)) mc.player.addVelocity(0, 0, -0.5);
					else mc.player.addVelocity(0, 0, -0.7);
					//break;
				}
			}
		}
	}
	
	public Box moveBox(Box box, double x, double y, double z) {
		return new Box(new Vec3d(box.minX, box.minY, box.minZ).add(x, y, z), new Vec3d(box.maxX, box.maxY, box.maxZ).add(x, y, z));
	}

}
