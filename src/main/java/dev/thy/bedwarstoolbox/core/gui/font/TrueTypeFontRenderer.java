package dev.thy.bedwarstoolbox.core.gui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TrueTypeFontRenderer {
    private static final ResourceLocation FONT_LOCATION = new ResourceLocation("bedwarstoolbox", "font/normal.ttf");
    private static final int RENDER_SCALE = 2;
    private static final int HORIZONTAL_PADDING = 2;
    private static final int VERTICAL_OFFSET = -2;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Map<String, RenderedText> cache = new HashMap<>();
    private Font font;
    private FontMetrics metrics;

    public TrueTypeFontRenderer(float size) {
        loadFont(size);
    }

    public int drawString(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        if (font == null) {
            return minecraft.fontRendererObj.drawString(text, Math.round(x), Math.round(y + VERTICAL_OFFSET), color);
        }

        RenderedText renderedText = getRenderedText(text, color);
        drawTexture(
                renderedText.location,
                x - HORIZONTAL_PADDING,
                y + VERTICAL_OFFSET,
                renderedText.textureWidth / (float) RENDER_SCALE,
                renderedText.textureHeight / (float) RENDER_SCALE
        );
        return getStringWidth(text);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x + 1.0F, y + 1.0F, 0xAA000000);
        return drawString(text, x, y, color);
    }

    public int getStringWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        if (font == null) {
            return minecraft.fontRendererObj.getStringWidth(text);
        }

        return Math.round(metrics.stringWidth(text) / (float) RENDER_SCALE);
    }

    public int getStringHeight() {
        if (font == null) {
            return minecraft.fontRendererObj.FONT_HEIGHT;
        }

        return Math.round(metrics.getHeight() / (float) RENDER_SCALE);
    }

    private void loadFont(float size) {
        try (InputStream inputStream = minecraft.getResourceManager().getResource(FONT_LOCATION).getInputStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, size * RENDER_SCALE);
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            applyRenderingHints(graphics);
            graphics.setFont(font);
            metrics = graphics.getFontMetrics();
            graphics.dispose();
        } catch (Exception exception) {
            exception.printStackTrace();
            font = null;
            metrics = null;
        }
    }

    private RenderedText getRenderedText(String text, int color) {
        String key = text + "\u0000" + color;
        RenderedText renderedText = cache.get(key);
        if (renderedText != null) {
            return renderedText;
        }

        int padding = HORIZONTAL_PADDING * RENDER_SCALE;
        int width = Math.max(1, metrics.stringWidth(text) + padding * 2);
        int height = Math.max(1, metrics.getAscent() + metrics.getDescent());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        applyRenderingHints(graphics);
        graphics.setFont(font);
        graphics.setColor(new Color(color, true));
        graphics.drawString(text, padding, metrics.getAscent());
        graphics.dispose();

        DynamicTexture texture = new DynamicTexture(image);
        ResourceLocation location = minecraft.getTextureManager().getDynamicTextureLocation(
                "bedwarstoolbox/font/" + cache.size(),
                texture
        );
        renderedText = new RenderedText(location, width, height);
        cache.put(key, renderedText);
        return renderedText;
    }

    private void applyRenderingHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private void drawTexture(ResourceLocation location, float x, float y, float width, float height) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(location);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        renderer.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        renderer.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        renderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    private static class RenderedText {
        private final ResourceLocation location;
        private final int textureWidth;
        private final int textureHeight;

        private RenderedText(ResourceLocation location, int textureWidth, int textureHeight) {
            this.location = location;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }
    }
}
