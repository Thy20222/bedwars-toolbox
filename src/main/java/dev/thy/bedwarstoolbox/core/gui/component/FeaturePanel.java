package dev.thy.bedwarstoolbox.core.gui.component;

import dev.thy.bedwarstoolbox.core.config.BooleanSetting;
import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.config.Setting;
import dev.thy.bedwarstoolbox.core.feature.Feature;
import dev.thy.bedwarstoolbox.core.feature.FeatureCategory;
import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import dev.thy.bedwarstoolbox.core.gui.GuiManager;
import dev.thy.bedwarstoolbox.core.gui.font.TrueTypeFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.util.IdentityHashMap;
import java.util.Map;

public class FeaturePanel extends GuiComponent {
    private static final int HEADER_HEIGHT = 24;
    private static final int CATEGORY_HEIGHT = 22;
    private static final int FEATURE_ROW_HEIGHT = 22;
    private static final int BOOLEAN_SETTING_HEIGHT = 20;
    private static final int COLOR_SETTING_HEIGHT = 76;
    private static final int SLIDER_HEIGHT = 8;
    private static final int SPACING = 4;
    private static final float TEXT_SCALE = 0.92F;
    private static final int NUMBER_SLIDER_EXTENSION = 8;

    private final GuiManager guiManager;
    private final FeatureManager featureManager;
    private final Map<Feature, Boolean> collapsedFeatures = new IdentityHashMap<>();
    private final Map<ColorSetting, Boolean> collapsedColorSettings = new IdentityHashMap<>();
    private FeatureCategory activeCategory;
    private ColorSetting draggingColorSetting;
    private int draggingColorChannel = -1;
    private NumberSetting draggingNumberSetting;

    public FeaturePanel(int x, int y, int width, GuiManager guiManager) {
        super(x, y, width, HEADER_HEIGHT);
        this.guiManager = guiManager;
        this.featureManager = guiManager.getFeatureManager();
        this.activeCategory = guiManager.getSelectedFeatureCategory();
    }

    @Override
    public void render(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int panelHeight = getPanelHeight();
        TrueTypeFontRenderer font = guiManager.getFontRenderer();

        Gui.drawRect(x, y, x + width, y + panelHeight, guiManager.getPanelColor());
        Gui.drawRect(x, y, x + width, y + HEADER_HEIGHT, guiManager.getHeaderColor());
        Gui.drawRect(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, guiManager.getAccentColor());
        drawTextWithShadow(font, "Features", x + 10, y + 5, 0xFFFFFFFF);
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
        draggingNumberSetting = null;
    }

    private void renderFeatureRow(Minecraft minecraft, Feature feature, int rowY) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        int color = feature.isEnabled() ? guiManager.getAccentColor() : guiManager.getRowColor();
        String state = feature.isEnabled() ? "ON" : "OFF";
        String collapseState = isFeatureCollapsed(feature) ? "+" : "-";
        int stateX = x + width - 36 - font.getStringWidth(state);

        Gui.drawRect(x + 6, rowY, x + width - 6, rowY + FEATURE_ROW_HEIGHT, color);
        Gui.drawRect(x + 6, rowY, x + 8, rowY + FEATURE_ROW_HEIGHT, 0x55FFFFFF);
        drawText(font, getFeatureName(feature), x + 12, rowY + 4, 0xFFFFFFFF);
        drawText(font, state, stateX, rowY + 4, 0xFFFFFFFF);
        drawText(font, collapseState, x + width - 18, rowY + 4, 0xFFFFFFFF);
    }

    private void renderCategories(Minecraft minecraft) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        FeatureCategory[] categories = FeatureCategory.values();
        int categoryWidth = width / categories.length;
        int categoryY = y + HEADER_HEIGHT;

        for (int i = 0; i < categories.length; i++) {
            FeatureCategory category = categories[i];
            int categoryX = x + i * categoryWidth;
            int right = i == categories.length - 1 ? x + width : categoryX + categoryWidth;
            int color = category == activeCategory ? guiManager.getAccentColor() : guiManager.getHeaderColor();
            String label = category.getDisplayName();

            Gui.drawRect(categoryX, categoryY, right, categoryY + CATEGORY_HEIGHT, color);
            drawText(font, label, categoryX + (categoryWidth - font.getStringWidth(label)) / 2, categoryY + 5, 0xFFFFFFFF);
        }
    }

    private void handleCategoryClick(int mouseX) {
        FeatureCategory[] categories = FeatureCategory.values();
        int categoryWidth = width / categories.length;
        int index = Math.max(0, Math.min(categories.length - 1, (mouseX - x) / categoryWidth));
        activeCategory = categories[index];
        guiManager.setSelectedFeatureCategory(activeCategory);
    }

    private void renderSetting(Minecraft minecraft, Feature feature, Setting<?> setting, int rowY, int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting) {
            renderBooleanSetting(minecraft, feature, (BooleanSetting) setting, rowY);
        } else if (setting instanceof ColorSetting) {
            renderColorSetting(minecraft, (ColorSetting) setting, rowY, mouseX, mouseY);
        } else if (setting instanceof NumberSetting) {
            renderNumberSetting(minecraft, (NumberSetting) setting, rowY, mouseX);
        }
    }

    private void renderBooleanSetting(Minecraft minecraft, Feature feature, BooleanSetting setting, int rowY) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        boolean value = setting.getValue();
        int color = value ? guiManager.getAccentColor() : guiManager.getRowColor();
        String state = value ? "ON" : "OFF";
        int stateX = x + width - 19 - font.getStringWidth(state);

        Gui.drawRect(x + 10, rowY, x + width - 10, rowY + BOOLEAN_SETTING_HEIGHT, guiManager.getSettingColor());
        Gui.drawRect(x + width - 50, rowY + 4, x + width - 14, rowY + BOOLEAN_SETTING_HEIGHT - 4, color);
        drawText(font, setting.getName(), x + 16, rowY + 3, setting == feature.getEnabledSetting() ? 0xFFFFFFFF : 0xFFE0E0E0);
        drawText(font, state, stateX, rowY + 3, 0xFFFFFFFF);
    }

    private void renderColorSetting(Minecraft minecraft, ColorSetting setting, int rowY, int mouseX, int mouseY) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        boolean collapsed = isColorSettingCollapsed(setting);
        if (!collapsed && draggingColorSetting == setting && Mouse.isButtonDown(0)) {
            updateColorSlider(setting, draggingColorChannel, mouseX);
        }

        Gui.drawRect(x + 10, rowY, x + width - 10, rowY + getSettingHeight(setting), guiManager.getSettingColor());
        drawText(font, setting.getName(), x + 16, rowY + 3, 0xFFFFFFFF);
        drawText(font, collapsed ? "+" : "-", x + width - 50, rowY + 3, 0xFFE0E0E0);
        Gui.drawRect(x + width - 34, rowY + 5, x + width - 14, rowY + 15, setting.toArgb());
        Gui.drawRect(x + width - 35, rowY + 4, x + width - 13, rowY + 5, 0x66FFFFFF);
        Gui.drawRect(x + width - 35, rowY + 15, x + width - 13, rowY + 16, 0x66FFFFFF);
        Gui.drawRect(x + width - 35, rowY + 4, x + width - 34, rowY + 16, 0x66FFFFFF);
        Gui.drawRect(x + width - 14, rowY + 4, x + width - 13, rowY + 16, 0x66FFFFFF);
        if (collapsed) {
            return;
        }

        int sliderY = rowY + 26;
        renderColorSlider(minecraft, setting, 0, "R", setting.getRed(), sliderY, 0xFFE05252);
        renderColorSlider(minecraft, setting, 1, "G", setting.getGreen(), sliderY + 12, 0xFF4FAA66);
        renderColorSlider(minecraft, setting, 2, "B", setting.getBlue(), sliderY + 24, 0xFF4F7DCE);
        renderColorSlider(minecraft, setting, 3, "A", setting.getAlpha(), sliderY + 36, 0xFFAAAAAA);
    }

    private void renderNumberSetting(Minecraft minecraft, NumberSetting setting, int rowY, int mouseX) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        if (draggingNumberSetting == setting && Mouse.isButtonDown(0)) {
            updateNumberSlider(setting, mouseX);
        }

        int sliderX = getNumberSliderX();
        int sliderWidth = getNumberSliderWidth();
        double range = setting.getMaximum() - setting.getMinimum();
        double normalized = range <= 0.0D ? 0.0D : (setting.getValue() - setting.getMinimum()) / range;
        normalized = Math.max(0.0D, Math.min(1.0D, normalized));
        int fillWidth = (int) Math.round(sliderWidth * normalized);
        String value = String.format(java.util.Locale.US, "%.2f", setting.getValue());

        Gui.drawRect(x + 10, rowY, x + width - 10, rowY + BOOLEAN_SETTING_HEIGHT, guiManager.getSettingColor());
        drawText(font, setting.getName(), x + 16, rowY, 0xFFE0E0E0);
        drawText(font, value, x + width - 16 - font.getStringWidth(value), rowY, 0xFFE0E0E0);
        Gui.drawRect(sliderX, rowY + BOOLEAN_SETTING_HEIGHT - 4, sliderX + sliderWidth, rowY + BOOLEAN_SETTING_HEIGHT - 1, guiManager.getRowColor());
        Gui.drawRect(sliderX, rowY + BOOLEAN_SETTING_HEIGHT - 5, sliderX + fillWidth, rowY + BOOLEAN_SETTING_HEIGHT, guiManager.getAccentColor());
        Gui.drawRect(sliderX + fillWidth - 1, rowY + BOOLEAN_SETTING_HEIGHT - 6, sliderX + fillWidth + 1, rowY + BOOLEAN_SETTING_HEIGHT + 1, 0xFFFFFFFF);
    }

    private void renderColorSlider(Minecraft minecraft, ColorSetting setting, int channel, String label, int value, int sliderY, int fillColor) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        int sliderX = getSliderX();
        int sliderWidth = getSliderWidth();
        int fillWidth = Math.round(sliderWidth * (value / 255.0F));

        drawText(font, label, x + 18, sliderY - 2, 0xFFE0E0E0);
        Gui.drawRect(sliderX, sliderY + 2, sliderX + sliderWidth, sliderY + 2 + SLIDER_HEIGHT, guiManager.getRowColor());
        Gui.drawRect(sliderX, sliderY + 1, sliderX + fillWidth, sliderY + 1 + SLIDER_HEIGHT, fillColor);
        Gui.drawRect(sliderX + fillWidth - 1, sliderY, sliderX + fillWidth + 1, sliderY + SLIDER_HEIGHT + 2, 0xFFFFFFFF);
        drawText(font, String.valueOf(getColorChannel(setting, channel)), x + width - 38, sliderY - 2, 0xFFE0E0E0);
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
        } else if (setting instanceof NumberSetting) {
            draggingNumberSetting = (NumberSetting) setting;
            updateNumberSlider(draggingNumberSetting, mouseX);
        }
    }

    private int getColorSliderChannel(int rowY, int mouseX, int mouseY) {
        int sliderX = getSliderX();
        int sliderY = rowY + 26;
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

    private void updateNumberSlider(NumberSetting setting, int mouseX) {
        int sliderX = getNumberSliderX();
        int sliderWidth = getNumberSliderWidth();
        double normalized = (mouseX - sliderX) / (double) sliderWidth;
        normalized = Math.max(0.0D, Math.min(1.0D, normalized));
        double value = setting.getMinimum() + (setting.getMaximum() - setting.getMinimum()) * normalized;

        setting.setValue(Math.round(value * 100.0D) / 100.0D);
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

    private int getNumberSliderX() {
        return getSliderX() - NUMBER_SLIDER_EXTENSION;
    }

    private int getNumberSliderWidth() {
        return getSliderWidth() + NUMBER_SLIDER_EXTENSION * 2;
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

        return panelHeight + SPACING;
    }

    private String getFeatureName(Feature feature) {
        return feature.getClass().getSimpleName()
                .replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");
    }

    private void drawText(TrueTypeFontRenderer font, String text, float textX, float textY, int color) {
        font.drawStringScaled(text, textX, textY, color, TEXT_SCALE);
    }

    private void drawTextWithShadow(TrueTypeFontRenderer font, String text, float textX, float textY, int color) {
        font.drawStringWithShadowScaled(text, textX, textY, color, TEXT_SCALE);
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
            draggingNumberSetting = null;
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
