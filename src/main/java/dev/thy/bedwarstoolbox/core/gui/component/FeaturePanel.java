package dev.thy.bedwarstoolbox.core.gui.component;

import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class FeaturePanel extends GuiComponent {
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 16;
    private static final int SPACING = 2;

    private final FeatureManager featureManager;

    public FeaturePanel(int x, int y, int width, FeatureManager featureManager) {
        super(x, y, width, HEADER_HEIGHT);
        this.featureManager = featureManager;
    }

    @Override
    public void render(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int panelHeight = getPanelHeight();

        Gui.drawRect(x, y, x + width, y + panelHeight, 0xAA101010);
        Gui.drawRect(x, y, x + width, y + HEADER_HEIGHT, 0xFF202020);
        minecraft.fontRendererObj.drawStringWithShadow("Features", x + 6, y + 5, 0xFFFFFFFF);

        int rowY = y + HEADER_HEIGHT + SPACING;
        for (Feature feature : featureManager.getFeatures()) {
            renderFeatureRow(minecraft, feature, rowY);
            rowY += ROW_HEIGHT + SPACING;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        int rowY = y + HEADER_HEIGHT + SPACING;
        for (Feature feature : featureManager.getFeatures()) {
            if (isMouseInside(mouseX, mouseY, x, rowY, width, ROW_HEIGHT)) {
                feature.setEnabled(!feature.isEnabled());
                return;
            }

            rowY += ROW_HEIGHT + SPACING;
        }
    }

    private void renderFeatureRow(Minecraft minecraft, Feature feature, int rowY) {
        int color = feature.isEnabled() ? 0xFF2F7D4F : 0xFF303030;
        String state = feature.isEnabled() ? "ON" : "OFF";

        Gui.drawRect(x + 4, rowY, x + width - 4, rowY + ROW_HEIGHT, color);
        minecraft.fontRendererObj.drawString(getFeatureName(feature), x + 8, rowY + 4, 0xFFFFFFFF);
        minecraft.fontRendererObj.drawString(state, x + width - 28, rowY + 4, 0xFFFFFFFF);
    }

    private int getPanelHeight() {
        int featureCount = featureManager.getFeatures().size();
        if (featureCount == 0) {
            return HEADER_HEIGHT;
        }

        return HEADER_HEIGHT + SPACING + featureCount * (ROW_HEIGHT + SPACING);
    }

    private String getFeatureName(Feature feature) {
        return feature.getClass().getSimpleName();
    }
}
