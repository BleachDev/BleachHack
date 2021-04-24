/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EventEntityRender extends Event {

	public static class Single extends EventEntityRender {
		protected Entity entity;
		protected MatrixStack matrix;
		protected VertexConsumerProvider vertex;
	
		public Entity getEntity() {
			return entity;
		}
		
		public MatrixStack getMatrix() {
			return matrix;
		}
		
		public VertexConsumerProvider getVertex() {
			return vertex;
		}
		
		public static class Pre extends Single {
			public Pre(Entity entity, MatrixStack matrix, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrix = matrix;
				this.vertex = vertex;
			}
			
			public void setMatrix(MatrixStack matrix) {
				this.matrix = matrix;
			}
			
			public void setVertex(VertexConsumerProvider vertex) {
				this.vertex = vertex;
			}
			
			public void setEntity(Entity entity) {
				this.entity = entity;
			}
		}
		
		public static class Post extends Single {
			public Post(Entity entity, MatrixStack matrix, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrix = matrix;
				this.vertex = vertex;
			}
		}
	
		public static class Label extends Single {
			public Label(Entity entity, MatrixStack matrix, VertexConsumerProvider vertex) {
				this.entity = entity;
				this.matrix = matrix;
				this.vertex = vertex;
			}
			
			public void setMatrix(MatrixStack matrix) {
				this.matrix = matrix;
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
