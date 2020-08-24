package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.AllahHaram;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer {
	
	@Shadow private TextHandler handler;
	
	@Shadow public abstract int drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean mirror);
	@Shadow public abstract int draw(OrderedText text, float x, float y, int color, Matrix4f matrix, boolean shadow);
	
	@Inject(method = "drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", at = @At("HEAD"), cancellable = true)
	public void drawWithShadow(MatrixStack matrices, Text text, float x, float y, int color, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(AllahHaram.class).isToggled()) {
			text = new LiteralText(chingChong(text.asString()));
			ci.setReturnValue(draw(text.asOrderedText(), x, y, color, matrices.peek().getModel(), true));
			ci.cancel();
		}
	}
	
	@Inject(method = "draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", at = @At("HEAD"), cancellable = true)
	public void draw(MatrixStack matrices, Text text, float x, float y, int color, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(AllahHaram.class).isToggled()) {
			text = new LiteralText(chingChong(text.asString()));
			ci.setReturnValue(draw(text.asOrderedText(), x, y, color, matrices.peek().getModel(), false));
			ci.cancel();
		}
	}
	
	@Inject(method = "draw(Ljava/lang/String;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZIIZ)I", at = @At("HEAD"), cancellable = true)
	public void draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, boolean seeThrough, int backgroundColor, int light, boolean mirror, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(AllahHaram.class).isToggled()) {
			text = chingChong(text);
			ci.setReturnValue(drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, seeThrough, backgroundColor, light, mirror));
			ci.cancel();
		}
	}

	@Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
	public void getWidth(String text, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(AllahHaram.class).isToggled()) {
			ci.setReturnValue(MathHelper.ceil(this.handler.getWidth(chingChong(text))));
			ci.cancel();
		}
	}

	@Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), cancellable = true)
	public void getWidth(StringVisitable text, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule(AllahHaram.class).isToggled() && text instanceof Text) {
			ci.setReturnValue(MathHelper.ceil(this.handler.getWidth(chingChong(((Text)text).asString()))));
			ci.cancel();
		}
	}

	private String chingChong(String s) {
		String newString = "";
		Random rand = new Random(s.hashCode());
		
		for (int i = 0; i < s.length(); i++) {
			if (i != s.length() - 1 && s.charAt(i) == '\u00a7') {
				newString += "\u00a7" + Character.toString(s.charAt(i + 1));
				i++;
			} else {
				newString += (char) (0xFB50 + rand.nextInt(0x60));
			}
		}
		
		return newString;
	}
}
