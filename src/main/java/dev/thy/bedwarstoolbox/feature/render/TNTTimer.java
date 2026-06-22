package dev.thy.bedwarstoolbox.feature.render;

import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.event.Render3DEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.lwjgl.opengl.GL11;

import java.util.Locale;

public class TNTTimer extends Feature {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final NumberSetting size = new NumberSetting("Size", "Timer scale", 1.0D, 0.5D, 4.0D);
    private final NumberSetting yOffset = new NumberSetting("Y Offset", "Vertical offset", 0.0D, -2.0D, 2.0D);
    private final ColorSetting color = new ColorSetting("Color", 255, 80, 80, 255);

    public TNTTimer() {
        super(FeatureCategory.RENDER);
        registerSetting(size);
        registerSetting(yOffset);
        registerSetting(color);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || minecraft.theWorld == null) {
            return;
        }

        for (Object object : minecraft.theWorld.loadedEntityList) {
            if (object instanceof EntityTNTPrimed) {
                renderTimer((EntityTNTPrimed) object, event.getPartialTicks());
            }
        }
    }

    private void renderTimer(EntityTNTPrimed tnt, float partialTicks) {
        double x = interpolate(tnt.lastTickPosX, tnt.posX, partialTicks);
        double y = interpolate(tnt.lastTickPosY, tnt.posY, partialTicks) + tnt.height / 2.0D + yOffset.getValue();
        double z = interpolate(tnt.lastTickPosZ, tnt.posZ, partialTicks);
        float seconds = Math.max(0.0F, (tnt.fuse - partialTicks) / 20.0F);
        String text = String.format(Locale.US, "%.1fs", seconds);

        renderWorldText(text, x, y, z);
    }

    private void renderWorldText(String text, double x, double y, double z) {
        RenderManager renderManager = minecraft.getRenderManager();
        FontRenderer fontRenderer = minecraft.fontRendererObj;
        double renderX = x - renderManager.viewerPosX;
        double renderY = y - renderManager.viewerPosY;
        double renderZ = z - renderManager.viewerPosZ;
        float scale = 0.02666667F * size.getValue().floatValue();
        int halfWidth = fontRenderer.getStringWidth(text) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderX, renderY, renderZ);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.enableTexture2D();

        fontRenderer.drawStringWithShadow(text, -halfWidth, 0, color.toArgb());

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private double interpolate(double previous, double current, float partialTicks) {
        return previous + (current - previous) * partialTicks;
    }
}
