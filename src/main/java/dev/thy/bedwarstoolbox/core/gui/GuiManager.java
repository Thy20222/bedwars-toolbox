package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.config.NumberSetting;
import dev.thy.bedwarstoolbox.core.config.SettingManager;
import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class GuiManager {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final FeatureManager featureManager;
    private final NumberSetting clickGuiX = new NumberSetting("ClickGui X", null, 20.0D, -1000.0D, 1000.0D);
    private final NumberSetting clickGuiY = new NumberSetting("ClickGui Y", null, 20.0D, -1000.0D, 1000.0D);
    private final NumberSetting clickGuiScale = new NumberSetting("ClickGui Scale", null, 0.85D, 0.5D, 1.5D);
    private final KeyBinding clickGuiKeyBinding = new KeyBinding(
            "key.bedwarstoolbox.click_gui",
            Keyboard.KEY_RSHIFT,
            "key.categories.bedwarstoolbox"
    );

    public GuiManager(FeatureManager featureManager, SettingManager settingManager) {
        this.featureManager = featureManager;
        settingManager.register(clickGuiX);
        settingManager.register(clickGuiY);
        settingManager.register(clickGuiScale);
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
}
