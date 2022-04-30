package com.lowdragmc.lowdraglib.client.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Stack;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {

    private static final Stack<int[]> scissorFrameStack = new Stack<>();

    public static void useScissor(int x, int y, int width, int height, Runnable codeBlock) {
        pushScissorFrame(x, y, width, height);
        try {
            codeBlock.run();
        } finally {
            popScissorFrame();
        }
    }

    private static int[] peekFirstScissorOrFullScreen() {
        int[] currentTopFrame = scissorFrameStack.isEmpty() ? null : scissorFrameStack.peek();
        if (currentTopFrame == null) {
            MainWindow window = Minecraft.getInstance().getWindow();
            return new int[]{0, 0, window.getWidth(), window.getHeight()};
        }
        return currentTopFrame;
    }

    public static void pushScissorFrame(int x, int y, int width, int height) {
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];

        boolean pushedFrame = false;
        if (x <= parentX + parentWidth && y <= parentY + parentHeight) {
            int newX = Math.max(x, parentX);
            int newY = Math.max(y, parentY);
            int newWidth = width - (newX - x);
            int newHeight = height - (newY - y);
            if (newWidth > 0 && newHeight > 0) {
                int maxWidth = parentWidth - (x - parentX);
                int maxHeight = parentHeight - (y - parentY);
                newWidth = Math.min(maxWidth, newWidth);
                newHeight = Math.min(maxHeight, newHeight);
                applyScissor(newX, newY, newWidth, newHeight);
                //finally, push applied scissor on top of scissor stack
                if (scissorFrameStack.isEmpty()) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                }
                scissorFrameStack.push(new int[]{newX, newY, newWidth, newHeight});
                pushedFrame = true;
            }
        }
        if (!pushedFrame) {
            if (scissorFrameStack.isEmpty()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            scissorFrameStack.push(new int[]{parentX, parentY, parentWidth, parentHeight});
        }
    }

    public static void popScissorFrame() {
        scissorFrameStack.pop();
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];
        applyScissor(parentX, parentY, parentWidth, parentHeight);
        if (scissorFrameStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    //applies scissor with gui-space coordinates and sizes
    private static void applyScissor(int x, int y, int w, int h) {
        //translate upper-left to bottom-left
        MainWindow window = Minecraft.getInstance().getWindow();
        double s = window.getGuiScale();
        int translatedY = window.getGuiScaledHeight() - y - h;
        GL11.glScissor((int)(x * s), (int)(translatedY * s), (int)(w * s), (int)(h * s));
    }

    public static void renderBlockOverLay(MatrixStack matrixStack, BlockPos pos, float r, float g, float b, float scale) {
        if (pos == null) return;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        matrixStack.pushPose();
        matrixStack.translate((pos.getX() + 0.5), (pos.getY() + 0.5), (pos.getZ() + 0.5));
        matrixStack.scale(scale, scale, scale);

        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.disableTexture();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        RenderUtils.renderCubeFace(matrixStack, buffer, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, r, g, b, 1);
        tessellator.end();

        matrixStack.popPose();

        RenderSystem.enableTexture();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1, 1, 1, 1);
    }

    public static void renderCubeFace(MatrixStack matrixStack, BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        Matrix4f mat = matrixStack.last().pose();
        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(mat, minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(mat, minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }

    public static void useLightMap(float x, float y, Runnable codeBlock){
        /* hack the lightmap */
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        float lastBrightnessX = GlStateManager.lastBrightnessX;
        float lastBrightnessY = GlStateManager.lastBrightnessY;
        RenderSystem.glMultiTexCoord2f(33986, x, y);
        if (codeBlock != null) {
            codeBlock.run();
        }
        /* restore the lightmap  */
        RenderSystem.glMultiTexCoord2f(33986, lastBrightnessX, lastBrightnessY);
        GL11.glPopAttrib();
    }

    public static void moveToFace(MatrixStack matrixStack, double x, double y, double z, Direction face) {
        matrixStack.translate(x + 0.5 + face.getStepX() * 0.5, y + 0.5 + face.getStepY() * 0.5, z + 0.5 + face.getStepZ() * 0.5);
    }

    public static void rotateToFace(MatrixStack matrixStack, Direction face, @Nullable Direction spin) {
        int angle = spin == Direction.EAST ? 90 : spin == Direction.SOUTH ? 180 : spin == Direction.WEST ? -90 : 0;
        switch (face) {
            case UP:
                matrixStack.scale(1.0f, -1.0f, 1.0f);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                break;
            case DOWN:
                matrixStack.scale(1.0f, -1.0f, 1.0f);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(spin == Direction.EAST ? 90 : spin == Direction.NORTH ? 180 : spin == Direction.WEST ? -90 : 0));
                break;
            case EAST:
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                break;
            case WEST:
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                break;
            case NORTH:
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                break;
            case SOUTH:
                matrixStack.scale(-1.0f, -1.0f, -1.0f);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(angle));
                break;
            default:
                break;
        }
    }
}
