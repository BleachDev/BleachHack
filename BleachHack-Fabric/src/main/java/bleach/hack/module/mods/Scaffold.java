package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 1, 0.3, 1, "Range: "));
	
	private HashMap<BlockPos, Integer> lastPlaced = new HashMap<>();
	
	public Scaffold() {
		super("Scaffold", GLFW.GLFW_KEY_N, Category.WORLD, "Places blocks under you", settings);
	}

	@Subscribe
	public void onTick(EventTick event) {
		HashMap<BlockPos, Integer> tempMap = new HashMap<>();
		for(Entry<BlockPos, Integer> e: lastPlaced.entrySet()) {
			if(e.getValue() > 0) tempMap.put(e.getKey(), e.getValue() - 1);
		}
		lastPlaced.clear();
		lastPlaced.putAll(tempMap);
		
		if(!(mc.player.inventory.getMainHandStack().getItem() instanceof BlockItem)) return;
		
		int slot = -1;
		int prevSlot = mc.player.inventory.selectedSlot;
		if(mc.player.inventory.getMainHandStack().getItem() instanceof BlockItem) {
			slot = mc.player.inventory.selectedSlot;
		}else for(int i = 0; i < 9; i++) {
			if(mc.player.inventory.getInvStack(i).getItem() instanceof BlockItem) {
				slot = i;
				break;
			}
		}
		if(slot == -1) return;
		
		mc.player.inventory.selectedSlot = slot;
		double range = getSettings().get(0).toSlider().getValue();
		for(int r = 0; r < 5; r++) {
			Vec3d r1 = new Vec3d(0,-0.85,0);
			if(r == 1) r1 = r1.add(range, 0, 0);
			if(r == 2) r1 = r1.add(-range, 0, 0);
			if(r == 3) r1 = r1.add(0, 0, range);
			if(r == 4) r1 = r1.add(0, 0, -range);
			
			if(WorldUtils.NONSOLID_BLOCKS.contains(
					mc.world.getBlockState(new BlockPos(mc.player.getPos().add(r1))).getBlock())) {
				placeBlockAuto(new BlockPos(mc.player.getPos().add(r1)));
				return;
			}
		}
		mc.player.inventory.selectedSlot = prevSlot;
	}
	
	public void placeBlockAuto(BlockPos block) {
		if(lastPlaced.containsKey(block)) return;
		for(Direction d: Direction.values()) {
			if(!WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
				if(WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SNEAKING));}
				mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, 
						new BlockHitResult(new Vec3d(block), d.getOpposite(), block.offset(d), false));
				mc.player.swingHand(Hand.MAIN_HAND);
				if(WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SNEAKING));}
				lastPlaced.put(block, 5);
				return;
			}
		}
	}

}
