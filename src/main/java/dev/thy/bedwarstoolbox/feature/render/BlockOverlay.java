package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.event.BlockHighlightEvent;
import dev.thy.bedwarstoolbox.core.event.Render3DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

public class BlockOverlay extends Feature {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final ColorSetting outlineColor = new ColorSetting("Outline Color", 128, 128, 128, 100);
    private final ColorSetting fillColor = new ColorSetting("Fill Color", 128, 128, 128, 100);
    private final BooleanSetting fullBox = new BooleanSetting("Full Box", false);

    public BlockOverlay() {
        super(FeatureCategory.RENDER);
        registerSetting(outlineColor);
        registerSetting(fillColor);
        registerSetting(fullBox);
    }

    @Subscribe
    public void onBlockHighlight(BlockHighlightEvent event) {
        if (isEnabled()) {
            event.setCancelled(true);
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || minecraft.theWorld == null || minecraft.objectMouseOver == null) {
            return;
        }

        MovingObjectPosition target = minecraft.objectMouseOver;
        if (target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        BlockPos pos = target.getBlockPos();
        Block block = minecraft.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB box = fullBox.getValue()
                ? new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D)
                : block.getSelectedBoundingBox(minecraft.theWorld, pos);
        if (box == null) {
            return;
        }

        double renderX = minecraft.getRenderManager().viewerPosX;
        double renderY = minecraft.getRenderManager().viewerPosY;
        double renderZ = minecraft.getRenderManager().viewerPosZ;
        AxisAlignedBB renderBox = box.expand(0.002D, 0.002D, 0.002D).offset(-renderX, -renderY, -renderZ);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        if (fullBox.getValue()) {
            GlStateManager.disableDepth();
        }

        drawFilledBox(renderBox, fillColor);
        GL11.glLineWidth(2.0F);
        RenderGlobal.drawOutlinedBoundingBox(
                renderBox,
                outlineColor.getRed(),
                outlineColor.getGreen(),
                outlineColor.getBlue(),
                outlineColor.getAlpha()
        );
        GL11.glLineWidth(1.0F);

        if (fullBox.getValue()) {
            GlStateManager.enableDepth();
        }
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawFilledBox(AxisAlignedBB box, ColorSetting color) {
        GlStateManager.color(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        renderer.pos(box.minX, box.minY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        renderer.pos(box.minX, box.minY, box.maxZ).endVertex();

        renderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        renderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.minZ).endVertex();

        renderer.pos(box.minX, box.minY, box.minZ).endVertex();
        renderer.pos(box.minX, box.maxY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.minY, box.minZ).endVertex();

        renderer.pos(box.maxX, box.minY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        renderer.pos(box.maxX, box.minY, box.maxZ).endVertex();

        renderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        renderer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        renderer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        renderer.pos(box.minX, box.maxY, box.maxZ).endVertex();

        renderer.pos(box.minX, box.minY, box.minZ).endVertex();
        renderer.pos(box.minX, box.minY, box.maxZ).endVertex();
        renderer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        renderer.pos(box.minX, box.maxY, box.minZ).endVertex();

        tessellator.draw();
    }
}
