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

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EventEntityRender extends Event {

	public static class Single extends EventEntityRender {

		protected Entity entity;
		protected MatrixStack matrices;
		protected VertexConsumerProvider vertex;

		public Entity getEntity() {
			return entity;
		}

		public MatrixStack getMatrix() {
			return matrices;
		}

		public VertexConsumerProvider getVertex() {
			return vertex;
		}

		public static class Pre extends Single {

			public Pre(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrices = matrices;
				this.vertex = vertex;
			}

			public void setMatrix(MatrixStack matrices) {
				this.matrices = matrices;
			}

			public void setVertex(VertexConsumerProvider vertex) {
				this.vertex = vertex;
			}

			public void setEntity(Entity entity) {
				this.entity = entity;
			}
		}

		public static class Post extends Single {

			public Post(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrices = matrices;
				this.vertex = vertex;
			}
		}

		public static class Label extends Single {

			public Label(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrices = matrices;
				this.vertex = vertex;
			}

			public void setMatrix(MatrixStack matrices) {
				this.matrices = matrices;
			}

			public void setVertex(VertexConsumerProvider vertex) {
				this.vertex = vertex;
			}
		}
	}

	public static class PreAll extends EventEntityRender {
	}

	public static class PostAll extends EventEntityRender {
	}
}
