package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.VertexConsumer;

/**
 * BufferBuilder patch that allows the color to be temporarily fixed even when the VertexFormatElement isn't COLOR
 */
@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder extends FixedColorVertexConsumer implements BufferVertexConsumer {

	@Shadow private int elementOffset;
	@Shadow private boolean field_21594;
	@Shadow private boolean field_21595;

	@Overwrite
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		if (this.colorFixed) {
			if (fixedRed != -1) red = fixedRed;
			if (fixedGreen != -1) green = fixedGreen;
			if (fixedBlue != -1) blue = fixedBlue;
			if (fixedAlpha != -1) alpha = fixedAlpha;
		}

		return BufferVertexConsumer.super.color(red, green, blue, alpha);
	}

	@Overwrite
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
		if (this.colorFixed) {
			if (fixedRed != -1) red = fixedRed / 255f;
			if (fixedGreen != -1) green = fixedGreen / 255f;
			if (fixedBlue != -1) blue = fixedBlue / 255f;
			if (fixedAlpha != -1) alpha = fixedAlpha / 255f;
		}

		if (this.field_21594) {
			this.putFloat(0, x);
			this.putFloat(4, y);
			this.putFloat(8, z);
			this.putByte(12, (byte)((int)(red * 255.0F)));
			this.putByte(13, (byte)((int)(green * 255.0F)));
			this.putByte(14, (byte)((int)(blue * 255.0F)));
			this.putByte(15, (byte)((int)(alpha * 255.0F)));
			this.putFloat(16, u);
			this.putFloat(20, v);
			byte j;
			if (this.field_21595) {
				this.putShort(24, (short)(overlay & '\uffff'));
				this.putShort(26, (short)(overlay >> 16 & '\uffff'));
				j = 28;
			} else {
				j = 24;
			}

			this.putShort(j + 0, (short)(light & '\uffff'));
			this.putShort(j + 2, (short)(light >> 16 & '\uffff'));
			this.putByte(j + 4, BufferVertexConsumer.method_24212(normalX));
			this.putByte(j + 5, BufferVertexConsumer.method_24212(normalY));
			this.putByte(j + 6, BufferVertexConsumer.method_24212(normalZ));
			this.elementOffset += j + 8;
			this.next();
		} else {
			super.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
		}
	}
}
