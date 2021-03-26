package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class BiomeColorEvent extends Event {

	protected BlockRenderView world;
	protected BlockPos pos;

	private Integer color = null;

	public static class Grass extends BiomeColorEvent {

		public Grass(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

	}

	public static class Foilage extends BiomeColorEvent {

		public Foilage(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

	}

	public static class Water extends BiomeColorEvent {

		public Water(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

	}

	public BlockRenderView getWorld() {
		return world;
	}

	public void setWorld(BlockRenderView world) {
		this.world = world;
	}

	public BlockPos getPos() {
		return pos;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}
	
	public void setColor(Integer color) {
		this.color = color;
	}

	public Integer getColor() {
		return color;
	}

}
