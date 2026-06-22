package dev.thy.bedwarstoolbox.core.gui.component;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.Setting;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.util.IdentityHashMap;
import java.util.Map;

public class FeaturePanel extends GuiComponent {
    private static final int HEADER_HEIGHT = 18;
    private static final int CATEGORY_HEIGHT = 18;
    private static final int FEATURE_ROW_HEIGHT = 18;
    private static final int BOOLEAN_SETTING_HEIGHT = 16;
    private static final int COLOR_SETTING_HEIGHT = 66;
    private static final int SLIDER_HEIGHT = 8;
    private static final int SPACING = 2;

    private final FeatureManager featureManager;
    private final Map<Feature, Boolean> collapsedFeatures = new IdentityHashMap<>();
    private final Map<ColorSetting, Boolean> collapsedColorSettings = new IdentityHashMap<>();
    private FeatureCategory activeCategory = FeatureCategory.RENDER;
    private ColorSetting draggingColorSetting;
    private int draggingColorChannel = -1;

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
        renderCategories(minecraft);

        int rowY = y + HEADER_HEIGHT + CATEGORY_HEIGHT + SPACING;
        for (Feature feature : featureManager.getFeatures(activeCategory)) {
            renderFeatureRow(minecraft, feature, rowY);
            rowY += FEATURE_ROW_HEIGHT + SPACING;

            if (isFeatureCollapsed(feature)) {
                continue;
            }

            for (Setting<?> setting : feature.getSettings()) {
                renderSetting(minecraft, feature, setting, rowY, mouseX, mouseY);
                rowY += getSettingHeight(setting) + SPACING;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }

        if (isMouseInside(mouseX, mouseY, x, y + HEADER_HEIGHT, width, CATEGORY_HEIGHT)) {
            if (mouseButton == 0) {
                handleCategoryClick(mouseX);
            }
            return;
        }

        int rowY = y + HEADER_HEIGHT + CATEGORY_HEIGHT + SPACING;
        for (Feature feature : featureManager.getFeatures(activeCategory)) {
            if (isMouseInside(mouseX, mouseY, x, rowY, width, FEATURE_ROW_HEIGHT)) {
                if (mouseButton == 0) {
                    feature.setEnabled(!feature.isEnabled());
                } else {
                    toggleFeatureCollapsed(feature);
                }
                return;
            }

            rowY += FEATURE_ROW_HEIGHT + SPACING;

            if (isFeatureCollapsed(feature)) {
                continue;
            }

            for (Setting<?> setting : feature.getSettings()) {
                int settingHeight = getSettingHeight(setting);
                if (isMouseInside(mouseX, mouseY, x + 8, rowY, width - 16, settingHeight)) {
                    handleSettingClick(feature, setting, rowY, mouseX, mouseY, mouseButton);
                    return;
                }

                rowY += settingHeight + SPACING;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingColorSetting = null;
        draggingColorChannel = -1;
    }

    private void renderFeatureRow(Minecraft minecraft, Feature feature, int rowY) {
        int color = feature.isEnabled() ? 0xFF2F7D4F : 0xFF303030;
        String state = feature.isEnabled() ? "ON" : "OFF";
        String collapseState = isFeatureCollapsed(feature) ? "+" : "-";

        Gui.drawRect(x + 4, rowY, x + width - 4, rowY + FEATURE_ROW_HEIGHT, color);
        minecraft.fontRendererObj.drawString(getFeatureName(feature), x + 8, rowY + 5, 0xFFFFFFFF);
        minecraft.fontRendererObj.drawString(state, x + width - 28, rowY + 5, 0xFFFFFFFF);
        minecraft.fontRendererObj.drawString(collapseState, x + width - 12, rowY + 5, 0xFFFFFFFF);
    }

    private void renderCategories(Minecraft minecraft) {
        FeatureCategory[] categories = FeatureCategory.values();
        int categoryWidth = width / categories.length;
        int categoryY = y + HEADER_HEIGHT;

        for (int i = 0; i < categories.length; i++) {
            FeatureCategory category = categories[i];
            int categoryX = x + i * categoryWidth;
            int right = i == categories.length - 1 ? x + width : categoryX + categoryWidth;
            int color = category == activeCategory ? 0xFF2F7D4F : 0xFF262626;

            Gui.drawRect(categoryX, categoryY, right, categoryY + CATEGORY_HEIGHT, color);
            minecraft.fontRendererObj.drawString(
                    category.getDisplayName(),
                    categoryX + 6,
                    categoryY + 5,
                    0xFFFFFFFF
            );
        }
    }

    private void handleCategoryClick(int mouseX) {
        FeatureCategory[] categories = FeatureCategory.values();
        int categoryWidth = width / categories.length;
        int index = Math.max(0, Math.min(categories.length - 1, (mouseX - x) / categoryWidth));
        activeCategory = categories[index];
    }

    private void renderSetting(Minecraft minecraft, Feature feature, Setting<?> setting, int rowY, int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting) {
            renderBooleanSetting(minecraft, feature, (BooleanSetting) setting, rowY);
        } else if (setting instanceof ColorSetting) {
            renderColorSetting(minecraft, (ColorSetting) setting, rowY, mouseX, mouseY);
        }
    }

    private void renderBooleanSetting(Minecraft minecraft, Feature feature, BooleanSetting setting, int rowY) {
        boolean value = setting.getValue();
        int color = value ? 0xFF2F7D4F : 0xFF252525;
        String state = value ? "ON" : "OFF";

        Gui.drawRect(x + 8, rowY, x + width - 8, rowY + BOOLEAN_SETTING_HEIGHT, 0xDD181818);
        Gui.drawRect(x + width - 34, rowY + 3, x + width - 12, rowY + BOOLEAN_SETTING_HEIGHT - 3, color);
        minecraft.fontRendererObj.drawString(setting.getName(), x + 12, rowY + 4, setting == feature.getEnabledSetting() ? 0xFFFFFFFF : 0xFFDDDDDD);
        minecraft.fontRendererObj.drawString(state, x + width - 31, rowY + 4, 0xFFFFFFFF);
    }

    private void renderColorSetting(Minecraft minecraft, ColorSetting setting, int rowY, int mouseX, int mouseY) {
        boolean collapsed = isColorSettingCollapsed(setting);
        if (!collapsed && draggingColorSetting == setting && Mouse.isButtonDown(0)) {
            updateColorSlider(setting, draggingColorChannel, mouseX);
        }

        Gui.drawRect(x + 8, rowY, x + width - 8, rowY + getSettingHeight(setting), 0xDD181818);
        minecraft.fontRendererObj.drawString(setting.getName(), x + 12, rowY + 4, 0xFFFFFFFF);
        minecraft.fontRendererObj.drawString(collapsed ? "+" : "-", x + width - 42, rowY + 4, 0xFFDDDDDD);
        Gui.drawRect(x + width - 28, rowY + 4, x + width - 12, rowY + 12, setting.toArgb());
        Gui.drawRect(x + width - 29, rowY + 3, x + width - 11, rowY + 4, 0x66FFFFFF);
        Gui.drawRect(x + width - 29, rowY + 12, x + width - 11, rowY + 13, 0x66FFFFFF);
        Gui.drawRect(x + width - 29, rowY + 3, x + width - 28, rowY + 13, 0x66FFFFFF);
        Gui.drawRect(x + width - 12, rowY + 3, x + width - 11, rowY + 13, 0x66FFFFFF);
        if (collapsed) {
            return;
        }

        int sliderY = rowY + 18;
        renderColorSlider(minecraft, setting, 0, "R", setting.getRed(), sliderY, 0xFFE05252);
        renderColorSlider(minecraft, setting, 1, "G", setting.getGreen(), sliderY + 12, 0xFF4FAA66);
        renderColorSlider(minecraft, setting, 2, "B", setting.getBlue(), sliderY + 24, 0xFF4F7DCE);
        renderColorSlider(minecraft, setting, 3, "A", setting.getAlpha(), sliderY + 36, 0xFFAAAAAA);
    }

    private void renderColorSlider(Minecraft minecraft, ColorSetting setting, int channel, String label, int value, int sliderY, int fillColor) {
        int sliderX = getSliderX();
        int sliderWidth = getSliderWidth();
        int fillWidth = Math.round(sliderWidth * (value / 255.0F));

        minecraft.fontRendererObj.drawString(label, x + 14, sliderY, 0xFFDDDDDD);
        Gui.drawRect(sliderX, sliderY + 1, sliderX + sliderWidth, sliderY + 1 + SLIDER_HEIGHT, 0xFF303030);
        Gui.drawRect(sliderX, sliderY + 1, sliderX + fillWidth, sliderY + 1 + SLIDER_HEIGHT, fillColor);
        Gui.drawRect(sliderX + fillWidth - 1, sliderY, sliderX + fillWidth + 1, sliderY + SLIDER_HEIGHT + 2, 0xFFFFFFFF);
        minecraft.fontRendererObj.drawString(String.valueOf(getColorChannel(setting, channel)), x + width - 34, sliderY, 0xFFDDDDDD);
    }

    private void handleSettingClick(Feature feature, Setting<?> setting, int rowY, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (setting instanceof ColorSetting) {
                toggleColorSettingCollapsed((ColorSetting) setting);
            }
            return;
        }

        if (setting instanceof BooleanSetting) {
            if (setting == feature.getEnabledSetting()) {
                feature.setEnabled(!feature.isEnabled());
            } else {
                BooleanSetting booleanSetting = (BooleanSetting) setting;
                booleanSetting.setValue(!booleanSetting.getValue());
            }
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting) setting;
            int channel = getColorSliderChannel(rowY, mouseX, mouseY);
            if (channel != -1) {
                draggingColorSetting = colorSetting;
                draggingColorChannel = channel;
                updateColorSlider(colorSetting, channel, mouseX);
            }
        }
    }

    private int getColorSliderChannel(int rowY, int mouseX, int mouseY) {
        int sliderX = getSliderX();
        int sliderY = rowY + 18;
        int sliderWidth = getSliderWidth();

        for (int channel = 0; channel < 4; channel++) {
            int channelY = sliderY + channel * 12;
            if (isMouseInside(mouseX, mouseY, sliderX, channelY, sliderWidth, SLIDER_HEIGHT + 2)) {
                return channel;
            }
        }

        return -1;
    }

    private void updateColorSlider(ColorSetting setting, int channel, int mouseX) {
        if (channel < 0) {
            return;
        }

        int sliderX = getSliderX();
        int sliderWidth = getSliderWidth();
        int value = Math.round((mouseX - sliderX) * 255.0F / sliderWidth);
        value = Math.max(0, Math.min(255, value));

        if (channel == 0) {
            setting.setRed(value);
        } else if (channel == 1) {
            setting.setGreen(value);
        } else if (channel == 2) {
            setting.setBlue(value);
        } else {
            setting.setAlpha(value);
        }
    }

    private int getColorChannel(ColorSetting setting, int channel) {
        if (channel == 0) {
            return setting.getRed();
        }
        if (channel == 1) {
            return setting.getGreen();
        }
        if (channel == 2) {
            return setting.getBlue();
        }
        return setting.getAlpha();
    }

    private int getSliderX() {
        return x + 38;
    }

    private int getSliderWidth() {
        return width - 82;
    }

    private int getSettingHeight(Setting<?> setting) {
        if (setting instanceof ColorSetting) {
            if (isColorSettingCollapsed((ColorSetting) setting)) {
                return BOOLEAN_SETTING_HEIGHT;
            }

            return COLOR_SETTING_HEIGHT;
        }

        return BOOLEAN_SETTING_HEIGHT;
    }

    private int getPanelHeight() {
        int panelHeight = HEADER_HEIGHT + CATEGORY_HEIGHT;
        for (Feature feature : featureManager.getFeatures(activeCategory)) {
            panelHeight += SPACING + FEATURE_ROW_HEIGHT;
            if (isFeatureCollapsed(feature)) {
                continue;
            }

            for (Setting<?> setting : feature.getSettings()) {
                panelHeight += SPACING + getSettingHeight(setting);
            }
        }

        return panelHeight;
    }

    private String getFeatureName(Feature feature) {
        return feature.getClass().getSimpleName();
    }

    private boolean isFeatureCollapsed(Feature feature) {
        return Boolean.TRUE.equals(collapsedFeatures.get(feature));
    }

    private void toggleFeatureCollapsed(Feature feature) {
        boolean collapsed = !isFeatureCollapsed(feature);
        collapsedFeatures.put(feature, collapsed);
        if (collapsed) {
            draggingColorSetting = null;
            draggingColorChannel = -1;
        }
    }

    private boolean isColorSettingCollapsed(ColorSetting setting) {
        return Boolean.TRUE.equals(collapsedColorSettings.get(setting));
    }

    private void toggleColorSettingCollapsed(ColorSetting setting) {
        boolean collapsed = !isColorSettingCollapsed(setting);
        collapsedColorSettings.put(setting, collapsed);
        if (collapsed && draggingColorSetting == setting) {
            draggingColorSetting = null;
            draggingColorChannel = -1;
        }
    }
}
