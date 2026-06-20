package dev.thy.bedwarstoolbox.core.gui;

import dev.thy.bedwarstoolbox.core.feature.FeatureManager;
import dev.thy.bedwarstoolbox.core.gui.component.FeaturePanel;
import dev.thy.bedwarstoolbox.core.gui.component.GuiComponent;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {
    private final FeatureManager featureManager;
    private final List<GuiComponent> components = new ArrayList<>();

    public ClickGuiScreen(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    @Override
    public void initGui() {
        components.clear();
        components.add(new FeaturePanel(20, 20, 160, featureManager));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        for (GuiComponent component : components) {
            component.render(mc, mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (GuiComponent component : components) {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (GuiComponent component : components) {
            component.mouseReleased(mouseX, mouseY, state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
