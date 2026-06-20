package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiManager {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final FeatureManager featureManager;
    private int clickGuiKeyCode = Keyboard.KEY_RSHIFT;

    public GuiManager(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    public void openClickGui() {
        minecraft.displayGuiScreen(new ClickGuiScreen(featureManager));
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
        return clickGuiKeyCode;
    }

    public void setClickGuiKeyCode(int clickGuiKeyCode) {
        this.clickGuiKeyCode = clickGuiKeyCode;
    }
}
