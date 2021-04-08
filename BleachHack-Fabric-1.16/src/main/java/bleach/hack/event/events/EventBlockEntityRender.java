/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class EventBlockEntityRender extends Event {

	public static class Single extends EventBlockEntityRender {
		protected BlockEntity blockEntity;
		protected MatrixStack matrices;
		protected VertexConsumerProvider vertexConsumers;
		
		public BlockEntity getBlockEntity() {
			return blockEntity;
		}
		
		public MatrixStack getMatrices() {
			return matrices;
		}
		
		public VertexConsumerProvider getVertexConsumers() {
			return vertexConsumers;
		}
	
		public static class Pre extends Single {
			public Pre(BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
				this.blockEntity = blockEntity;
				this.matrices = matrices;
				this.vertexConsumers = vertexConsumers;
			}
		
			public void setBlockEntity(BlockEntity blockEntity) {
				this.blockEntity = blockEntity;
			}
		
			public void setMatrices(MatrixStack matrices) {
				this.matrices = matrices;
			}
		
			public void setVertexConsumers(VertexConsumerProvider vertexConsumers) {
				this.vertexConsumers = vertexConsumers;
			}
}
		
		public static class Post extends Single {
			public Post(BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
				this.blockEntity = blockEntity;
				this.matrices = matrices;
				this.vertexConsumers = vertexConsumers;
			}
		}
	}
	
	public static class PreAll extends EventEntityRender {
	}
	
	public static class PostAll extends EventEntityRender {
	}
}
