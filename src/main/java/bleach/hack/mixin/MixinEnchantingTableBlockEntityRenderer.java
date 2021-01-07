package bleach.hack.mixin;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;

import java.util.Objects;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public class MixinEnchantingTableBlockEntityRenderer {

    @Final
    @Shadow
    public static SpriteIdentifier BOOK_TEXTURE;

    @Final
    @Shadow
    private final BookModel book = new BookModel();

    @Overwrite
    public void render(EnchantingTableBlockEntity enchantingTableBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        matrixStack.push();
        matrixStack.translate(0.5D, 0.75D, 0.5D);
        float g = (float)enchantingTableBlockEntity.ticks + f;
        matrixStack.translate(0.0D, (double)(0.1F + MathHelper.sin(g * 0.1F) * 0.01F), 0.0D);

        float h;
        for(h = enchantingTableBlockEntity.field_11964 - enchantingTableBlockEntity.field_11963; h >= 3.1415927F; h -= 6.2831855F) {
        }

        while(h < -3.1415927F) {
            h += 6.2831855F;
        }

        float k = enchantingTableBlockEntity.field_11963 + h * f;
        matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(-k));
        matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(80.0F));
        if (!ModuleManager.getModule(NoRender.class).getSetting(18).asToggle().state) {
            float l = MathHelper.lerp(f, enchantingTableBlockEntity.pageAngle, enchantingTableBlockEntity.nextPageAngle);
            float m = MathHelper.fractionalPart(l + 0.25F) * 1.6F - 0.3F;
            float n = MathHelper.fractionalPart(l + 0.75F) * 1.6F - 0.3F;
            float o = MathHelper.lerp(f, enchantingTableBlockEntity.pageTurningSpeed, enchantingTableBlockEntity.nextPageTurningSpeed);
            this.book.setPageAngles(g, MathHelper.clamp(m, 0.0F, 1.0F), MathHelper.clamp(n, 0.0F, 1.0F), o);
            VertexConsumer vertexConsumer = BOOK_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
            this.book.method_24184(matrixStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        matrixStack.pop();
    }
}
