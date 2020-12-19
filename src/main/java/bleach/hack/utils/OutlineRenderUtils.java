package bleach.hack.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class OutlineRenderUtils {

    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void renderPass(Entity entity, MatrixStack matrices, float r, float b, float g, float a) {
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        OutlineVertexConsumerProvider outlineVertexConsumerProvider = mc.getBufferBuilders().getOutlineVertexConsumers();

        double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * mc.getTickDelta() - mc.gameRenderer.getCamera().getPos().getX();
        double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * mc.getTickDelta() - mc.gameRenderer.getCamera().getPos().getY();
        double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * mc.getTickDelta() - mc.gameRenderer.getCamera().getPos().getZ();
        float yaw = entity.prevYaw + (entity.yaw - entity.prevYaw) * mc.getTickDelta();

        matrices.push();
        matrices.translate(x, y, z);

        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);

        outlineVertexConsumerProvider.setColor((int) (r * 255), (int) (b * 255), (int) (g * 255), (int) (a * 255));

        mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, yaw, mc.getTickDelta(), matrices, outlineVertexConsumerProvider, 0xF000F0);
        immediate.draw(RenderLayer.getEntitySolid(mc.getEntityRenderDispatcher().getRenderer(entity).getTexture(entity)));
        immediate.draw(RenderLayer.getEntityCutout(mc.getEntityRenderDispatcher().getRenderer(entity).getTexture(entity)));
        immediate.draw(RenderLayer.getEntityCutoutNoCull(mc.getEntityRenderDispatcher().getRenderer(entity).getTexture(entity)));
        immediate.draw(RenderLayer.getEntitySmoothCutout(mc.getEntityRenderDispatcher().getRenderer(entity).getTexture(entity)));
        immediate.draw();
        outlineVertexConsumerProvider.draw();
        matrices.pop();
    }

    public static void renderOne(float linewidth) {
        checkSetupFBO();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(linewidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glClearStencil(0xF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public static void renderThree() {
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(1.0F, -2000000f);
        GL13.glMultiTexCoord2f(0x84C1, 240.0F, 240.0F);
    }

    public static void renderFour() {
        GL11.glPolygonOffset(1.0F, 2000000f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = MinecraftClient.getInstance().getFramebuffer();

        if (fbo != null) {
            // Checks if screen has been resized or new FBO has been created
            if (fbo.getDepthAttachment() > -1) {
                // Sets up the FBO with depth and stencil extensions (24/8 bit)
                setupFBO(fbo);
                // Reset the ID to prevent multiple FBO's
                FabricReflect.writeField(fbo, -1, "field_1474", "depthAttachment");
            }
        }
    }

    public static void setupFBO(Framebuffer fbo) {
        // Deletes old render buffer extensions such as depth
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.getDepthAttachment());
        // Generates a new render buffer ID for the depth and stencil extension
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        // Binds new render buffer by ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        // Adds the depth and stencil extension
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight());
        // Adds the stencil attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        // Adds the depth attachment
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
    }
}