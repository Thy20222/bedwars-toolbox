package dev.thy.bedwarstoolbox.core.gui.component;

import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.config.Setting;
import dev.thy.bedwarstoolbox.core.gui.GuiManager;
import dev.thy.bedwarstoolbox.core.gui.font.TrueTypeFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.util.IdentityHashMap;
import java.util.Map;

public class GlobalSettingsPanel extends GuiComponent {
    private static final int HEADER_HEIGHT = 24;
    private static final int NUMBER_SETTING_HEIGHT = 26;
    private static final int COLOR_SETTING_HEIGHT = 76;
    private static final int COLLAPSED_COLOR_HEIGHT = 22;
    private static final int SLIDER_HEIGHT = 8;
    private static final int SPACING = 4;

    private final GuiManager guiManager;
    private final Map<ColorSetting, Boolean> collapsedColorSettings = new IdentityHashMap<>();
    private NumberSetting draggingNumberSetting;
    private ColorSetting draggingColorSetting;
    private int draggingColorChannel = -1;

    public GlobalSettingsPanel(int x, int y, int width, GuiManager guiManager) {
        super(x, y, width, HEADER_HEIGHT);
        this.guiManager = guiManager;
    }

    @Override
    public void render(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int panelHeight = getPanelHeight();
        TrueTypeFontRenderer font = guiManager.getFontRenderer();

        if (draggingNumberSetting != null && Mouse.isButtonDown(0)) {
            updateNumberSlider(draggingNumberSetting, mouseX);
        }
        if (draggingColorSetting != null && Mouse.isButtonDown(0)) {
            updateColorSlider(draggingColorSetting, draggingColorChannel, mouseX);
        }

        Gui.drawRect(x, y, x + width, y + panelHeight, guiManager.getPanelColor());
        Gui.drawRect(x, y, x + width, y + HEADER_HEIGHT, guiManager.getHeaderColor());
        Gui.drawRect(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, guiManager.getAccentColor());
        font.drawStringWithShadow("Global Settings", x + 10, y + 4.5f, 0xFFFFFFFF);

        int rowY = y + HEADER_HEIGHT + SPACING;
        for (Setting<?> setting : guiManager.getSettingManager().getGlobalSettings()) {
            if (setting instanceof NumberSetting) {
                renderNumberSetting((NumberSetting) setting, rowY);
            } else if (setting instanceof ColorSetting) {
                renderColorSetting((ColorSetting) setting, rowY);
            }

            rowY += getSettingHeight(setting) + SPACING;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }

        int rowY = y + HEADER_HEIGHT + SPACING;
        for (Setting<?> setting : guiManager.getSettingManager().getGlobalSettings()) {
            int settingHeight = getSettingHeight(setting);
            if (isMouseInside(mouseX, mouseY, x + 10, rowY, width - 20, settingHeight)) {
                handleSettingClick(setting, rowY, mouseX, mouseY, mouseButton);
                return;
            }

            rowY += settingHeight + SPACING;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingNumberSetting = null;
        draggingColorSetting = null;
        draggingColorChannel = -1;
    }

    private void renderNumberSetting(NumberSetting setting, int rowY) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        int sliderX = getSliderX();
        int sliderY = rowY + 14;
        int sliderWidth = getSliderWidth();
        double range = setting.getMaximum() - setting.getMinimum();
        double progress = range == 0.0D ? 0.0D : (setting.getValue() - setting.getMinimum()) / range;
        int fillWidth = Math.round((float) (sliderWidth * Math.max(0.0D, Math.min(1.0D, progress))));
        String value = formatNumber(setting.getValue());

        Gui.drawRect(x + 10, rowY, x + width - 10, rowY + NUMBER_SETTING_HEIGHT, guiManager.getSettingColor());
        font.drawString(setting.getName(), x + 16, rowY, 0xFFFFFFFF);
        font.drawString(value, x + width - 16 - font.getStringWidth(value), rowY, 0xFFE0E0E0);
        Gui.drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + SLIDER_HEIGHT, guiManager.getRowColor());
        Gui.drawRect(sliderX, sliderY, sliderX + fillWidth, sliderY + SLIDER_HEIGHT, guiManager.getAccentColor());
        Gui.drawRect(sliderX + fillWidth - 1, sliderY - 1, sliderX + fillWidth + 1, sliderY + SLIDER_HEIGHT + 1, 0xFFFFFFFF);
    }

    private void renderColorSetting(ColorSetting setting, int rowY) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        boolean collapsed = isColorSettingCollapsed(setting);

        Gui.drawRect(x + 10, rowY, x + width - 10, rowY + getSettingHeight(setting), guiManager.getSettingColor());
        font.drawString(setting.getName(), x + 16, rowY + 3, 0xFFFFFFFF);
        font.drawString(collapsed ? "+" : "-", x + width - 50, rowY + 3, 0xFFE0E0E0);
        Gui.drawRect(x + width - 34, rowY + 5, x + width - 14, rowY + 15, setting.toArgb());
        Gui.drawRect(x + width - 35, rowY + 4, x + width - 13, rowY + 5, 0x66FFFFFF);
        Gui.drawRect(x + width - 35, rowY + 15, x + width - 13, rowY + 16, 0x66FFFFFF);
        Gui.drawRect(x + width - 35, rowY + 4, x + width - 34, rowY + 16, 0x66FFFFFF);
        Gui.drawRect(x + width - 14, rowY + 4, x + width - 13, rowY + 16, 0x66FFFFFF);

        if (collapsed) {
            return;
        }

        int sliderY = rowY + 26;
        renderColorSlider(setting, 0, "R", setting.getRed(), sliderY, 0xFFE05252);
        renderColorSlider(setting, 1, "G", setting.getGreen(), sliderY + 12, 0xFF4FAA66);
        renderColorSlider(setting, 2, "B", setting.getBlue(), sliderY + 24, 0xFF4F7DCE);
        renderColorSlider(setting, 3, "A", setting.getAlpha(), sliderY + 36, 0xFFAAAAAA);
    }

    private void renderColorSlider(ColorSetting setting, int channel, String label, int value, int sliderY, int fillColor) {
        TrueTypeFontRenderer font = guiManager.getFontRenderer();
        int sliderX = getSliderX();
        int sliderWidth = getSliderWidth();
        int fillWidth = Math.round(sliderWidth * (value / 255.0F));

        font.drawString(label, x + 18, sliderY - 2, 0xFFE0E0E0);
        Gui.drawRect(sliderX, sliderY + 2, sliderX + sliderWidth, sliderY + 2 + SLIDER_HEIGHT, guiManager.getRowColor());
        Gui.drawRect(sliderX, sliderY + 1, sliderX + fillWidth, sliderY + 1 + SLIDER_HEIGHT, fillColor);
        Gui.drawRect(sliderX + fillWidth - 1, sliderY, sliderX + fillWidth + 1, sliderY + SLIDER_HEIGHT + 2, 0xFFFFFFFF);
        font.drawString(String.valueOf(getColorChannel(setting, channel)), x + width - 38, sliderY - 2, 0xFFE0E0E0);
    }

    private void handleSettingClick(Setting<?> setting, int rowY, int mouseX, int mouseY, int mouseButton) {
        if (setting instanceof NumberSetting && mouseButton == 0) {
            draggingNumberSetting = (NumberSetting) setting;
            updateNumberSlider(draggingNumberSetting, mouseX);
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting) setting;
            if (mouseButton == 1) {
                toggleColorSettingCollapsed(colorSetting);
                return;
            }

            int channel = getColorSliderChannel(colorSetting, rowY, mouseX, mouseY);
            if (channel != -1) {
                draggingColorSetting = colorSetting;
                draggingColorChannel = channel;
                updateColorSlider(colorSetting, channel, mouseX);
            }
        }
    }

    private void updateNumberSlider(NumberSetting setting, int mouseX) {
        int sliderX = getSliderX();
        int sliderWidth = getSliderWidth();
        double progress = (mouseX - sliderX) / (double) sliderWidth;
        progress = Math.max(0.0D, Math.min(1.0D, progress));
        setting.setValue(setting.getMinimum() + (setting.getMaximum() - setting.getMinimum()) * progress);
    }

    private int getColorSliderChannel(ColorSetting setting, int rowY, int mouseX, int mouseY) {
        if (isColorSettingCollapsed(setting)) {
            return -1;
        }

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
        return x + 42;
    }

    private int getSliderWidth() {
        return width - 92;
    }

    private int getSettingHeight(Setting<?> setting) {
        if (setting instanceof ColorSetting) {
            return isColorSettingCollapsed((ColorSetting) setting) ? COLLAPSED_COLOR_HEIGHT : COLOR_SETTING_HEIGHT;
        }
        if (setting instanceof NumberSetting) {
            return NUMBER_SETTING_HEIGHT;
        }
        return 0;
    }

    private int getPanelHeight() {
        int panelHeight = HEADER_HEIGHT;
        for (Setting<?> setting : guiManager.getSettingManager().getGlobalSettings()) {
            panelHeight += SPACING + getSettingHeight(setting);
        }
        return panelHeight;
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

    private String formatNumber(double value) {
        return String.format("%.2f", value);
    }
}
