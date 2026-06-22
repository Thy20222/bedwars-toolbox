package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.config.ColorSetting;
import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.config.SettingManager;
import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import dev.thy.bedwarstoolbox.core.gui.font.TrueTypeFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class GuiManager {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final FeatureManager featureManager;
    private final SettingManager settingManager;
    private final TrueTypeFontRenderer fontRenderer = new TrueTypeFontRenderer(13.0F);
    private final NumberSetting clickGuiX = new NumberSetting("ClickGui X", null, 20.0D, -1000.0D, 1000.0D);
    private final NumberSetting clickGuiY = new NumberSetting("ClickGui Y", null, 20.0D, -1000.0D, 1000.0D);
    private final NumberSetting clickGuiScale = new NumberSetting("ClickGui Scale", null, 0.85D, 0.25D, 1.5D);
    private final ColorSetting panelColor = new ColorSetting("ClickGui Panel Color", 16, 18, 22, 205);
    private final ColorSetting headerColor = new ColorSetting("ClickGui Header Color", 28, 31, 36, 255);
    private final ColorSetting accentColor = new ColorSetting("ClickGui Accent Color", 64, 145, 96, 255);
    private final ColorSetting rowColor = new ColorSetting("ClickGui Row Color", 35, 38, 44, 230);
    private final ColorSetting settingColor = new ColorSetting("ClickGui Setting Color", 23, 25, 29, 220);
    private final ColorSetting disabledTextColor = new ColorSetting("ClickGui Disabled Text Color", 150, 154, 162, 255);
    private final KeyBinding clickGuiKeyBinding = new KeyBinding(
            "key.bedwarstoolbox.click_gui",
            Keyboard.KEY_RSHIFT,
            "key.categories.bedwarstoolbox"
    );

    public GuiManager(FeatureManager featureManager, SettingManager settingManager) {
        this.featureManager = featureManager;
        this.settingManager = settingManager;
        settingManager.register(clickGuiX);
        settingManager.register(clickGuiY);
        settingManager.register(clickGuiScale);
        settingManager.register(panelColor);
        settingManager.register(headerColor);
        settingManager.register(accentColor);
        settingManager.register(rowColor);
        settingManager.register(settingColor);
        settingManager.register(disabledTextColor);
    }

    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(clickGuiKeyBinding);
    }

    public void openClickGui() {
        minecraft.displayGuiScreen(new ClickGuiScreen(this));
    }

    public void closeClickGui() {
        if (minecraft.currentScreen instanceof ClickGuiScreen) {
            minecraft.displayGuiScreen(null);
        }
    }

    public void toggleClickGui() {
        if (minecraft.currentScreen instanceof ClickGuiScreen) {
            closeClickGui();
        } else {
            openClickGui();
        }
    }

    public int getClickGuiKeyCode() {
        return clickGuiKeyBinding.getKeyCode();
    }

    public void setClickGuiKeyCode(int clickGuiKeyCode) {
        clickGuiKeyBinding.setKeyCode(clickGuiKeyCode);
    }

    public boolean isClickGuiKeyPressed() {
        return clickGuiKeyBinding.isPressed();
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public SettingManager getSettingManager() {
        return settingManager;
    }

    public TrueTypeFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public double getClickGuiX() {
        return clickGuiX.getValue();
    }

    public void setClickGuiX(double x) {
        clickGuiX.setValue(x);
    }

    public double getClickGuiY() {
        return clickGuiY.getValue();
    }

    public void setClickGuiY(double y) {
        clickGuiY.setValue(y);
    }

    public double getClickGuiScale() {
        return clickGuiScale.getValue();
    }

    public void setClickGuiScale(double scale) {
        clickGuiScale.setValue(scale);
    }

    public int getPanelColor() {
        return panelColor.toArgb();
    }

    public int getHeaderColor() {
        return headerColor.toArgb();
    }

    public int getAccentColor() {
        return accentColor.toArgb();
    }

    public int getRowColor() {
        return rowColor.toArgb();
    }

    public int getSettingColor() {
        return settingColor.toArgb();
    }

    public int getDisabledTextColor() {
        return disabledTextColor.toArgb();
    }
}
