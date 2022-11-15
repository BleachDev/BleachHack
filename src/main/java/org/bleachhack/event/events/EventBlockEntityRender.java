/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import org.bleachhack.event.Event;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class EventBlockEntityRender extends Event {

	public static class Single extends EventBlockEntityRender {

		protected BlockEntity blockEntity;
		protected MatrixStack matrices;
		protected VertexConsumerProvider vertex;

		public BlockEntity getBlockEntity() {
			return blockEntity;
		}

		public MatrixStack getMatrices() {
			return matrices;
		}

		public VertexConsumerProvider getVertex() {
			return vertex;
		}

		public static class Pre extends Single {

			public Pre(BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertex) {
				this.blockEntity = blockEntity;
				this.matrices = matrices;
				this.vertex = vertex;
			}

			public void setBlockEntity(BlockEntity blockEntity) {
				this.blockEntity = blockEntity;
			}

			public void setMatrices(MatrixStack matrices) {
				this.matrices = matrices;
			}

			public void setVertex(VertexConsumerProvider vertex) {
				this.vertex = vertex;
			}
		}

		public static class Post extends Single {
			public Post(BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
				this.blockEntity = blockEntity;
				this.matrices = matrices;
				this.vertex = vertexConsumers;
			}
		}
	}

	public static class PreAll extends EventEntityRender {
	}

	public static class PostAll extends EventEntityRender {
	}
}
