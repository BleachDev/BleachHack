package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ArrowJuke extends Module {

	private final static List<SettingBase> settings = Arrays.asList(
			new SettingMode("Move: ", "Client", "Packet"));
	
	public ArrowJuke() {
		super("ArrowJuke", -1, Category.COMBAT, "Tries to dodge arrows coming at you", settings);
	}
	
	@Subscribe
	public void onTick(EventTick envent) {
		for(Entity e: mc.world.getEntities()) {
			if(!(e instanceof ArrowEntity) || e.age > 50) continue;
			
			Box pBox = mc.player.getBoundingBox().expand(0.555);
			List<Box> boxes = new ArrayList<>();
			
			for(int i = 0; i < 100; i++) {
				Vec3d nextPos = e.getPos().add(e.getVelocity().multiply(i/5));
				boxes.add(new Box(
						nextPos.subtract(e.getBoundingBox().getXSize()/2, 0, e.getBoundingBox().getZSize()/2), 
						nextPos.add(e.getBoundingBox().getXSize()/2, e.getBoundingBox().getYSize(), e.getBoundingBox().getZSize()/2)));
			}
			
			int mode = getSettings().get(0).toMode().mode;
			
			for(int i = 0; i < 75; i++) {
				Vec3d nextPos = e.getPos().add(e.getVelocity().multiply(i/5));
				Box nextBox = new Box(
						nextPos.subtract(e.getBoundingBox().getXSize()/2, 0, e.getBoundingBox().getZSize()/2), 
						nextPos.add(e.getBoundingBox().getXSize()/2, e.getBoundingBox().getYSize(), e.getBoundingBox().getZSize()/2));
				
				if(pBox.intersects(nextBox)) {
					for(Vec3d vel: new Vec3d[] {new Vec3d(1,0,0), new Vec3d(-1,0,0), new Vec3d(0,0,1)}) {
						boolean contains = false;
						for(Box b : boxes) if(b.intersects(moveBox(pBox, vel.x, vel.y, vel.z))) contains = true;
						if(!contains) {
							if(mode == 0) mc.player.addVelocity(vel.x, vel.y, vel.z);
							else if(mode == 1) {
								Vec3d vel2 = mc.player.getPos().add(vel.multiply(1.5));
								mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y, vel2.z, false));
								mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y-0.01, vel2.z, true));
							}
							return;
						}
					}
					if(mode == 0) mc.player.addVelocity(0, 0, -0.5);
					else if(mode == 1) {
						Vec3d vel2 = mc.player.getPos().add(new Vec3d(0,0,-0.75));
						mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y, vel2.z, false));
						mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y-0.01, vel2.z, true));
					}
				}
			}
		}
	}
	
	public Box moveBox(Box box, double x, double y, double z) {
		return new Box(new Vec3d(box.minX, box.minY, box.minZ).add(x, y, z), new Vec3d(box.maxX, box.maxY, box.maxZ).add(x, y, z));
	}

}
