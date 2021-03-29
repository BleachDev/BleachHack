package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.operation.Operation;
import bleach.hack.util.operation.OperationList;
import bleach.hack.util.operation.blueprint.OperationBlueprint;
import bleach.hack.util.operation.blueprint.PlaceDirOperationBlueprint;
import bleach.hack.util.operation.blueprint.PlaceOperationBlueprint;
import bleach.hack.util.operation.blueprint.RemoveOperationBlueprint;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class AutoBuild extends Module {

	private static List<List<OperationBlueprint>> BLUEPRINTS = Arrays.asList(
			Arrays.asList( // Bomber Mid
					new PlaceOperationBlueprint(0, 0, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(0, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, 0, 0, Items.DETECTOR_RAIL),
					new PlaceOperationBlueprint(1, -1, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(2, -1, 1, Items.SLIME_BLOCK),
					new RemoveOperationBlueprint(1, -1, 1),
					new PlaceOperationBlueprint(2, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					new PlaceOperationBlueprint(2, -2, 0, Items.TNT),
					new PlaceOperationBlueprint(3, -1, 0, Items.SLIME_BLOCK),
					new PlaceDirOperationBlueprint(3, 0, 0, Items.OBSERVER, Direction.WEST),
					new PlaceDirOperationBlueprint(4, 0, 0, Items.PISTON, Direction.WEST)),
			Arrays.asList( // Bomber End
					new PlaceOperationBlueprint(0, 0, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(0, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, -1, 0, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(1, 0, 0, Items.DETECTOR_RAIL),
					new PlaceOperationBlueprint(1, -1, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(2, -1, 1, Items.SLIME_BLOCK),
					new RemoveOperationBlueprint(1, -1, 1),
					new PlaceOperationBlueprint(2, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 1, Items.SLIME_BLOCK),
					new PlaceOperationBlueprint(3, -2, 0, Items.TUBE_CORAL_FAN),
					new PlaceOperationBlueprint(2, -2, 0, Items.TNT),
					new PlaceOperationBlueprint(3, -1, 0, Items.SANDSTONE_WALL)));

	private OperationList current = null;
	private BlockHitResult ray = null;
	private boolean active = false;

	public AutoBuild() {
		super("AutoBuild", KEY_UNBOUND, Category.WORLD, "Auto builds stuff",
				new SettingMode("Build", "Bomber-Mid", "Bomber-End").withDesc("What to build (more things soon)"));
	}

	public void onDisable() {
		current = null;
		ray = null;
		active = false;

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!active) {
			ray = (BlockHitResult) mc.player.raycast(40, mc.getTickDelta(), false);
			Direction dir = ray.getSide();

			if (dir.getAxis() == Axis.Y) {
				dir = Math.abs(ray.getBlockPos().getX() - mc.player.getBlockPos().getX()) > Math.abs(ray.getBlockPos().getZ() - mc.player.getBlockPos().getZ())
						? ray.getBlockPos().getX() - mc.player.getBlockPos().getX() > 0 ? Direction.EAST : Direction.WEST
								: ray.getBlockPos().getZ() - mc.player.getBlockPos().getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
			}

			current = OperationList.create(BLUEPRINTS.get(getSetting(0).asMode().mode), ray.getBlockPos().offset(ray.getSide()), dir);

			if (mc.mouse.wasLeftButtonClicked() || mc.mouse.wasRightButtonClicked()) {
				active = true;
			}
		} else {
			if (current.executeNext() && current.isDone()) {
				setEnabled(false);
			}
		}
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		if (current != null) {
			//RenderUtils.drawOutlineBox(current.getBox(), 1f, 1f, 0f, 0.5f);

			for (Operation o: current.getRemainingOps()) {
				o.render();
			}
			
			RenderUtils.drawOutline(new Box(current.getNext().pos).contract(0.01), 1f, 1f, 0f, 0.5f, 3f);
		}

		if (ray != null && !active) {
			BlockPos pos = ray.getBlockPos();
			Direction dir = ray.getSide();
			RenderUtils.drawFilledBox(new Box(
					pos.getX() + (dir == Direction.EAST ? 0.98 : 0), pos.getY() + (dir == Direction.UP ? 0.98 : 0), pos.getZ() + (dir == Direction.SOUTH ? 0.98 : 0),
					pos.getX() + (dir == Direction.WEST ? 0.02 : 1), pos.getY() + (dir == Direction.DOWN ? 0.02 : 1), pos.getZ() + (dir == Direction.NORTH ? 0.02 : 1)),
					1f, 1f, 0f, 0.3f);
		}
	}
}