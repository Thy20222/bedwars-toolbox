package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.BedwarsToolbox;
import net.minecraft.client.gui.GuiScreen;

public class ConfigClickGuiScreen extends ClickGuiScreen {
    public ConfigClickGuiScreen(GuiScreen parentScreen) {
        super(parentScreen, BedwarsToolbox.INSTANCE.getGuiManager());
    }
}
